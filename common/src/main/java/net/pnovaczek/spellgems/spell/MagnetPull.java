package net.pnovaczek.spellgems.spell;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Shared item and experience pull used by the magnet spell and the magnet affix.
 */
public final class MagnetPull {

    public static final double SPEED = 0.65;

    private MagnetPull() {
    }

    /**
     * Living casters pull toward mid eye height. Dispenser casts pull toward the cast origin.
     */
    public static Vec3 target(SpellContext context) {
        if (context.caster() != null && !context.isDispenserCast()) {
            var caster = context.caster();
            return caster.position().add(0.0, caster.getEyeHeight() * 0.5, 0.0);
        }
        return context.origin();
    }

    public static boolean pullInRange(
            ServerLevel level,
            Vec3 pullTarget,
            double range,
            @Nullable Player player,
            boolean setPlayerTarget
    ) {
        AABB box = new AABB(pullTarget, pullTarget).inflate(range);
        double rangeSqr = range * range;
        boolean pulled = false;
        List<ItemEntity> items = level.getEntitiesOfClass(
                ItemEntity.class,
                box,
                item -> item.isAlive() && !item.getItem().isEmpty() && item.position().distanceToSqr(pullTarget) <= rangeSqr
        );
        for (ItemEntity item : items) {
            if (pullItem(item, pullTarget, player, setPlayerTarget)) {
                pulled = true;
            }
        }
        List<ExperienceOrb> orbs = level.getEntitiesOfClass(
                ExperienceOrb.class,
                box,
                orb -> orb.isAlive() && orb.position().distanceToSqr(pullTarget) <= rangeSqr
        );
        for (ExperienceOrb orb : orbs) {
            if (pullToward(orb, pullTarget)) {
                pulled = true;
            }
        }
        return pulled;
    }

    /**
     * Fresh drops sit on the corpse for a tick. Pull those toward the caster or dispenser.
     */
    public static void pullFresh(
            ServerLevel level,
            Vec3 corpse,
            Vec3 pullTarget,
            @Nullable Player player,
            boolean setPlayerTarget
    ) {
        double radius = 2.0;
        AABB box = new AABB(corpse, corpse).inflate(radius);
        for (ItemEntity item : level.getEntitiesOfClass(
                ItemEntity.class,
                box,
                entity -> entity.isAlive() && entity.tickCount == 0 && !entity.getItem().isEmpty()
        )) {
            pullItem(item, pullTarget, player, setPlayerTarget);
        }
        for (ExperienceOrb orb : level.getEntitiesOfClass(
                ExperienceOrb.class,
                box,
                entity -> entity.isAlive() && entity.tickCount == 0
        )) {
            pullToward(orb, pullTarget);
        }
    }

    private static boolean pullItem(ItemEntity item, Vec3 pullTarget, @Nullable Player player, boolean setPlayerTarget) {
        if (!pullToward(item, pullTarget)) {
            return false;
        }
        if (setPlayerTarget && player != null) {
            item.setTarget(player.getUUID());
            item.setPickUpDelay(0);
        }
        return true;
    }

    private static boolean pullToward(net.minecraft.world.entity.Entity entity, Vec3 pullTarget) {
        Vec3 offset = pullTarget.subtract(entity.position());
        if (offset.lengthSqr() < 1.0E-8) {
            return false;
        }
        entity.setDeltaMovement(offset.normalize().scale(SPEED));
        return true;
    }
}
