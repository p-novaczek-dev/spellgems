package net.pnovaczek.spellgems.spell;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public final class HotbarBoneMeal {

    private HotbarBoneMeal() {
    }

    public static boolean hasBoneMeal(Player player) {
        for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
            if (isBoneMeal(player.getInventory().getItem(slot))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBoneMeal(ItemStack stack) {
        return !stack.isEmpty() && stack.is(Items.BONE_MEAL);
    }

    public static @Nullable ItemStack pickWeightedBoneMeal(Player player, RandomSource random) {
        int totalCount = 0;
        for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (isBoneMeal(stack)) {
                totalCount += stack.getCount();
            }
        }

        if (totalCount <= 0) {
            return null;
        }

        int roll = random.nextInt(totalCount);
        for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (isBoneMeal(stack)) {
                roll -= stack.getCount();
                if (roll < 0) {
                    return stack;
                }
            }
        }

        return null;
    }
}