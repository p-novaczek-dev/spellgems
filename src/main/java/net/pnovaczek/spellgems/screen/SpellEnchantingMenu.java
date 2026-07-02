package net.pnovaczek.spellgems.screen;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.pnovaczek.spellgems.ModBlocks;
import net.pnovaczek.spellgems.ModItems;
import net.pnovaczek.spellgems.ModTags;
import net.pnovaczek.spellgems.ModComponents;
import net.pnovaczek.spellgems.ModMenuTypes;
import net.pnovaczek.spellgems.item.data.SpellGemData;
import net.pnovaczek.spellgems.item.data.TomeData;
import net.pnovaczek.spellgems.network.ModNetworking;
import net.pnovaczek.spellgems.recipe.SpellEnchantingRecipe;
import net.pnovaczek.spellgems.recipe.SpellEnchantingRecipeLookup;
import net.pnovaczek.spellgems.spell.Spells;
import net.pnovaczek.spellgems.spell.enchantment.ModifierEnchantment;
import net.pnovaczek.spellgems.spell.enchantment.ModifierEnchantments;
import net.pnovaczek.spellgems.spell.enchantment.StrikeEnchantment;
import net.pnovaczek.spellgems.spell.enchantment.StrikeEnchantments;
import net.pnovaczek.spellgems.spell.enchantment.PotionEnchantment;
import net.pnovaczek.spellgems.spell.enchantment.PotionEnchantments;
import net.pnovaczek.spellgems.spell.enchantment.UtilityEnchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class SpellEnchantingMenu extends AbstractContainerMenu {

    public static final int CATALYST_KIND_ITEM = 0;
    public static final int CATALYST_KIND_ANY_POTION = 1;

    private static final int DATA_RECIPE_COUNT = 0;
    private static final int DATA_RECIPE_START = 1;

    private final Container enchantSlots = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            super.setChanged();
            SpellEnchantingMenu.this.slotsChanged(this);
        }
    };

    private final ContainerLevelAccess access;
    private final ContainerData recipeData;
    private final Player viewer;
    private final Random random = new Random();
    private ItemStack lastTarget = ItemStack.EMPTY;
    private String[] recipeDescriptions = new String[0];

    public SpellEnchantingMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, ContainerLevelAccess.NULL, createClientRecipeData());
    }

    public SpellEnchantingMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
        this(containerId, inventory, access, createServerRecipeData());
    }

    private SpellEnchantingMenu(int containerId, Inventory inventory, ContainerLevelAccess access, ContainerData recipeData) {
        super(ModMenuTypes.SPELL_ENCHANTING_TABLE, containerId);
        this.access = access;
        this.recipeData = recipeData;
        this.viewer = inventory.player;

        this.addSlot(new Slot(this.enchantSlots, 0, 15, 47) {
            @Override public int getMaxStackSize() { return 1; }
            @Override public boolean mayPlace(ItemStack stack) { return isEnchantableTarget(stack); }
        });

        this.addSlot(new Slot(this.enchantSlots, 1, 35, 47) {
            @Override public int getMaxStackSize() { return 64; }
            @Override public boolean mayPlace(ItemStack stack) { return isCatalystItem(stack); }
        });

        this.addStandardInventorySlots(inventory, 8, 84);
        this.addDataSlots(this.recipeData);
    }

    private static ContainerData createServerRecipeData() {
        return new SimpleContainerData(1 + SpellEnchantingRecipeLookup.MAX_RECIPES * SpellEnchantingRecipeLookup.FIELDS_PER_RECIPE);
    }

    private static ContainerData createClientRecipeData() {
        return createServerRecipeData();
    }

    @Override
    public void slotsChanged(Container container) {
        if (container != this.enchantSlots) {
            return;
        }

        ItemStack targetStack = this.enchantSlots.getItem(0);
        if (targetStack.isEmpty()) {
            if (!this.lastTarget.isEmpty()) {
                this.lastTarget = ItemStack.EMPTY;
                this.access.execute((level, pos) -> {
                    if (level.isClientSide()) {
                        return;
                    }
                    clearRecipeData();
                    syncRecipeDescriptions(List.of());
                    this.broadcastChanges();
                });
            }
            return;
        }

        // Recipe list depends only on the target item; catalyst changes are reflected client-side
        // via requirement checks without rebuilding the synced recipe list.
        if (ItemStack.matches(targetStack, this.lastTarget)) {
            return;
        }

        this.lastTarget = targetStack.copy();

        this.access.execute((level, pos) -> {
            if (level.isClientSide()) {
                return;
            }

            clearRecipeData();

            if (!(level.recipeAccess() instanceof RecipeManager recipeManager)) {
                this.broadcastChanges();
                return;
            }

            List<RecipeHolder<SpellEnchantingRecipe>> recipes =
                    SpellEnchantingRecipeLookup.findRecipesForTarget(recipeManager, targetStack);
            int recipeCount = Math.min(recipes.size(), SpellEnchantingRecipeLookup.MAX_RECIPES);
            this.recipeData.set(DATA_RECIPE_COUNT, recipeCount);

            List<String> descriptions = new ArrayList<>(recipeCount);
            for (int i = 0; i < recipeCount; i++) {
                SpellEnchantingRecipe recipe = recipes.get(i).value();
                int base = recipeDataIndex(i);
                this.recipeData.set(base, recipe.getLevelRequirement());
                this.recipeData.set(base + 1, recipe.getXpCost());
                this.recipeData.set(base + 2, catalystItemId(recipe));
                this.recipeData.set(base + 3, recipe.getCatalystDef().count());
                this.recipeData.set(base + 4, catalystKind(recipe));
                descriptions.add(recipe.getDescription());
            }

            syncRecipeDescriptions(descriptions);
            this.broadcastChanges();
        });
    }

    private void syncRecipeDescriptions(List<String> descriptions) {
        if (this.viewer instanceof ServerPlayer serverPlayer) {
            ModNetworking.sendRecipeDescriptions(serverPlayer, this, descriptions);
        }
    }

    public void setRecipeDescriptions(List<String> descriptions) {
        this.recipeDescriptions = descriptions.toArray(String[]::new);
    }

    public String getRecipeDescription(int recipeIndex) {
        if (recipeIndex < 0 || recipeIndex >= this.recipeDescriptions.length) {
            return "";
        }
        return this.recipeDescriptions[recipeIndex];
    }

    @Override
    public boolean clickMenuButton(Player player, int recipeIndex) {
        if (recipeIndex < 0 || recipeIndex >= getRecipeCount()) {
            return false;
        }

        ItemStack targetStack = this.enchantSlots.getItem(0);
        ItemStack catalystStack = this.enchantSlots.getItem(1);

        if (targetStack.isEmpty()) {
            return false;
        }

        this.access.execute((level, pos) -> {
            if (level.isClientSide()) {
                return;
            }

            if (!(level.recipeAccess() instanceof RecipeManager recipeManager)) {
                return;
            }

            RecipeHolder<SpellEnchantingRecipe> recipeHolder =
                    SpellEnchantingRecipeLookup.getRecipeAt(recipeManager, targetStack, recipeIndex);
            if (recipeHolder == null) {
                return;
            }

            SpellEnchantingRecipe recipe = recipeHolder.value();

            if (!recipe.getCatalystDef().hasSufficient(catalystStack)) {
                return;
            }

            if (player.experienceLevel < recipe.getLevelRequirement()
                    || player.totalExperience < recipe.getXpCost()) {
                return;
            }

            if (targetStack.is(Items.BOOK)) {
                TomeData newData = applyTomeRecipe(TomeData.create(), recipe);
                if (!newData.isEnchanted()) {
                    return;
                }
                ItemStack result = new ItemStack(ModItems.SPELL_TOME);
                result.set(ModComponents.TOME_DATA, newData);
                this.enchantSlots.setItem(0, result);
            } else {
                SpellGemData currentData = targetStack.getOrDefault(
                        ModComponents.SPELL_GEM_DATA,
                        SpellGemData.create(Spells.PROJECTILE)
                );
                if (currentData.isEnchanted()) {
                    return;
                }
                SpellGemData newData = applySpellGemRecipe(currentData, recipe, catalystStack);
                if (newData == null) {
                    return;
                }
                targetStack.set(ModComponents.SPELL_GEM_DATA, newData);
            }

            catalystStack.shrink(recipe.getCatalystDef().count());
            if (catalystStack.isEmpty()) {
                this.enchantSlots.setItem(1, ItemStack.EMPTY);
            }

            player.giveExperiencePoints(-recipe.getXpCost());

            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ENCHANTMENT_TABLE_USE,
                    net.minecraft.sounds.SoundSource.BLOCKS, 1.0F,
                    level.getRandom().nextFloat() * 0.1F + 0.9F);

            this.enchantSlots.setChanged();
            this.slotsChanged(this.enchantSlots);
        });
        return true;
    }

    private void clearRecipeData() {
        for (int i = 0; i < this.recipeData.getCount(); i++) {
            this.recipeData.set(i, 0);
        }
    }

    private static int recipeDataIndex(int recipeIndex) {
        return DATA_RECIPE_START + recipeIndex * SpellEnchantingRecipeLookup.FIELDS_PER_RECIPE;
    }

    private static int catalystItemId(SpellEnchantingRecipe recipe) {
        Identifier catalystId = recipe.getCatalystDef().item();
        Optional<Item> item = BuiltInRegistries.ITEM.get(catalystId).map(holder -> holder.value());
        return item.map(Item::getId).orElse(0);
    }

    private static int catalystKind(SpellEnchantingRecipe recipe) {
        return recipe.getCatalystDef().anyPotion() ? CATALYST_KIND_ANY_POTION : CATALYST_KIND_ITEM;
    }

    public int getRecipeCount() {
        return this.recipeData.get(DATA_RECIPE_COUNT);
    }

    public int getLevelRequirement(int recipeIndex) {
        return getRecipeField(recipeIndex, 0);
    }

    public int getXpCost(int recipeIndex) {
        return getRecipeField(recipeIndex, 1);
    }

    public int getCatalystItemId(int recipeIndex) {
        return getRecipeField(recipeIndex, 2);
    }

    public int getCatalystCount(int recipeIndex) {
        return getRecipeField(recipeIndex, 3);
    }

    public int getCatalystKind(int recipeIndex) {
        return getRecipeField(recipeIndex, 4);
    }

    private int getRecipeField(int recipeIndex, int fieldOffset) {
        if (recipeIndex < 0 || recipeIndex >= getRecipeCount()) {
            return 0;
        }
        return this.recipeData.get(recipeDataIndex(recipeIndex) + fieldOffset);
    }

    private TomeData applyTomeRecipe(TomeData data, SpellEnchantingRecipe recipe) {
        var result = recipe.getResult();

        if (result.modifiers().orElse(0) > 0) {
            List<Identifier> modifiers = ModifierEnchantments.getAll();
            if (!modifiers.isEmpty()) {
                return new TomeData(modifiers.get(random.nextInt(modifiers.size())));
            }
        }

        if (result.strikes().orElse(0) > 0) {
            List<Identifier> strikes = StrikeEnchantments.getAll();
            if (!strikes.isEmpty()) {
                return new TomeData(strikes.get(random.nextInt(strikes.size())));
            }
        }

        if (result.utility().isPresent()) {
            return new TomeData(result.utility().get());
        }

        return data;
    }

    private SpellGemData applySpellGemRecipe(SpellGemData data, SpellEnchantingRecipe recipe, ItemStack catalystStack) {
        var result = recipe.getResult();

        List<ModifierEnchantment> newModifiers = new ArrayList<>(data.modifierEffects());
        List<StrikeEnchantment> newStrikes = new ArrayList<>(data.strikeEffects());
        List<UtilityEnchantment> newUtilities = new ArrayList<>(data.utilityEffects());
        List<PotionEnchantment> newPotions = new ArrayList<>(data.potionEffects());

        result.modifiers().ifPresent(count -> {
            List<Identifier> compatible = ModifierEnchantments.getCompatible(data.spellId());
            for (int i = 0; i < count && !compatible.isEmpty(); i++) {
                Identifier id = compatible.get(random.nextInt(compatible.size()));
                newModifiers.add(new ModifierEnchantment(id));
            }
        });

        result.strikes().ifPresent(count -> {
            List<Identifier> allStrikes = StrikeEnchantments.getAll();
            for (int i = 0; i < count && !allStrikes.isEmpty(); i++) {
                Identifier id = allStrikes.get(random.nextInt(allStrikes.size()));
                newStrikes.add(new StrikeEnchantment(id));
            }
        });

        result.utility().ifPresent(enchantId -> newUtilities.add(new UtilityEnchantment(enchantId)));

        if (result.potion()) {
            PotionEnchantment potionEnchantment = PotionEnchantments.fromCatalyst(catalystStack);
            if (potionEnchantment == null) {
                return null;
            }
            newPotions.add(potionEnchantment);
        }

        return new SpellGemData(
                data.spellId(),
                newModifiers,
                newStrikes,
                newUtilities,
                newPotions
        );
    }

    private static boolean isEnchantableTarget(ItemStack stack) {
        return stack.is(Items.BOOK)
                || stack.is(ModTags.COMBAT_SPELL_GEMS)
                || stack.is(ModTags.UTILITY_SPELL_GEMS);
    }

    private static boolean isCatalystItem(ItemStack stack) {
        return stack.is(Items.LAPIS_LAZULI) || PotionEnchantments.isValidCatalyst(stack);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack clicked = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            clicked = stack.copy();

            if (slotIndex == 0 || slotIndex == 1) {
                if (!this.moveItemStackTo(stack, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (isCatalystItem(stack)) {
                if (!this.moveItemStackTo(stack, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (isEnchantableTarget(stack)) {
                if (!this.moveItemStackTo(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == clicked.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stack);
        }

        return clicked;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.SPELL_ENCHANTING_TABLE);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> this.clearContainer(player, this.enchantSlots));
    }
}