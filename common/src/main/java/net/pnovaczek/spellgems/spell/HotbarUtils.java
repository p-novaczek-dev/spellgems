package net.pnovaczek.spellgems.spell;

import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.CropBlock;
import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Utilities for inspecting and selecting items from a player hotbar or arbitrary container
 * using weighted random selection (weighted by stack size).
 */
public final class HotbarUtils {

    private HotbarUtils() {
    }

    /** Returns true if any item in the player's hotbar matches the predicate. */
    public static boolean hasItem(Player player, Predicate<ItemStack> predicate) {
        return hasItem(player.getInventory(), 0, Inventory.getSelectionSize(), predicate);
    }

    /** Returns true if any item in the container matches the predicate. */
    public static boolean hasItem(Container container, Predicate<ItemStack> predicate) {
        return hasItem(container, 0, container.getContainerSize(), predicate);
    }

    public static boolean hasItem(Container container, int startSlot, int endSlotExclusive, Predicate<ItemStack> predicate) {
        for (int slot = startSlot; slot < endSlotExclusive; slot++) {
            if (predicate.test(container.getItem(slot))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Picks a random matching item from the player's hotbar, probability proportional to stack count.
     * Returns the actual ItemStack (so the caller can shrink it).
     */
    public static @Nullable ItemStack pickWeighted(Player player, RandomSource random, Predicate<ItemStack> predicate) {
        return pickWeighted(player.getInventory(), 0, Inventory.getSelectionSize(), random, predicate);
    }

    /**
     * Picks a random matching item from the full container, probability proportional to stack count.
     */
    public static @Nullable ItemStack pickWeighted(Container container, RandomSource random, Predicate<ItemStack> predicate) {
        return pickWeighted(container, 0, container.getContainerSize(), random, predicate);
    }

    public static @Nullable ItemStack pickWeighted(
            Container container,
            int startSlot,
            int endSlotExclusive,
            RandomSource random,
            Predicate<ItemStack> predicate
    ) {
        int totalCount = 0;
        for (int slot = startSlot; slot < endSlotExclusive; slot++) {
            ItemStack stack = container.getItem(slot);
            if (predicate.test(stack)) {
                totalCount += stack.getCount();
            }
        }

        if (totalCount <= 0) {
            return null;
        }

        int roll = random.nextInt(totalCount);
        for (int slot = startSlot; slot < endSlotExclusive; slot++) {
            ItemStack stack = container.getItem(slot);
            if (predicate.test(stack)) {
                roll -= stack.getCount();
                if (roll < 0) {
                    return stack;
                }
            }
        }

        return null;
    }

    /**
     * Resolves items from a {@link SpellContext}: explicit machine inventory (all slots),
     * or player hotbar for hand/wand casts.
     */
    public static boolean hasItem(SpellContext context, Predicate<ItemStack> predicate) {
        Container container = context.resolveItemSource();
        if (container == null) {
            return false;
        }
        if (context.useHotbarOnly()) {
            return hasItem(container, 0, Inventory.getSelectionSize(), predicate);
        }
        return hasItem(container, predicate);
    }

    public static @Nullable ItemStack pickWeighted(SpellContext context, RandomSource random, Predicate<ItemStack> predicate) {
        Container container = context.resolveItemSource();
        if (container == null) {
            return null;
        }
        if (context.useHotbarOnly()) {
            return pickWeighted(container, 0, Inventory.getSelectionSize(), random, predicate);
        }
        return pickWeighted(container, random, predicate);
    }

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
