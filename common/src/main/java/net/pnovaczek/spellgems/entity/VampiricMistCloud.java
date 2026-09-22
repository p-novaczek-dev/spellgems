package net.pnovaczek.spellgems.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.pnovaczek.spellgems.ModEntities;
import net.pnovaczek.spellgems.particle.ModParticles;
import net.pnovaczek.spellgems.Spellgems;

public class VampiricMistCloud extends SpellAreaEffectCloud {

    /**
     * Required for entity registration in ModEntities.
     */
    @SuppressWarnings("this-escape")
    public VampiricMistCloud(EntityType<? extends VampiricMistCloud> entityType, Level level) {
        super(entityType, level);
        this.setRadius(CLOUD_RADIUS);
        this.setDuration(CLOUD_DURATION);
        this.setCustomParticle(ModParticles.RAID_OMEN_MOTE);
    }

    /**
     * Convenience constructor used when spawning from strike logic.
     */
    @SuppressWarnings("this-escape")
    public VampiricMistCloud(Level level, double x, double y, double z, LivingEntity owner) {
        this(ModEntities.VAMPIRIC_MIST_CLOUD, level);
        this.setPos(x, y, z);
        this.setOwner(owner);
    }

    @Override
    public void tick() {
        super.tick();
        int interval = Math.max(1, Spellgems.CONFIG.vampiricMistIntervalTicks);
        if (this.level() instanceof ServerLevel && !this.isWaiting()
                && this.tickCount > 0 && this.tickCount % interval == 0) {
            pulse();
        }
    }

    @Override
    protected void applyInitialDamage() {
        pulse();
    }

    @Override
    protected void applyEffectToTarget(LivingEntity target) {
        // Periodic damage and healing are handled by pulse(), not the 5-tick status cycle.
    }

    private void pulse() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        float damage = Spellgems.CONFIG.vampiricMistDamage;
        if (damage <= 0.0F) {
            return;
        }

        float radius = this.getRadius();
        LivingEntity owner = this.getOwner();
        float heal = Spellgems.CONFIG.vampiricMistHealPerTarget;
        DamageSource source = (owner != null)
                ? this.damageSources().indirectMagic(this, owner)
                : this.damageSources().magic();

        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox())) {
            if (shouldSkipTarget(target, owner)) {
                continue;
            }
            double dx = target.getX() - this.getX();
            double dz = target.getZ() - this.getZ();
            if (dx * dx + dz * dz > radius * radius) {
                continue;
            }
            if (target.hurtServer(serverLevel, source, damage) && owner != null && heal > 0.0F) {
                owner.heal(heal);
            }
        }
    }
}
