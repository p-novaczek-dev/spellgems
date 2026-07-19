package net.pnovaczek.spellgems.entity;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.pnovaczek.spellgems.ModEntities;
import net.pnovaczek.spellgems.spell.PotionDelivery;
import net.pnovaczek.spellgems.spell.enchantment.PotionEnchantment;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Magical bow ammo. Never drops or can be picked up as an item (including potion-tipped shots).
 * Potion-tipped shots spawn {@link ParticleTypes#ENTITY_EFFECT} trail particles like vanilla tipped arrows,
 * colored via {@link PotionContents#getColor()} (supports custom colors and modded effects).
 */
public class AstralArrow extends AbstractArrow {

    /** Synced RGB color; {@link #NO_EFFECT_COLOR} means no potion trail. */
    private static final EntityDataAccessor<Integer> ID_EFFECT_COLOR =
            SynchedEntityData.defineId(AstralArrow.class, EntityDataSerializers.INT);
    private static final int NO_EFFECT_COLOR = -1;

    private @Nullable PotionEnchantment potionEnchantment;
    private final Set<UUID> potionAppliedEntities = new HashSet<>();
    private boolean potionAppliedOnBlock;

    public AstralArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        disallowPickup();
    }

    public AstralArrow(Level level, LivingEntity shooter) {
        super(ModEntities.ASTRAL_ARROW, shooter, level, ItemStack.EMPTY, null);
        // setOwner(Player) would flip pickup to ALLOWED — force intangible ammo.
        disallowPickup();
    }

    public AstralArrow(Level level, LivingEntity shooter, ItemStack firedFromWeapon) {
        super(ModEntities.ASTRAL_ARROW, shooter, level, ItemStack.EMPTY, firedFromWeapon);
        disallowPickup();
    }

    public void setPotionEnchantment(@Nullable PotionEnchantment potionEnchantment) {
        this.potionEnchantment = potionEnchantment;
        // Do not setPickupItemStack to a potion — that made ground pickup yield the potion item.
        disallowPickup();
        updateEffectColor();
    }

    private void disallowPickup() {
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    private void updateEffectColor() {
        if (this.potionEnchantment == null || !this.potionEnchantment.contents().hasEffects()) {
            this.entityData.set(ID_EFFECT_COLOR, NO_EFFECT_COLOR);
            return;
        }
        // PotionContents.getColor() uses custom_color if present, otherwise blends visible
        // MobEffect colors — works for vanilla and most modded potions.
        this.entityData.set(ID_EFFECT_COLOR, this.potionEnchantment.contents().getColor());
    }

    public int getEffectColor() {
        return this.entityData.get(ID_EFFECT_COLOR);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ID_EFFECT_COLOR, NO_EFFECT_COLOR);
    }

    @Override
    public void setOwner(@Nullable Entity owner) {
        super.setOwner(owner);
        // AbstractArrow promotes DISALLOWED → ALLOWED when owner is a Player.
        disallowPickup();
    }

    @Override
    protected boolean tryPickup(Player player) {
        return false;
    }

    @Override
    protected void doPostHurtEffects(LivingEntity mob) {
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        applyPotionOnHit(result.getLocation(), result.getEntity() instanceof LivingEntity living ? living : null);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        applyPotionOnHit(result.getLocation(), null);
    }

    private void applyPotionOnHit(Vec3 hitPos, @Nullable LivingEntity hitEntity) {
        if (this.potionEnchantment == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity shooter = this.getOwner() instanceof LivingEntity living ? living : null;
        if (shooter == null) {
            return;
        }

        if (hitEntity != null) {
            if (!this.potionAppliedEntities.add(hitEntity.getUUID())) {
                return;
            }
            PotionDelivery.applyOnEntityHit(serverLevel, shooter, hitEntity, hitPos, this.potionEnchantment);
        } else {
            if (this.potionAppliedOnBlock) {
                return;
            }
            this.potionAppliedOnBlock = true;
            PotionDelivery.applyOnBlockHit(serverLevel, shooter, hitPos, this.potionEnchantment);
        }
    }

    @Override
    public void tick() {
        super.tick();

        // Match vanilla tipped-arrow trails (Arrow.tick / makeParticle).
        if (this.level().isClientSide() && getEffectColor() != NO_EFFECT_COLOR) {
            if (this.isInGround()) {
                if (this.inGroundTime % 5 == 0) {
                    makeParticle(1);
                }
            } else {
                makeParticle(2);
            }
        }

        if (this.inGroundTime >= 100) {
            this.discard();
        }
    }

    private void makeParticle(int amount) {
        int colorValue = getEffectColor();
        if (colorValue == NO_EFFECT_COLOR || amount <= 0) {
            return;
        }
        for (int i = 0; i < amount; i++) {
            this.level().addParticle(
                    ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, colorValue),
                    this.getRandomX(0.5),
                    this.getRandomY(),
                    this.getRandomZ(0.5),
                    0.0,
                    0.0,
                    0.0
            );
        }
    }
}
