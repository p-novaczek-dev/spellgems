package net.pnovaczek.spellgems.screen;

import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.pnovaczek.spellgems.ModBlocks;
import net.pnovaczek.spellgems.ModTags;
import net.pnovaczek.spellgems.ModComponents;
import net.pnovaczek.spellgems.ModMenuTypes;
import net.pnovaczek.spellgems.item.data.SpellGemData;
import net.pnovaczek.spellgems.recipe.SpellEnchantingRecipe;
import net.pnovaczek.spellgems.recipe.SpellEnchantingRecipeInput;
import net.pnovaczek.spellgems.spell.Spells;
import net.pnovaczek.spellgems.spell.enchantment.ModifierEnchantment;
import net.pnovaczek.spellgems.spell.enchantment.ModifierEnchantments;
import net.pnovaczek.spellgems.spell.enchantment.StrikeEnchantment;
import net.pnovaczek.spellgems.spell.enchantment.StrikeEnchantments;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class SpellEnchantingMenu extends AbstractContainerMenu {

    private final Container enchantSlots = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            super.setChanged();
            SpellEnchantingMenu.this.slotsChanged(this);
        }
    };

    private final ContainerLevelAccess access;
    private final Random random = new Random();

    // One entry per button (0=top/utility, 1=middle/combat, 2=bottom/future)
    private final int[] levelRequirements = new int[3];
    private final int[] xpCosts = new int[3];

    public SpellEnchantingMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, ContainerLevelAccess.NULL);
    }

    public SpellEnchantingMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
        super(ModMenuTypes.SPELL_ENCHANTING_TABLE, containerId);
        this.access = access;

        // Target slot (spell gem or catalyst book)
        this.addSlot(new Slot(this.enchantSlots, 0, 15, 47) {
            @Override public int getMaxStackSize() { return 1; }
            @Override public boolean mayPlace(ItemStack stack) { return isEnchantableTarget(stack); }
        });

        // Catalyst slot (stacks allowed; recipe defines how many are consumed per craft)
        this.addSlot(new Slot(this.enchantSlots, 1, 35, 47) {
            @Override public int getMaxStackSize() { return 64; }
            @Override public boolean mayPlace(ItemStack stack) { return isCatalystItem(stack); }
        });

        this.addStandardInventorySlots(inventory, 8, 84);

        // Register preview data for all 3 buttons (vanilla style)
        for (int i = 0; i < 3; i++) {
            this.addDataSlot(DataSlot.shared(this.levelRequirements, i));
            this.addDataSlot(DataSlot.shared(this.xpCosts, i));
        }
    }

    @Override
    public void slotsChanged(Container container) {
        if (container != this.enchantSlots) {
            return;
        }

        ItemStack targetStack = this.enchantSlots.getItem(0);
        ItemStack catalystStack = this.enchantSlots.getItem(1);

        // Clear all buttons by default
        for (int i = 0; i < 3; i++) {
            this.levelRequirements[i] = 0;
            this.xpCosts[i] = 0;
        }

        if (targetStack.isEmpty() || catalystStack.isEmpty()) {
            this.access.execute((level, pos) -> this.broadcastChanges());
            return;
        }

        this.access.execute((level, pos) -> {
            if (level.isClientSide()) {
                return;
            }

            SpellEnchantingRecipeInput recipeInput = new SpellEnchantingRecipeInput(targetStack, catalystStack);

            // Button 0 (top) = utility, button 1 (middle) = combat
            findRecipeForButton(0, recipeInput, level);
            findRecipeForButton(1, recipeInput, level);
            // Button 2 (bottom) = future (tomes / catalyst books) - leave at 0 for now

            this.broadcastChanges();
        });
    }

    private void findRecipeForButton(int buttonIndex, SpellEnchantingRecipeInput input, net.minecraft.world.level.Level level) {
        findMatchingRecipe(buttonIndex, input, level).ifPresent(recipe -> {
            this.levelRequirements[buttonIndex] = recipe.getLevelRequirement();
            this.xpCosts[buttonIndex] = recipe.getXpCost();
        });
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId < 0 || buttonId > 2) return false;

        ItemStack targetStack = this.enchantSlots.getItem(0);
        ItemStack catalystStack = this.enchantSlots.getItem(1);

        if (targetStack.isEmpty() || catalystStack.isEmpty()) {
            return false;
        }

        SpellEnchantingRecipeInput recipeInput = new SpellEnchantingRecipeInput(targetStack, catalystStack);

        this.access.execute((level, pos) -> {
            if (level.isClientSide()) {
                return;
            }

            Optional<SpellEnchantingRecipe> optionalRecipe = findMatchingRecipe(buttonId, recipeInput, level);
            if (optionalRecipe.isEmpty()) {
                return;
            }

            SpellEnchantingRecipe recipe = optionalRecipe.get();

            if (!recipe.getCatalystDef().hasSufficient(catalystStack)) {
                return;
            }

            // XP / level checks
            if (player.experienceLevel < recipe.getLevelRequirement() ||
                    player.totalExperience < recipe.getXpCost()) {
                return;
            }

            SpellGemData currentData = targetStack.getOrDefault(
                    ModComponents.SPELL_GEM_DATA,
                    SpellGemData.create(Spells.PROJECTILE) // fallback
            );

            SpellGemData newData = applyRecipeEffects(currentData, recipe);

            targetStack.set(ModComponents.SPELL_GEM_DATA, newData);

            // Consume only the recipe-required amount from the catalyst stack
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

    private static Optional<SpellEnchantingRecipe> findMatchingRecipe(
            int buttonId, SpellEnchantingRecipeInput input, net.minecraft.world.level.Level level) {
        String category = categoryForButton(buttonId);
        if (category == null) {
            return Optional.empty();
        }

        if (!(level.recipeAccess() instanceof RecipeManager recipeManager)) {
            return Optional.empty();
        }

        for (var holder : recipeManager.getRecipes()) {
            if (holder.value().getType() != SpellEnchantingRecipe.TYPE) continue;
            if (!(holder.value() instanceof SpellEnchantingRecipe recipe)) continue;
            if (!category.equals(recipe.getCategory())) continue;
            if (recipe.matches(input, level)) {
                return Optional.of(recipe);
            }
        }
        return Optional.empty();
    }

    private static @org.jspecify.annotations.Nullable String categoryForButton(int buttonId) {
        return switch (buttonId) {
            case 0 -> "utility";
            case 1 -> "combat";
            default -> null;
        };
    }

    private SpellGemData applyRecipeEffects(SpellGemData data, SpellEnchantingRecipe recipe) {
        var result = recipe.getResult();

        List<ModifierEnchantment> newModifiers = new ArrayList<>(data.modifierEffects());
        List<StrikeEnchantment> newStrikes = new ArrayList<>(data.strikeEffects());

        // Combat - random selection
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

        // Utility - deterministic
        result.utility().ifPresent(enchantId -> {
            // TODO: apply utility enchantment via your utility system
        });

        if (result.potion()) {
            // TODO: apply potion-based effect (depends on catalyst potion)
        }

        return new SpellGemData(
                data.spellId(),
                newModifiers,
                newStrikes,
                data.utilityEffects(),
                data.potionEffects()
        );
    }

    private <T> List<T> addIfNotPresent(List<T> list, T element) {
        if (!list.contains(element)) {
            return com.google.common.collect.ImmutableList.<T>builder()
                    .addAll(list).add(element).build();
        }
        return list;
    }

    /** Legacy single-button getters (point at middle/combat button for backward compat) */
    public int getLevelRequirement() {
        return this.levelRequirements[1];
    }

    public int getXpCost() {
        return this.xpCosts[1];
    }

    /** Per-button access for the 3-button vanilla-style layout */
    public int getLevelRequirement(int button) {
        return (button >= 0 && button < 3) ? this.levelRequirements[button] : 0;
    }

    public int getXpCost(int button) {
        return (button >= 0 && button < 3) ? this.xpCosts[button] : 0;
    }

    private static boolean isEnchantableTarget(ItemStack stack) {
        return stack.is(ModTags.COMBAT_SPELL_GEMS)
                || stack.is(ModTags.UTILITY_SPELL_GEMS)
                || stack.is(ModTags.CATALYST_BOOKS);
    }

    private static boolean isCatalystItem(ItemStack stack) {
        return stack.is(Items.LAPIS_LAZULI);
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