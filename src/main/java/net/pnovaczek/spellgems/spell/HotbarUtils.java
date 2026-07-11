package net.pnovaczek.spellgems.spell;

import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.CropBlock;
import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Utility for inspecting and selecting items from the player's hotbar (first 9 slots)
 * using weighted random selection (weighted by stack size).
 */
public final class HotbarUtils {

    private HotbarUtils() {
    }

    /** Returns true if any item in the hotbar matches the predicate. */
    public static boolean hasItem(Player player, Predicate<ItemStack> predicate) {
        for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
            if (predicate.test(player.getInventory().getItem(slot))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Picks a random matching item from the hotbar, with probability proportional to stack count.
     * Returns the actual ItemStack (so the caller can shrink it).
     */
    public static @Nullable ItemStack pickWeighted(Player player, RandomSource random, Predicate<ItemStack> predicate) {
        int totalCount = 0;
        for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (predicate.test(stack)) {
                totalCount += stack.getCount();
            }
        }

        if (totalCount <= 0) {
            return null;
        }

        int roll = random.nextInt(totalCount);
        for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (predicate.test(stack)) {
                roll -= stack.getCount();
                if (roll < 0) {
                    return stack;
                }
            }
        }

        return null;
    }

    // ---------------------------------------------------------------------
    // Specific item-type helpers (moved from the old Hotbar* classes)
    // ---------------------------------------------------------------------

    public static boolean isPlantableSeed(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        if (stack.is(ItemTags.VILLAGER_PLANTABLE_SEEDS)) {
            return true;
        }
        return blockItem.getBlock() instanceof CropBlock;
    }

    public static boolean isPlaceableBlock(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof BlockItem;
    }

    public static boolean isBoneMeal(ItemStack stack) {
        return !stack.isEmpty() && stack.is(Items.BONE_MEAL);
    }
}