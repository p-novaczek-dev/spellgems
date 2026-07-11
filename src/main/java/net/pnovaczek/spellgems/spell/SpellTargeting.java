package net.pnovaczek.spellgems.spell;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class SpellTargeting {

    private SpellTargeting() {}

    public static Vec3 resolveCastCenter(LivingEntity caster, double maxDistance) {
        HitResult hit = ProjectileUtil.getHitResultOnViewVector(
                caster,
                entity -> entity != caster && entity.canBeHitByProjectile(),
                maxDistance
        );

        if (hit.getType() == HitResult.Type.MISS) {
            Vec3 eye = caster.getEyePosition();
            return eye.add(caster.getViewVector(1.0F).scale(maxDistance));
        }

        return hit.getLocation();
    }

    public static @Nullable BlockHitResult resolveBlockHit(LivingEntity caster, double maxDistance) {
        HitResult hit = ProjectileUtil.getHitResultOnViewVector(
                caster,
                entity -> false,
                maxDistance
        );

        if (hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult blockHit) {
            return blockHit;
        }

        return null;
    }
}