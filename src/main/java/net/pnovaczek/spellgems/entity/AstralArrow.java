package net.pnovaczek.spellgems.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
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

public class AstralArrow extends AbstractArrow {

    private @Nullable PotionEnchantment potionEnchantment;
    private final Set<UUID> potionAppliedEntities = new HashSet<>();
    private boolean potionAppliedOnBlock;

    public AstralArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    public AstralArrow(Level level, LivingEntity shooter) {
        super(ModEntities.ASTRAL_ARROW, shooter, level, ItemStack.EMPTY, null);
    }

    public AstralArrow(Level level, LivingEntity shooter, ItemStack firedFromWeapon) {
        super(ModEntities.ASTRAL_ARROW, shooter, level, ItemStack.EMPTY, firedFromWeapon);
    }

    public void setPotionEnchantment(@Nullable PotionEnchantment potionEnchantment) {
        this.potionEnchantment = potionEnchantment;
        if (potionEnchantment != null) {
            this.setPickupItemStack(potionEnchantment.toItemStack());
        }
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

        if (this.inGroundTime >= 100) {
            this.discard();
        }
    }
}