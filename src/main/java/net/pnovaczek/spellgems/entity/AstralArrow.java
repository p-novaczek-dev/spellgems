package net.pnovaczek.spellgems.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.pnovaczek.spellgems.ModEntities;
import org.jspecify.annotations.NonNull;

public class AstralArrow extends Arrow {

    public AstralArrow(EntityType<? extends Arrow> entityType, Level level) {
        super(entityType, level);
    }

    public AstralArrow(Level level, LivingEntity shooter) {
        super(ModEntities.ASTRAL_ARROW, level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ());
    }

    @Override
    protected @NonNull ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.inGroundTime >= 100) {
            this.discard();
        }
    }

    // Future-proof hooks for potion gems (will be used later)
    // @Override
    // protected void onHitEntity(...) { ... }
}