package net.pnovaczek.spellgems.enchantment.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

public record RechargeEffect() implements EnchantmentEntityEffect {

    public static final MapCodec<RechargeEffect> CODEC = MapCodec.unit(RechargeEffect::new);

    @Override
    public void apply(ServerLevel serverLevel, int level, EnchantedItemInUse context, Entity target, Vec3 pos) {

        ItemStack stack = context.itemStack();
        if (stack.isEmpty()) {
            return;
        }

        // Passively repair 1 durability every 20 ticks (1 second) while the enchanted item is active.
        // Logic inlined from the previous RechargeHelper implementation.
        if (serverLevel.getGameTime() % 20 == 0) {
            int currentDamage = stack.getDamageValue();
            if (currentDamage > 0) {
                stack.setDamageValue(Math.max(0, currentDamage - 1));
            }
        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> codec() {
        return CODEC;
    }
}
