package net.pnovaczek.spellgems.spell;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class HotbarBlocks {

    private HotbarBlocks() {
    }

    public static boolean hasPlaceableBlock(Player player) {
        for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
            if (isPlaceableBlock(player.getInventory().getItem(slot))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPlaceableBlock(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof BlockItem;
    }

    public static @Nullable ItemStack pickWeightedPlaceableBlock(Player player, RandomSource random) {
        int totalCount = 0;
        for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (isPlaceableBlock(stack)) {
                totalCount += stack.getCount();
            }
        }

        if (totalCount <= 0) {
            return null;
        }

        int roll = random.nextInt(totalCount);
        for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (isPlaceableBlock(stack)) {
                roll -= stack.getCount();
                if (roll < 0) {
                    return stack;
                }
            }
        }

        return null;
    }
}