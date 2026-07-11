package net.pnovaczek.spellgems.spell;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class HotbarFeeds {

    private HotbarFeeds() {
    }

    public static @Nullable ItemStack pickWeightedFoodFor(Player player, Animal animal, RandomSource random) {
        int totalCount = 0;
        for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && animal.isFood(stack)) {
                totalCount += stack.getCount();
            }
        }

        if (totalCount <= 0) {
            return null;
        }

        int roll = random.nextInt(totalCount);
        for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && animal.isFood(stack)) {
                roll -= stack.getCount();
                if (roll < 0) {
                    return stack;
                }
            }
        }

        return null;
    }
}