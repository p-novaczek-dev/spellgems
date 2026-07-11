package net.pnovaczek.spellgems.spell;

import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CropBlock;
import org.jspecify.annotations.Nullable;

public final class HotbarSeeds {

    private HotbarSeeds() {
    }

    public static boolean hasPlantableSeed(Player player) {
        for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
            if (isPlantableSeed(player.getInventory().getItem(slot))) {
                return true;
            }
        }
        return false;
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

    public static @Nullable ItemStack pickWeightedPlantableSeed(Player player, RandomSource random) {
        int totalCount = 0;
        for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (isPlantableSeed(stack)) {
                totalCount += stack.getCount();
            }
        }

        if (totalCount <= 0) {
            return null;
        }

        int roll = random.nextInt(totalCount);
        for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (isPlantableSeed(stack)) {
                roll -= stack.getCount();
                if (roll < 0) {
                    return stack;
                }
            }
        }

        return null;
    }
}