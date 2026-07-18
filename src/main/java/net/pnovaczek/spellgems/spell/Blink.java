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
import net.pnovaczek.spellgems.spell.enchantment.UtilityEnchantment;
import net.pnovaczek.spellgems.spell.enchantment.UtilityEnchantments;

import java.util.List;
import java.util.Optional;

public class Blink extends AbstractSpell {

    private static final int PARTICLE_COUNT = 32;

    @Override
    public Identifier id() {
        return SpellIds.BLINK;
    }

    @Override
    protected int getCooldownTicks() {
        return 20;
    }

    @Override
    protected boolean performCast(SpellContext context) {
        var caster = context.caster();
        // alive handled by base

        double baseMaxDistance = Spellgems.CONFIG.spells.blink.maxDistance;
        List<UtilityEnchantment> utilities = (context.data() != null) ? context.data().utilityEffects() : List.of();
        boolean hasExtend = utilities.stream().anyMatch(u -> u.is(UtilityEnchantments.EXTEND));
        double maxDistance = hasExtend
                ? baseMaxDistance * Spellgems.CONFIG.spells.blink.extendMultiplier
                : baseMaxDistance;
        Vec3 target = SpellTargeting.resolveCastCenter(caster, maxDistance);

        if (context.level().isClientSide()) {
            spawnParticles(context.level(), caster.position(), target);
            return false;  // client effects only; cooldown will be no-op
        }

        if (!(context.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        Optional<Vec3> safePosition = resolveSafeTeleportPosition(serverLevel, caster, target);
        if (safePosition.isEmpty()) {
            return false;
        }

        Vec3 destination = safePosition.get();
        Vec3 origin = caster.position();

        if (!teleportCaster(serverLevel, caster, destination)) {
            return false;
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

        return true;
    }

    private static boolean teleportCaster(ServerLevel level, LivingEntity caster, Vec3 destination) {
        if (caster instanceof ServerPlayer serverPlayer) {
            // Use relative rotation deltas of 0 to preserve the player's current orientation.
            // This matches the approach used by vanilla's ServerPlayer.teleportTo(x, y, z).
            ServerPlayer teleported = serverPlayer.teleport(
                    new TeleportTransition(
                            level,
                            destination,
                            Vec3.ZERO,
                            0.0F,
                            0.0F,
                            Relative.union(Relative.DELTA, Relative.ROTATION),
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