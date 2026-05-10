package net.pnovaczek.spellgems.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.pnovaczek.spellgems.ModEntities;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.SpellgemsConfig;
import net.pnovaczek.spellgems.spell.SpellContext;
import net.pnovaczek.spellgems.spell.Spells;

public class SpellProjectile extends AbstractHurtingProjectile {

    private SpellContext spellContext;

    public SpellProjectile(EntityType<? extends SpellProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public SpellProjectile(double x, double y, double z, Vec3 direction, Level level) {
        super(ModEntities.SPELL_PROJECTILE, x, y, z, direction, level);
    }

    public SpellProjectile(SpellContext spellContext) {

        this.spellContext = spellContext;
        var level = spellContext.level();
        var caster = spellContext.caster();
        var direction = spellContext.lookAngle();

        super(ModEntities.SPELL_PROJECTILE, level);
        this.setOwner(caster);

        Vec3 eyePos = new Vec3(caster.getX(), caster.getEyeY() - 0.1, caster.getZ());
        Vec3 spawnPos = eyePos.add(direction.normalize().scale(0.6));
        this.setPos(spawnPos.x, spawnPos.y, spawnPos.z);

        this.setDeltaMovement(direction.normalize().scale(1.8));
        this.accelerationPower = 0.06;
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected net.minecraft.core.particles.ParticleOptions getTrailParticle() {
        return null;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (spellContext == null)
            return;

        var data = spellContext.data();
        var spellConfig = (SpellgemsConfig.SpellCombatConfig)Spellgems.CONFIG.spells.getOrDefault(Spells.PROJECTILE.getPath(), new SpellgemsConfig.SpellCombatConfig());

        Entity target = result.getEntity();

        if (target instanceof LivingEntity living && this.level() instanceof ServerLevel serverLevel) {
            DamageSource source = this.damageSources().magic();
            living.hurtServer(serverLevel, source, spellConfig.damage);
            for (var effect : data.strikeEffects())
                effect.applyTo(living);
        }

        this.discard();
    }

    @Override
    protected void onHitBlock(net.minecraft.world.phys.BlockHitResult result) {
        super.onHitBlock(result);
        this.discard(); // no block interaction for now
    }

    @Override
    public void tick() {
        super.tick();

        // add particles/sound here
        if (this.level().isClientSide()) {
            this.level().addParticle(
                ParticleTypes.CRIT,
                this.getX(),
                this.getY(),
                this.getZ(),
                0.0,
                0.0,
                0.0
            );
        }
    }
}