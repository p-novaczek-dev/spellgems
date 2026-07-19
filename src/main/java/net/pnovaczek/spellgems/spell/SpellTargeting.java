package net.pnovaczek.spellgems.spell;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;

public final class SpellTargeting {

    private SpellTargeting() {}

    public static Vec3 resolveCastCenter(LivingEntity caster, double maxDistance) {
        return resolveCastCenter(
                caster.level(),
                caster.getEyePosition(),
                caster.getViewVector(1.0F),
                maxDistance,
                caster,
                entity -> entity != caster && entity.canBeHitByProjectile()
        );
    }

    public static Vec3 resolveCastCenter(SpellContext context, double maxDistance) {
        LivingEntity caster = context.caster();
        Predicate<Entity> filter = caster != null
                ? entity -> entity != caster && entity.canBeHitByProjectile()
                : Entity::canBeHitByProjectile;
        return resolveCastCenter(
                context.level(),
                context.eyeOrigin(),
                context.lookAngle(),
                maxDistance,
                caster,
                filter
        );
    }

    public static Vec3 resolveCastCenter(
            Level level,
            Vec3 origin,
            Vec3 direction,
            double maxDistance,
            @Nullable Entity except,
            Predicate<Entity> entityFilter
    ) {
        Vec3 dir = direction.lengthSqr() < 1.0E-8 ? new Vec3(0.0, 0.0, 1.0) : direction.normalize();
        Vec3 end = origin.add(dir.scale(maxDistance));

        CollisionContext collision = except != null
                ? CollisionContext.of(except)
                : CollisionContext.empty();
        BlockHitResult blockHit = level.clip(new ClipContext(
                origin,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                collision
        ));

        double blockDist = blockHit.getType() != HitResult.Type.MISS
                ? origin.distanceTo(blockHit.getLocation())
                : maxDistance;
        Vec3 entityEnd = origin.add(dir.scale(blockDist));
        AABB searchBox = new AABB(origin, entityEnd).inflate(1.0);

        EntityHitResult entityHit = except != null
                ? ProjectileUtil.getEntityHitResult(level, except, origin, entityEnd, searchBox, entityFilter, 0.3F)
                : findEntityHit(level, origin, entityEnd, searchBox, entityFilter, 0.3F);

        if (entityHit != null) {
            return entityHit.getLocation();
        }
        if (blockHit.getType() != HitResult.Type.MISS) {
            return blockHit.getLocation();
        }
        return origin.add(dir.scale(maxDistance));
    }

    public static @Nullable BlockHitResult resolveBlockHit(LivingEntity caster, double maxDistance) {
        return resolveBlockHit(
                caster.level(),
                caster.getEyePosition(),
                caster.getViewVector(1.0F),
                maxDistance,
                caster
        );
    }

    public static @Nullable BlockHitResult resolveBlockHit(SpellContext context, double maxDistance) {
        return resolveBlockHit(
                context.level(),
                context.eyeOrigin(),
                context.lookAngle(),
                maxDistance,
                context.caster()
        );
    }

    public static @Nullable BlockHitResult resolveBlockHit(
            Level level,
            Vec3 origin,
            Vec3 direction,
            double maxDistance,
            @Nullable Entity except
    ) {
        Vec3 dir = direction.lengthSqr() < 1.0E-8 ? new Vec3(0.0, 0.0, 1.0) : direction.normalize();
        Vec3 end = origin.add(dir.scale(maxDistance));

        CollisionContext collision = except != null
                ? CollisionContext.of(except)
                : CollisionContext.empty();
        BlockHitResult blockHit = level.clip(new ClipContext(
                origin,
                end,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                collision
        ));

        if (blockHit.getType() == HitResult.Type.BLOCK) {
            return blockHit;
        }
        return null;
    }

    private static @Nullable EntityHitResult findEntityHit(
            Level level,
            Vec3 from,
            Vec3 to,
            AABB searchBox,
            Predicate<Entity> matching,
            float margin
    ) {
        double nearest = Double.MAX_VALUE;
        Entity hitEntity = null;
        Vec3 hitPos = null;

        for (Entity entity : level.getEntities((Entity) null, searchBox, matching)) {
            AABB bb = entity.getBoundingBox().inflate(margin);
            var location = bb.clip(from, to);
            if (location.isPresent()) {
                double dd = from.distanceToSqr(location.get());
                if (dd < nearest) {
                    hitEntity = entity;
                    nearest = dd;
                    hitPos = location.get();
                }
            }
        }

        return hitEntity == null ? null : new EntityHitResult(hitEntity, hitPos);
    }
}
