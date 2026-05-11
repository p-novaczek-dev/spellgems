package net.pnovaczek.spellgems.screen;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.pnovaczek.spellgems.ModBlocks;
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

    public SpellEnchantingMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, ContainerLevelAccess.NULL);
    }

    public SpellEnchantingMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
        super(ModMenuTypes.SPELL_ENCHANTING_TABLE, containerId);
        this.access = access;

        // Target slot (spell gem or catalyst book)
        this.addSlot(new Slot(this.enchantSlots, 0, 15, 47) {
            @Override public int getMaxStackSize() { return 1; }
        });

        // Catalyst slot
        this.addSlot(new Slot(this.enchantSlots, 1, 35, 47) {
            @Override public boolean mayPlace(ItemStack stack) { return true; } // validated by recipe
        });

        this.addStandardInventorySlots(inventory, 8, 84);
    }

    @Override
    public void slotsChanged(Container container) {
        if (container == this.enchantSlots) {
            this.broadcastChanges();
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId != 0) return false;

        ItemStack targetStack = this.enchantSlots.getItem(0);
        ItemStack catalystStack = this.enchantSlots.getItem(1);

        if (targetStack.isEmpty() || catalystStack.isEmpty()) {
            return false;
        }

        SpellEnchantingRecipeInput recipeInput = new SpellEnchantingRecipeInput(targetStack, catalystStack);

        this.access.execute((level, pos) -> {
            if (level instanceof ServerLevel serverLevel) {
                Optional<RecipeHolder<SpellEnchantingRecipe>> optionalRecipe =
                        serverLevel.recipeAccess().getRecipeFor(SpellEnchantingRecipe.TYPE, recipeInput, level);

                if (optionalRecipe.isEmpty()) return;

                SpellEnchantingRecipe recipe = optionalRecipe.get().value();

                // XP / level checks
                if (player.experienceLevel < recipe.getLevelRequirement() ||
                        player.totalExperience < recipe.getXpCost()) {
                    return;
                }

                // Apply enchantment effects to SpellGemData
                SpellGemData currentData = targetStack.getOrDefault(
                        ModComponents.SPELL_GEM_DATA,
                        SpellGemData.create(Spells.PROJECTILE) // fallback
                );

                SpellGemData newData = applyRecipeEffects(currentData, recipe);

                targetStack.set(ModComponents.SPELL_GEM_DATA, newData);

                // Consume catalyst
                catalystStack.shrink(recipe.getCatalystDef().count());
                if (catalystStack.isEmpty()) {
                    this.enchantSlots.setItem(1, ItemStack.EMPTY);
                }

                // Consume XP
                player.giveExperiencePoints(-recipe.getXpCost());

                // Feedback
                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ENCHANTMENT_TABLE_USE,
                        net.minecraft.sounds.SoundSource.BLOCKS, 1.0F,
                        level.getRandom().nextFloat() * 0.1F + 0.9F);

                this.enchantSlots.setChanged();
                this.slotsChanged(this.enchantSlots);
                return;
            }
        });
        return true;
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

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY; // TODO: implement proper quick-move if desired
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