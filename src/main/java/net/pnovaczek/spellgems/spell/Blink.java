package net.pnovaczek.spellgems.spell;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.pnovaczek.spellgems.Spellgems;

import java.util.Optional;

public class Blink extends AbstractSpell {

    private static final int COOLDOWN_TICKS = 20;
    private static final int PARTICLE_COUNT = 32;

    @Override
    public Identifier id() {
        return Spells.BLINK;
    }

    @Override
    public void cast(SpellContext context) {
        var caster = context.caster();
        if (!caster.isAlive()) {
            return;
        }

        double maxDistance = Spellgems.CONFIG.spells.blink.maxDistance;
        Vec3 target = SpellTargeting.resolveCastCenter(caster, maxDistance);

        if (context.level().isClientSide()) {
            spawnParticles(context.level(), caster.position(), target);
            return;
        }

        if (!(context.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Optional<Vec3> safePosition = resolveSafeTeleportPosition(serverLevel, caster, target);
        if (safePosition.isEmpty()) {
            return;
        }

        Vec3 destination = safePosition.get();
        Vec3 origin = caster.position();

        if (!teleportCaster(serverLevel, caster, destination)) {
            return;
        }

        serverLevel.playSound(
                null,
                origin.x, origin.y, origin.z,
                SoundEvents.PLAYER_TELEPORT,
                SoundSource.PLAYERS,
                0.75F,
                1.0F
        );
        serverLevel.playSound(
                null,
                destination.x, destination.y, destination.z,
                SoundEvents.PLAYER_TELEPORT,
                SoundSource.PLAYERS,
                0.75F,
                1.0F
        );

        applyCastCooldown(context, COOLDOWN_TICKS);
    }

    private static boolean teleportCaster(ServerLevel level, LivingEntity caster, Vec3 destination) {
        if (caster instanceof ServerPlayer serverPlayer) {
            ServerPlayer teleported = serverPlayer.teleport(
                    new TeleportTransition(
                            level,
                            destination,
                            Vec3.ZERO,
                            serverPlayer.getYRot(),
                            serverPlayer.getXRot(),
                            Relative.union(Relative.ROTATION, Relative.DELTA),
                            TeleportTransition.DO_NOTHING
                    )
            );
            if (teleported == null) {
                return false;
            }

            teleported.resetFallDistance();
            teleported.resetCurrentImpulseContext();
            return true;
        }

        caster.teleportTo(destination.x, destination.y, destination.z);
        caster.resetFallDistance();
        return true;
    }

    private static Optional<Vec3> resolveSafeTeleportPosition(Level level, LivingEntity entity, Vec3 target) {
        Vec3 view = entity.getViewVector(1.0F);
        Vec3 horizontalView = new Vec3(view.x, 0.0, view.z);
        if (horizontalView.lengthSqr() > 1.0E-8) {
            horizontalView = horizontalView.normalize();
        }

        Vec3 feet = new Vec3(target.x, target.y, target.z);
        Vec3[] candidates = {
                feet,
                feet.add(0.0, 0.5, 0.0),
                feet.add(0.0, 1.0, 0.0),
                feet.subtract(horizontalView.scale(0.5)),
                feet.subtract(horizontalView),
                feet.add(0.0, -0.5, 0.0),
        };

        for (Vec3 candidate : candidates) {
            if (isSafeTeleportPosition(level, entity, candidate)) {
                return Optional.of(candidate);
            }
        }

        return Optional.empty();
    }

    private static boolean isSafeTeleportPosition(Level level, LivingEntity entity, Vec3 feet) {
        Vec3 current = entity.position();
        AABB box = entity.getBoundingBox().move(
                feet.x - current.x,
                feet.y - current.y,
                feet.z - current.z
        );
        return level.noCollision(entity, box) && !level.containsAnyLiquid(box);
    }

    private static void spawnParticles(Level level, Vec3 origin, Vec3 destination) {
        var random = level.getRandom();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            level.addParticle(
                    ParticleTypes.PORTAL,
                    origin.x + (random.nextDouble() - 0.5) * entityWidthEstimate(),
                    origin.y + random.nextDouble() * 2.0,
                    origin.z + (random.nextDouble() - 0.5) * entityWidthEstimate(),
                    random.nextGaussian(),
                    0.0,
                    random.nextGaussian()
            );
            level.addParticle(
                    ParticleTypes.PORTAL,
                    destination.x + (random.nextDouble() - 0.5) * entityWidthEstimate(),
                    destination.y + random.nextDouble() * 2.0,
                    destination.z + (random.nextDouble() - 0.5) * entityWidthEstimate(),
                    random.nextGaussian(),
                    0.0,
                    random.nextGaussian()
            );
        }
    }

    private static double entityWidthEstimate() {
        return 0.6;
    }
}