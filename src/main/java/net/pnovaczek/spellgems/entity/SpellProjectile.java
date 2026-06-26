package net.pnovaczek.spellgems.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.pnovaczek.spellgems.ModEntities;
import net.pnovaczek.spellgems.ModEntityDataSerializers;
import net.pnovaczek.spellgems.item.data.SpellGemData;
import net.pnovaczek.spellgems.spell.ProjectileHitHandler;
import net.pnovaczek.spellgems.spell.SpellContext;

public class SpellProjectile extends AbstractHurtingProjectile {

    private static final EntityDataSerializer<CompoundTag> SPELL_GEM_SERIALIZER =
            ModEntityDataSerializers.SPELL_GEM_SERIALIZER;

    private static final EntityDataAccessor<CompoundTag> DATA_SPELL_GEM =
            SynchedEntityData.defineId(SpellProjectile.class, SPELL_GEM_SERIALIZER);

    private static final EntityDataAccessor<Integer> DATA_TINT_COLOR =
            SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.INT);

    private final SpellContext spellContext;
    private final ProjectileHitHandler hitHandler;

    public SpellProjectile(EntityType<? extends SpellProjectile> entityType, Level level) {
        super(entityType, level);
        this.spellContext = null;
        this.hitHandler = null;
    }

    public SpellProjectile(double x, double y, double z, Vec3 direction, Level level) {
        super(ModEntities.SPELL_PROJECTILE, x, y, z, direction, level);
        this.spellContext = null;
        this.hitHandler = null;
    }

    public SpellProjectile(SpellContext spellContext, Vec3 direction, ProjectileHitHandler hitHandler) {
        this.spellContext = spellContext;
        this.hitHandler = hitHandler;
        super(ModEntities.SPELL_PROJECTILE, spellContext.level());

        if (spellContext.data() != null) {
            this.entityData.set(DATA_SPELL_GEM, spellContext.data().save(new CompoundTag()));
            this.entityData.set(DATA_TINT_COLOR, spellContext.data().getTintColor());
        }

        var caster = spellContext.caster();
        Vec3 eyePos = new Vec3(caster.getX(), caster.getEyeY() - 0.1, caster.getZ());
        Vec3 sourcePos = eyePos.add(direction.normalize().scale(0.6));
        setImpulse(direction, sourcePos);
    }

    public SpellProjectile(SpellContext spellContext, Vec3 direction, Vec3 sourcePos, ProjectileHitHandler hitHandler) {
        this.spellContext = spellContext;
        this.hitHandler = hitHandler;
        super(ModEntities.SPELL_PROJECTILE, spellContext.level());

        if (spellContext.data() != null) {
            this.entityData.set(DATA_SPELL_GEM, spellContext.data().save(new CompoundTag()));
            this.entityData.set(DATA_TINT_COLOR, spellContext.data().getTintColor());
        }

        setImpulse(direction, sourcePos);

    }

    private void setImpulse(Vec3 direction, Vec3 sourcePos) {
        var caster = spellContext.caster();
        this.setOwner(caster);
        this.setPos(sourcePos.x, sourcePos.y, sourcePos.z);
        this.setDeltaMovement(direction.normalize().scale(1.8));
        this.accelerationPower = 0.06;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SPELL_GEM, new CompoundTag());
        builder.define(DATA_TINT_COLOR, 0xFFFFFF);
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

        if (hitHandler != null) {
            hitHandler.onHit(this, result);
        }

        this.discard();
    }

    @Override
    protected void onHitBlock(net.minecraft.world.phys.BlockHitResult result) {
        super.onHitBlock(result);
        this.discard();
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            spawnTrailParticles();
        }
    }

    private void spawnTrailParticles() {
        CompoundTag tag = this.entityData.get(DATA_SPELL_GEM);
        if (tag.isEmpty()) return;

        SpellGemData data = SpellGemData.load(tag);

        if (data == null) return;

        for (var strike : data.strikeEffects()) {
            strike.addParticle(level(), getX(), getY(), getZ(), random);
        }
    }

    @Override
    public void onClientRemoval() {
        // Spawn impact particles on the client when the server removes this projectile.
        // This is much more reliable than spawning on the server right before discard().
        spawnImpactParticles();
        super.onClientRemoval();
    }

    private void spawnImpactParticles() {
        if (!this.level().isClientSide()) {
            return;
        }

        CompoundTag tag = this.entityData.get(DATA_SPELL_GEM);
        if (tag.isEmpty()) return;

        SpellGemData data = SpellGemData.load(tag);

        if (data == null) return;

        for (var strike : data.strikeEffects()) {
            // Burst of particles on impact/discard (more intense than trail)
            for (int i = 0; i < 12; i++) {
                double vx = (random.nextDouble() - 0.5) * 0.4;
                double vy = (random.nextDouble() - 0.5) * 0.4;
                double vz = (random.nextDouble() - 0.5) * 0.4;
                strike.addParticle(level(), getX(), getY(), getZ(), random, vx, vy, vz);
            }
        }
    }

    public int getTintColor() {
        return this.entityData.get(DATA_TINT_COLOR);
    }
}