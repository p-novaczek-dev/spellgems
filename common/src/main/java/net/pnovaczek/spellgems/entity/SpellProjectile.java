package net.pnovaczek.spellgems.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.pnovaczek.spellgems.ModEntities;
import net.pnovaczek.spellgems.ModEntityDataSerializers;
import net.pnovaczek.spellgems.item.data.SpellGemData;
import net.pnovaczek.spellgems.spell.ProjectileHitHandler;
import net.pnovaczek.spellgems.spell.SpellContext;
import net.pnovaczek.spellgems.spell.SpellIds;
import net.pnovaczek.spellgems.spell.enchantment.ModifierEnchantments;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SpellProjectile extends AbstractHurtingProjectile {

    public static final int TRANSMUTE_TINT = 0xFFB6FF;
    private static final double COMBAT_SPEED = 1.8;
    private static final double TRANSMUTE_SPEED = 0.9;
    private static final double COMBAT_ACCELERATION = 0.06;
    private static final double TRANSMUTE_ACCELERATION = 0.03;
    /**
     * Back from the center of the block in front of the dispenser.
     * 0.5 would be the shared face. Staying under that starts outside the dispenser
     * and inside a full block in front. A point inside a full cube counts as a hit,
     * so starting inside the dispenser destroys the shot on the dispenser itself.
     */
    private static final double DISPENSER_LAUNCH_BACKSTEP = 0.49;

    private static final EntityDataSerializer<CompoundTag> SPELL_GEM_SERIALIZER =
            ModEntityDataSerializers.SPELL_GEM_SERIALIZER;

    private static final EntityDataAccessor<CompoundTag> DATA_SPELL_GEM =
            SynchedEntityData.defineId(SpellProjectile.class, SPELL_GEM_SERIALIZER);

    private static final EntityDataAccessor<Integer> DATA_TINT_COLOR =
            SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.INT);

    private final SpellContext spellContext;
    private final ProjectileHitHandler hitHandler;
    private final boolean piercing;
    /** Entity just struck by a parent projectile; split children must not immediately re-hit it. */
    private @Nullable UUID ignoredHitEntity;
    private final Set<UUID> piercedEntities = new HashSet<>();

    public SpellProjectile(EntityType<? extends SpellProjectile> entityType, Level level) {
        super(entityType, level);
        this.spellContext = null;
        this.hitHandler = null;
        this.piercing = false;
    }

    public SpellProjectile(double x, double y, double z, Vec3 direction, Level level) {
        super(ModEntities.SPELL_PROJECTILE, x, y, z, direction, level);
        this.spellContext = null;
        this.hitHandler = null;
        this.piercing = false;
    }

    @SuppressWarnings("this-escape")
    public SpellProjectile(SpellContext spellContext, Vec3 direction, ProjectileHitHandler hitHandler) {
        this.spellContext = spellContext;
        this.hitHandler = hitHandler;
        this.piercing = hasPiercing(spellContext);
        super(ModEntities.SPELL_PROJECTILE, spellContext.level());

        if (spellContext.data() != null) {
            this.entityData.set(DATA_SPELL_GEM, spellContext.data().save(new CompoundTag()));
            this.entityData.set(DATA_TINT_COLOR, tintFor(spellContext.data()));
        }

        setImpulse(direction, launchPosition(spellContext, direction));
    }

    @SuppressWarnings("this-escape")
    public SpellProjectile(SpellContext spellContext, Vec3 direction, Vec3 sourcePos, ProjectileHitHandler hitHandler) {
        this.spellContext = spellContext;
        this.hitHandler = hitHandler;
        this.piercing = hasPiercing(spellContext);
        super(ModEntities.SPELL_PROJECTILE, spellContext.level());

        if (spellContext.data() != null) {
            this.entityData.set(DATA_SPELL_GEM, spellContext.data().save(new CompoundTag()));
            this.entityData.set(DATA_TINT_COLOR, tintFor(spellContext.data()));
        }

        setImpulse(direction, sourcePos);

    }

    /**
     * Dispenser origin is the center of the block in front. Spawn just inside that block,
     * clear of the dispenser. Spawning further along skips it; spawning inside the dispenser hits the dispenser.
     */
    private static Vec3 launchPosition(SpellContext spellContext, Vec3 direction) {
        if (spellContext.isDispenserCast()) {
            return spellContext.origin().subtract(spellContext.lookAngle().scale(DISPENSER_LAUNCH_BACKSTEP));
        }
        Vec3 dir = direction.normalize();
        return spellContext.eyeOrigin().add(0.0, -0.1, 0.0).add(dir.scale(0.6));
    }

    @SuppressWarnings("this-escape")
    private void setImpulse(Vec3 direction, Vec3 sourcePos) {
        if (spellContext.caster() != null) {
            this.setOwner(spellContext.caster());
        }
        this.setPos(sourcePos.x, sourcePos.y, sourcePos.z);
        boolean transmute = isTransmute(spellContext);
        this.setDeltaMovement(direction.normalize().scale(transmute ? TRANSMUTE_SPEED : COMBAT_SPEED));
        this.accelerationPower = transmute ? TRANSMUTE_ACCELERATION : COMBAT_ACCELERATION;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SPELL_GEM, new CompoundTag());
        builder.define(DATA_TINT_COLOR, 0xFFFFFF);
    }

    public void ignoreHitEntity(Entity entity) {
        this.ignoredHitEntity = entity.getUUID();
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (this.ignoredHitEntity != null && this.ignoredHitEntity.equals(entity.getUUID())) {
            return false;
        }
        if (this.piercedEntities.contains(entity.getUUID())) {
            return false;
        }
        return super.canHitEntity(entity);
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

        if (this.piercing) {
            this.piercedEntities.add(result.getEntity().getUUID());
            return;
        }
        this.discard();
    }

    @Override
    protected void onHitBlock(net.minecraft.world.phys.BlockHitResult result) {
        super.onHitBlock(result);
        if (hitHandler != null) {
            hitHandler.onHitBlock(this, result);
        }
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

        if (isTransmute(data)) {
            this.level().addParticle(ParticleTypes.ELECTRIC_SPARK, getX(), getY(), getZ(),
                    (random.nextDouble() - 0.5) * 0.2,
                    (random.nextDouble() - 0.5) * 0.2,
                    (random.nextDouble() - 0.5) * 0.2);
        }

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

        if (isTransmute(data)) {
            for (int i = 0; i < 12; i++) {
                this.level().addParticle(
                        ParticleTypes.ELECTRIC_SPARK,
                        getX(), getY(), getZ(),
                        (random.nextDouble() - 0.5) * 0.4,
                        (random.nextDouble() - 0.5) * 0.4,
                        (random.nextDouble() - 0.5) * 0.4
                );
            }
        }

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

    public boolean isTransmuteProjectile() {
        return isTransmute(loadSpellData());
    }

    private @Nullable SpellGemData loadSpellData() {
        CompoundTag tag = this.entityData.get(DATA_SPELL_GEM);
        if (tag.isEmpty()) {
            return null;
        }
        return SpellGemData.load(tag);
    }

    private static int tintFor(SpellGemData data) {
        if (isTransmute(data)) {
            return TRANSMUTE_TINT;
        }
        return data.getTintColor();
    }

    private static boolean isTransmute(@Nullable SpellContext context) {
        return context != null && isTransmute(context.data());
    }

    private static boolean isTransmute(@Nullable SpellGemData data) {
        return data != null && SpellIds.TRANSMUTE.equals(data.spellId());
    }

    private static boolean hasPiercing(@Nullable SpellContext context) {
        if (context == null || context.data() == null) {
            return false;
        }
        for (var modifier : context.data().modifierEffects()) {
            if (modifier.is(ModifierEnchantments.PIERCING)) {
                return true;
            }
        }
        return false;
    }
}