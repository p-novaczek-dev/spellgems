package net.pnovaczek.spellgems.spell;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Particle helpers for multiplayer-visible spell FX.
 * <p>
 * Policy (FX-01):
 * <ul>
 *   <li><b>Server</b> broadcasts particles so nearby players always see spell FX
 *       (wand, hand, dispenser, etc.).</li>
 *   <li><b>Client</b> prediction ({@link Spell#castPredicted}) may also spawn local
 *       particles for low latency; pass the caster as {@code exceptViewer} on the
 *       server so the caster is not double-rendered.</li>
 *   <li>Dispenser and other null-caster casts broadcast to everyone.</li>
 * </ul>
 * {@link Level#addParticle} is client-only; server uses {@link ServerLevel#sendParticles}.
 */
public final class SpellParticles {

    private SpellParticles() {
    }

    /**
     * Spawns one particle for all nearby viewers (server) or locally (client).
     */
    public static void add(
            Level level,
            ParticleOptions particle,
            double x,
            double y,
            double z,
            double dx,
            double dy,
            double dz
    ) {
        add(level, null, particle, x, y, z, dx, dy, dz);
    }

    public static void add(
            Level level,
            ParticleOptions particle,
            double x,
            double y,
            double z
    ) {
        add(level, null, particle, x, y, z, 0.0, 0.0, 0.0);
    }

    /**
     * Spawns one particle.
     *
     * @param exceptViewer on the server, this entity does not receive the packet
     *                     (use the casting player when they already have client prediction)
     */
    public static void add(
            Level level,
            @Nullable Entity exceptViewer,
            ParticleOptions particle,
            double x,
            double y,
            double z,
            double dx,
            double dy,
            double dz
    ) {
        if (level instanceof ServerLevel serverLevel) {
            // count=0 → offsets are velocity components, speed multiplies them
            if (exceptViewer == null) {
                serverLevel.sendParticles(particle, x, y, z, 0, dx, dy, dz, 1.0);
                return;
            }
            for (ServerPlayer player : serverLevel.players()) {
                if (player == exceptViewer) {
                    continue;
                }
                serverLevel.sendParticles(player, particle, false, false, x, y, z, 0, dx, dy, dz, 1.0);
            }
        } else {
            level.addParticle(particle, x, y, z, dx, dy, dz);
        }
    }

    public static void add(
            Level level,
            @Nullable Entity exceptViewer,
            ParticleOptions particle,
            double x,
            double y,
            double z
    ) {
        add(level, exceptViewer, particle, x, y, z, 0.0, 0.0, 0.0);
    }

    /**
     * Viewer to exclude from server broadcasts when the caster already has predicted FX.
     * Null for dispenser / non-player / no prediction.
     */
    public static @Nullable Entity predictionExcept(@Nullable Entity caster) {
        return caster instanceof ServerPlayer ? caster : null;
    }
}
