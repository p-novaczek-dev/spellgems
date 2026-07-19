package net.pnovaczek.spellgems.spell;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * Particle helpers that work for both client-predicted casts (wand) and
 * server-only casts (spell dispenser). {@link Level#addParticle} is client-only;
 * server casts must use {@link ServerLevel#sendParticles}.
 */
public final class SpellParticles {

    private SpellParticles() {
    }

    /**
     * Spawns one particle. On the server, {@code dx/dy/dz} are treated as velocity
     * (sendParticles count=0 convention). On the client, same as {@link Level#addParticle}.
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
        if (level instanceof ServerLevel serverLevel) {
            // count=0 → offsets are velocity components, speed multiplies them
            serverLevel.sendParticles(particle, x, y, z, 0, dx, dy, dz, 1.0);
        } else {
            level.addParticle(particle, x, y, z, dx, dy, dz);
        }
    }

    public static void add(
            Level level,
            ParticleOptions particle,
            double x,
            double y,
            double z
    ) {
        add(level, particle, x, y, z, 0.0, 0.0, 0.0);
    }
}
