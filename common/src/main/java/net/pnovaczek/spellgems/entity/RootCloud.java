package net.pnovaczek.spellgems.entity;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.pnovaczek.spellgems.ModEntities;

public class RootCloud extends SpellAreaEffectCloud {

    private static final int ROOT_SLOWNESS_AMPLIFIER = 4;

    /**
     * Required for entity registration in ModEntities.
     */
    @SuppressWarnings("this-escape")
    public RootCloud(EntityType<? extends RootCloud> entityType, Level level) {
        super(entityType, level);
        this.setRadius(CLOUD_RADIUS);
        this.setDuration(CLOUD_DURATION);
        this.setCustomParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFF484D48));
    }

    /**
     * Convenience constructor used when spawning from strike logic.
     */
    @SuppressWarnings("this-escape")
    public RootCloud(Level level, double x, double y, double z, LivingEntity owner) {
        this(ModEntities.ROOT_CLOUD, level);
        this.setPos(x, y, z);
        this.setOwner(owner);
    }

    @Override
    protected void applyEffectToTarget(LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, EFFECT_DURATION, ROOT_SLOWNESS_AMPLIFIER));
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, EFFECT_DURATION, 0));
    }
}
