package net.pnovaczek.spellgems.spell.enchantment;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Behavior for a single strike enchantment (apply, tint, particles).
 * Registered by id in {@link StrikeEffects}.
 */
public interface StrikeEffect {

    void apply(LivingEntity target, LivingEntity caster);

    int tintColor();

    void addParticle(
            Level level,
            @Nullable Entity exceptViewer,
            double x,
            double y,
            double z,
            RandomSource random,
            double dx,
            double dy,
            double dz
    );
}
