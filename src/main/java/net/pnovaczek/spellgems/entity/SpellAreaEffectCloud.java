package net.pnovaczek.spellgems.entity;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public abstract class SpellAreaEffectCloud extends AreaEffectCloud {

    public SpellAreaEffectCloud(EntityType<? extends SpellAreaEffectCloud> entityType, Level level) {
        super(entityType, level);
        this.setWaitTime(0);
        this.setRadiusPerTick(0.0F);
    }

    /**
     * Convenience constructor used when spawning from strike/utility spell logic.
     */
    public SpellAreaEffectCloud(EntityType<? extends SpellAreaEffectCloud> entityType, Level level,
                                double x, double y, double z, LivingEntity owner,
                                float radius, int durationTicks, ParticleOptions particle) {
        this(entityType, level);
        this.setPos(x, y, z);
        this.setOwner(owner);
        this.setRadius(radius);
        this.setDuration(durationTicks);
        if (particle != null) {
            this.setCustomParticle(particle);
        }
    }

    @Override
    public void tick() {
        super.tick();

        // Matches vanilla AreaEffectCloud's 5-tick application cycle
        if (this.level() instanceof ServerLevel && this.tickCount % 5 == 0 && !this.isWaiting()) {
            applyEffectsToTargets();
        }
    }

    protected void applyEffectsToTargets() {
        float radius = this.getRadius();
        LivingEntity owner = this.getOwner();

        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox())) {
            if (shouldSkipTarget(target, owner)) {
                continue;
            }

            // Precise radius check (vanilla cloud does similar)
            double dx = target.getX() - this.getX();
            double dz = target.getZ() - this.getZ();
            if (dx * dx + dz * dz > radius * radius) {
                continue;
            }

            applyEffectToTarget(target);
        }
    }

    protected boolean shouldSkipTarget(LivingEntity target, LivingEntity owner) {
        return target == owner || target.isSpectator() || !target.isAlive();
    }

    /**
     * Override in subclasses (e.g. InfernoCloud) to apply spell-specific effects.
     * Called once per target per application tick.
     */
    protected abstract void applyEffectToTarget(LivingEntity target);
}