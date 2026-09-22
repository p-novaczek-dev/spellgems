package net.pnovaczek.spellgems.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.pnovaczek.spellgems.registry.ModRegistry;

/**
 * Spell particles that share flame's motion and lifetime.
 * Instances are created at class init so {@code StrikeEffects} can reference them
 * before NeoForge's {@code RegisterEvent}; {@link #register()} only inserts them.
 */
public final class ModParticles {

    public static final SimpleParticleType FLAME_MOTE = new ModSimpleParticleType();
    public static final SimpleParticleType SOUL_FIRE_MOTE = new ModSimpleParticleType();
    public static final SimpleParticleType INFEST_MOTE = new ModSimpleParticleType();
    public static final SimpleParticleType RAID_OMEN_MOTE = new ModSimpleParticleType();
    public static final SimpleParticleType GUST_MOTE = new ModSimpleParticleType();
    public static final SimpleParticleType DUST_PLUME_MOTE = new ModSimpleParticleType();
    public static final ParticleType<ColorParticleOption> SNOWFLAKE_MOTE = new SpellColorParticleType();
    public static final ParticleType<ColorParticleOption> EFFECT_MOTE = new SpellColorParticleType();
    public static final ParticleType<ColorParticleOption> SPARK_MOTE = new SpellColorParticleType();
    public static final ParticleType<ColorParticleOption> CRIT_MOTE = new SpellColorParticleType();
    public static final ParticleType<ColorParticleOption> DUST_MOTE = new SpellColorParticleType();

    private ModParticles() {
    }

    public static ParticleOptions tintedSnowflake(int rgb) {
        return tinted(SNOWFLAKE_MOTE, rgb);
    }

    public static ParticleOptions tintedEffect(int rgb) {
        return tinted(EFFECT_MOTE, rgb);
    }

    public static ParticleOptions tintedSpark(int rgb) {
        return tinted(SPARK_MOTE, rgb);
    }

    public static ParticleOptions tintedCrit(int rgb) {
        return tinted(CRIT_MOTE, rgb);
    }

    /** {@code rgb} is a 24-bit color, the same form as {@code DustParticleOptions}. */
    public static ParticleOptions tintedDust(int rgb) {
        return tinted(DUST_MOTE, rgb);
    }

    private static ParticleOptions tinted(ParticleType<ColorParticleOption> type, int rgb) {
        return ColorParticleOption.create(type, 0xFF000000 | (rgb & 0xFFFFFF));
    }

    public static void register() {
        register("flame_mote", FLAME_MOTE);
        register("soul_fire_mote", SOUL_FIRE_MOTE);
        register("infest_mote", INFEST_MOTE);
        register("raid_omen_mote", RAID_OMEN_MOTE);
        register("gust_mote", GUST_MOTE);
        register("dust_plume_mote", DUST_PLUME_MOTE);
        register("snowflake_mote", SNOWFLAKE_MOTE);
        register("effect_mote", EFFECT_MOTE);
        register("spark_mote", SPARK_MOTE);
        register("crit_mote", CRIT_MOTE);
        register("dust_mote", DUST_MOTE);
    }

    private static void register(String path, ParticleType<?> type) {
        ModRegistry.register(BuiltInRegistries.PARTICLE_TYPE, path, type);
    }

    private static final class ModSimpleParticleType extends SimpleParticleType {
        private ModSimpleParticleType() {
            super(false);
        }
    }

    private static final class SpellColorParticleType extends ParticleType<ColorParticleOption> {
        private SpellColorParticleType() {
            super(false);
        }

        @Override
        public MapCodec<ColorParticleOption> codec() {
            return ColorParticleOption.codec(this);
        }

        @Override
        @SuppressWarnings("unchecked")
        public StreamCodec<? super RegistryFriendlyByteBuf, ColorParticleOption> streamCodec() {
            return (StreamCodec<? super RegistryFriendlyByteBuf, ColorParticleOption>) (StreamCodec<?, ?>) ColorParticleOption.streamCodec(this);
        }
    }
}
