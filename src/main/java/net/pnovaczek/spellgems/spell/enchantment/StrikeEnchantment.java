package net.pnovaczek.spellgems.spell.enchantment;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.pnovaczek.spellgems.Spellgems;

public record StrikeEnchantment(Identifier id) {

    public static final Codec<StrikeEnchantment> CODEC = Identifier.CODEC.xmap(
            StrikeEnchantment::new,
            StrikeEnchantment::id
    );

    public void applyTo(LivingEntity living) {
        var duration = Spellgems.CONFIG.strikeEffectDuration;
        if (id.equals(StrikeEnchantments.POISON)) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, duration, 0));
        } else if (id.equals(StrikeEnchantments.FLAME)) {
            living.setRemainingFireTicks(duration);
        } else if (id.equals(StrikeEnchantments.FROST)) {
            living.setTicksFrozen(duration);
        }
    }

    public String tooltipNameKey() {
        return "tooltip.spellgems.spell_enchantment." + id.getPath() + ".name";
    }

    public String tooltipDescriptionKey() {
        return "tooltip.spellgems.spell_enchantment." + id.getPath() + ".description";
    }
}