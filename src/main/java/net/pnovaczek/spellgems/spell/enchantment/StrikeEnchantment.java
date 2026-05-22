package net.pnovaczek.spellgems.spell.enchantment;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.pnovaczek.spellgems.Spellgems;

public record StrikeEnchantment(Identifier id) {

    public static final Codec<StrikeEnchantment> CODEC = Identifier.CODEC.xmap(
            StrikeEnchantment::new,
            StrikeEnchantment::id
    );

    public void applyTo(LivingEntity living) {
        var duration = Spellgems.CONFIG.strikeEffectDuration;
        if (is(StrikeEnchantments.POISON)) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, duration, 0));
        } else if (is(StrikeEnchantments.FLAME)) {
            living.setRemainingFireTicks(duration);
        } else if (is(StrikeEnchantments.FROST)) {
            living.setTicksFrozen(duration);
        }
    }

    public boolean is(Identifier strikeId) {
        return id.equals(strikeId);
    }

    public String tooltipNameKey() {
        return "tooltip.spellgems.spell_enchantment." + id.getPath() + ".name";
    }

    public String tooltipDescriptionKey() {
        return "tooltip.spellgems.spell_enchantment." + id.getPath() + ".description";
    }

    public void addParticle(Level level, double x, double y, double z, RandomSource random) {
        var randomSpread = 0.1D;
        SimpleParticleType particleType = ParticleTypes.CRIT;

        if (is(StrikeEnchantments.FROST)) {
            particleType = ParticleTypes.SNOWFLAKE;
            randomSpread = 0.4D;
        }
        else if (is(StrikeEnchantments.FLAME)) {
            particleType = ParticleTypes.FLAME;
            randomSpread = 0.6D;
        }

        level.addParticle(
                particleType,
                x + (random.nextDouble() - 0.5) * randomSpread,
                y + (random.nextDouble() - 0.5) * randomSpread,
                z + (random.nextDouble() - 0.5) * randomSpread,
                0.0, 0.0, 0.0
        );
    }

    public Integer getTintColor() {
        if (is(StrikeEnchantments.FLAME)) return 0xFF3300;
        if (is(StrikeEnchantments.FROST)) return 0xEEEEFF;
        return 0xCCCCCC;
    }
}