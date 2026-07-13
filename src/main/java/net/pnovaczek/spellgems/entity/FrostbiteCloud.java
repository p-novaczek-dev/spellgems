package net.pnovaczek.spellgems.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.pnovaczek.spellgems.ModEntities;

public class FrostbiteCloud extends SpellAreaEffectCloud {

    /**
     * Required for entity registration in ModEntities.
     */
    @SuppressWarnings("this-escape")
    public FrostbiteCloud(EntityType<? extends FrostbiteCloud> entityType, Level level) {
        super(entityType, level);
        this.setRadius(CLOUD_RADIUS);
        this.setDuration(CLOUD_DURATION);
        this.setCustomParticle(ParticleTypes.SNOWFLAKE);
    }

    /**
     * Convenience constructor used when spawning from strike logic.
     */
    @SuppressWarnings("this-escape")
    public FrostbiteCloud(Level level, double x, double y, double z, LivingEntity owner) {
        this(ModEntities.FROSTBITE_CLOUD, level);
        this.setPos(x, y, z);
        this.setOwner(owner);
    }

    @Override
    protected void applyEffectToTarget(LivingEntity target) {
        target.setTicksFrozen(EFFECT_DURATION);
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, EFFECT_DURATION, 0));
    }
}
