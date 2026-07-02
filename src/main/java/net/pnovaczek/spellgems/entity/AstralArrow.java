package net.pnovaczek.spellgems.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.pnovaczek.spellgems.ModEntities;
import net.pnovaczek.spellgems.spell.PotionDelivery;
import net.pnovaczek.spellgems.spell.enchantment.PotionEnchantment;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class AstralArrow extends Arrow {

    private @Nullable PotionEnchantment potionEnchantment;
    private boolean potionApplied;

    public AstralArrow(EntityType<? extends Arrow> entityType, Level level) {
        super(entityType, level);
    }

    public AstralArrow(Level level, LivingEntity shooter) {
        super(ModEntities.ASTRAL_ARROW, level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ());
    }

    public void setPotionEnchantment(@Nullable PotionEnchantment potionEnchantment) {
        this.potionEnchantment = potionEnchantment;
    }

    @Override
    protected @NonNull ItemStack getPickupItem() {
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
        if (this.potionApplied
                || this.potionEnchantment == null
                || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity shooter = this.getOwner() instanceof LivingEntity living ? living : null;
        if (shooter == null) {
            return;
        }

        this.potionApplied = true;

        if (hitEntity != null) {
            PotionDelivery.applyOnEntityHit(serverLevel, shooter, hitEntity, hitPos, this.potionEnchantment);
        } else {
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