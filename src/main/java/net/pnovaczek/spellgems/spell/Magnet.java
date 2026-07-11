package net.pnovaczek.spellgems.spell;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.pnovaczek.spellgems.Spellgems;

import java.util.List;

public class Magnet extends AbstractSpell {

    private static final int COOLDOWN_TICKS = 10;
    private static final float PULL_SPEED = 0.65F;
    private static final int PARTICLE_COUNT = 12;

    @Override
    public Identifier id() {
        return Spells.MAGNET;
    }

    @Override
    public void cast(SpellContext context) {
        var level = context.level();
        var caster = context.caster();
        if (!caster.isAlive()) {
            return;
        }

        float range = Spellgems.CONFIG.spells.magnet.range;

        if (level.isClientSide()) {
            spawnParticles(level, caster.position());
            return;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        AABB searchBox = caster.getBoundingBox().inflate(range);
        double rangeSqr = range * range;
        Vec3 pullTarget = caster.position().add(0.0, caster.getEyeHeight() * 0.5, 0.0);
        List<ItemEntity> items = serverLevel.getEntitiesOfClass(
                ItemEntity.class,
                searchBox,
                item -> item.isAlive()
                        && !item.getItem().isEmpty()
                        && item.distanceToSqr(caster) <= rangeSqr
        );

        boolean pulledAny = false;
        for (ItemEntity item : items) {
            Vec3 offset = pullTarget.subtract(item.position());
            if (offset.lengthSqr() < 1.0E-8) {
                continue;
            }

            item.setDeltaMovement(offset.normalize().scale(PULL_SPEED));
            if (caster instanceof Player player) {
                item.setTarget(player.getUUID());
            }
            item.setPickUpDelay(0);
            pulledAny = true;
        }

        if (pulledAny) {
            serverLevel.playSound(
                    null,
                    caster.getX(),
                    caster.getY(),
                    caster.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS,
                    0.35F,
                    1.2F
            );
        }

        applyCastCooldown(context, COOLDOWN_TICKS);
    }

    private static void spawnParticles(net.minecraft.world.level.Level level, Vec3 center) {
        var random = level.getRandom();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            double x = center.x + (random.nextDouble() - 0.5) * 2.0;
            double y = center.y + random.nextDouble() * 2.0;
            double z = center.z + (random.nextDouble() - 0.5) * 2.0;
            level.addParticle(
                    ParticleTypes.ENCHANT,
                    x,
                    y,
                    z,
                    (center.x - x) * 0.15,
                    (center.y - y) * 0.15,
                    (center.z - z) * 0.15
            );
        }
    }
}