package net.pnovaczek.spellgems.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

public final class RechargeHelper {

    private RechargeHelper() {
        // Utility class
    }

    public static void tryRecharge(ItemStack stack, ServerLevel level) {
        if (level.isClientSide()) {
            return;
        }

        int currentDamage = stack.getDamageValue();
        if (currentDamage <= 0) {
            return;
        }

        if (level.getGameTime() % 20 == 0) {
            int repairAmount = 1;
            stack.setDamageValue(Math.max(0, currentDamage - repairAmount));
        }
    }
}