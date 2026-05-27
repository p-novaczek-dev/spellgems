package net.pnovaczek.spellgems.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.pnovaczek.spellgems.ModEntities;

public class InfernoCloud extends SpellAreaEffectCloud {

    /**
     * Required for entity registration in ModEntities.
     */
    public InfernoCloud(EntityType<? extends InfernoCloud> entityType, Level level) {
        super(entityType, level);
        this.setRadius(4.0F);
        this.setDuration(80);           // ~4 seconds
        this.setCustomParticle(ParticleTypes.FLAME);
    }

    /**
     * Convenience constructor used when spawning from spell logic.
     */
    public InfernoCloud(Level level, double x, double y, double z, LivingEntity owner) {
        this(ModEntities.INFERNO_CLOUD, level);
        this.setPos(x, y, z);
        this.setOwner(owner);
    }

    @Override
    protected void applyEffectToTarget(LivingEntity target) {
        target.setRemainingFireTicks(800);
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 140, 0));
    }
}