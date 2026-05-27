package net.pnovaczek.spellgems;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.enchantment.effect.RechargeEffect;

public final class ModEnchantmentEffects {

    private ModEnchantmentEffects() {
        // Utility class
    }

    public static void register() {
        Registry.register(
                BuiltInRegistries.ENCHANTMENT_ENTITY_EFFECT_TYPE,
                Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "recharge"),
                RechargeEffect.CODEC
        );
    }
}
