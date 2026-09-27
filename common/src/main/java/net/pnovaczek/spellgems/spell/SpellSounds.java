package net.pnovaczek.spellgems.spell;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Spell-owned sounds. A muffled cast (wool beside a spell dispenser) skips these.
 * Vanilla sounds from block breaks, harvesting, explosions, and lightning are played
 * by those systems and are not routed through here.
 */
public final class SpellSounds {

    private SpellSounds() {
    }

    public static void play(
            SpellContext context,
            Vec3 pos,
            SoundEvent sound,
            SoundSource source,
            float volume,
            float pitch
    ) {
        play(context, pos.x, pos.y, pos.z, sound, source, volume, pitch);
    }

    public static void play(
            SpellContext context,
            double x,
            double y,
            double z,
            SoundEvent sound,
            SoundSource source,
            float volume,
            float pitch
    ) {
        if (context.muffled()) {
            return;
        }
        context.level().playSound(null, x, y, z, sound, source, volume, pitch);
    }

    /**
     * Server broadcast that skips one player, for casts the client already predicted.
     * Falls back to a local sound on the client. No-op when the cast is muffled.
     */
    public static void playExcept(
            SpellContext context,
            @Nullable Player except,
            double x,
            double y,
            double z,
            SoundEvent sound,
            SoundSource source,
            float volume,
            float pitch
    ) {
        if (context.muffled()) {
            return;
        }
        Level level = context.level();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(except, x, y, z, sound, source, volume, pitch);
            return;
        }
        level.playLocalSound(x, y, z, sound, source, volume, pitch, false);
    }
}
