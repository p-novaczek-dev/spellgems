package net.pnovaczek.spellgems.entity;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.pnovaczek.spellgems.Spellgems;

public abstract class SpellAreaEffectCloud extends AreaEffectCloud {

    protected static float CLOUD_RADIUS = 4.0F;
    protected static int CLOUD_DURATION = 40;
    protected static int EFFECT_DURATION = Spellgems.CONFIG.strikeEffectDuration;
    protected static float CLOUD_DAMAGE = Spellgems.CONFIG.strikeCloudDamage;

    @SuppressWarnings("this-escape")
    public SpellAreaEffectCloud(EntityType<? extends SpellAreaEffectCloud> entityType, Level level) {
        super(entityType, level);
        this.setWaitTime(0);
        this.setRadiusPerTick(0.0F);
    }

    /**
     * Convenience constructor used when spawning from strike/utility spell logic.
     */
    @SuppressWarnings("this-escape")
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
        boolean isFirstTick = this.firstTick;
        super.tick();

        if (isFirstTick && this.level() instanceof ServerLevel) {
            applyInitialDamage();
        }

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

    /**
     * One-time damage applied to all valid targets in the cloud's area exactly once when the cloud
     * is first spawned (on its first tick). Subsequent entries into the cloud receive no damage
     * from this, but continue to receive the subclass-specific effects via applyEffectsToTargets.
     */
    protected void applyInitialDamage() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        float radius = this.getRadius();
        LivingEntity owner = this.getOwner();
        float damage = CLOUD_DAMAGE;
        if (damage <= 0.0F) return;

        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox())) {
            if (shouldSkipTarget(target, owner)) {
                continue;
            }

            // Precise radius check (same as applyEffectsToTargets)
            double dx = target.getX() - this.getX();
            double dz = target.getZ() - this.getZ();
            if (dx * dx + dz * dz > radius * radius) {
                continue;
            }

            DamageSource source = (owner != null)
                    ? this.damageSources().indirectMagic(this, owner)
                    : this.damageSources().magic();
            target.hurtServer(serverLevel, source, damage);
        }
    }
}