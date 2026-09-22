package net.pnovaczek.spellgems.client.particle;

import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.pnovaczek.spellgems.particle.ModParticles;

import java.util.function.Function;

/**
 * Binds each spell mote type to {@link SpellMoteParticle}. Loader entrypoints supply the registrar.
 */
public final class SpellMoteParticleProviders {

    @FunctionalInterface
    public interface Registrar {
        <T extends ParticleOptions> void register(ParticleType<T> type, Function<SpriteSet, ParticleProvider<T>> factory);
    }

    private SpellMoteParticleProviders() {
    }

    public static void register(Registrar registrar) {
        registrar.register(ModParticles.FLAME_MOTE, sprites -> SpellMoteParticle.simple(sprites, SpellMoteParticle.Appearance.FLAME));
        registrar.register(ModParticles.SOUL_FIRE_MOTE, sprites -> SpellMoteParticle.simple(sprites, SpellMoteParticle.Appearance.SOUL_FIRE));
        registrar.register(ModParticles.INFEST_MOTE, sprites -> SpellMoteParticle.simple(sprites, SpellMoteParticle.Appearance.INFEST));
        registrar.register(ModParticles.RAID_OMEN_MOTE, sprites -> SpellMoteParticle.simple(sprites, SpellMoteParticle.Appearance.RAID_OMEN));
        registrar.register(ModParticles.GUST_MOTE, sprites -> SpellMoteParticle.simple(sprites, SpellMoteParticle.Appearance.GUST));
        registrar.register(ModParticles.DUST_PLUME_MOTE, sprites -> SpellMoteParticle.simple(sprites, SpellMoteParticle.Appearance.DUST_PLUME));
        registrar.register(ModParticles.SNOWFLAKE_MOTE, sprites -> SpellMoteParticle.tinted(sprites, SingleQuadParticle.Layer.OPAQUE, SpellMoteParticle.Light.WORLD));
        registrar.register(ModParticles.EFFECT_MOTE, sprites -> SpellMoteParticle.tinted(sprites, SingleQuadParticle.Layer.TRANSLUCENT, SpellMoteParticle.Light.WORLD));
        registrar.register(ModParticles.SPARK_MOTE, sprites -> SpellMoteParticle.tinted(sprites, SingleQuadParticle.Layer.OPAQUE, SpellMoteParticle.Light.FULL));
        registrar.register(ModParticles.CRIT_MOTE, sprites -> SpellMoteParticle.tinted(sprites, SingleQuadParticle.Layer.OPAQUE, SpellMoteParticle.Light.WORLD));
        registrar.register(ModParticles.DUST_MOTE, sprites -> SpellMoteParticle.tinted(sprites, SingleQuadParticle.Layer.OPAQUE, SpellMoteParticle.Light.WORLD));
    }
}
