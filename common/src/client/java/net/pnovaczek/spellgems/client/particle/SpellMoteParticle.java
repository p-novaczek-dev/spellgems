package net.pnovaczek.spellgems.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;

/**
 * Same motion as vanilla {@code FlameParticle} / {@code RisingParticle} (26.1.2), with a slightly
 * shorter life. Sprite, tint, render layer, and light come from the preset.
 */
public class SpellMoteParticle extends SingleQuadParticle {

    private final SingleQuadParticle.Layer layer;
    private final Light light;

    private SpellMoteParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xd,
            double yd,
            double zd,
            TextureAtlasSprite sprite,
            SingleQuadParticle.Layer layer,
            Light light
    ) {
        super(level, x, y, z, xd, yd, zd, sprite);
        this.friction = 0.96F;
        this.xd = this.xd * 0.01F + xd;
        this.yd = this.yd * 0.01F + yd;
        this.zd = this.zd * 0.01F + zd;
        this.x = this.x + (this.random.nextFloat() - this.random.nextFloat()) * 0.05F;
        this.y = this.y + (this.random.nextFloat() - this.random.nextFloat()) * 0.05F;
        this.z = this.z + (this.random.nextFloat() - this.random.nextFloat()) * 0.05F;
        // Vanilla flame is (int)(8 / (random * 0.8 + 0.2)) + 4, about 12–44 ticks.
        int flameLifetime = (int) (8.0 / (this.random.nextFloat() * 0.8 + 0.2)) + 4;
        this.lifetime = Math.max(1, flameLifetime * 3 / 4);
        this.layer = layer;
        this.light = light;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return this.layer;
    }

    @Override
    public void move(double xa, double ya, double za) {
        this.setBoundingBox(this.getBoundingBox().move(xa, ya, za));
        this.setLocationFromBoundingbox();
    }

    @Override
    public float getQuadSize(float partialTick) {
        float ageFraction = (this.age + partialTick) / this.lifetime;
        return this.quadSize * (1.0F - ageFraction * ageFraction * 0.5F);
    }

    @Override
    public int getLightCoords(float partialTick) {
        return switch (this.light) {
            case FLAME -> LightCoordsUtil.addSmoothBlockEmission(
                    super.getLightCoords(partialTick), (this.age + partialTick) / this.lifetime);
            case FULL -> LightCoordsUtil.withBlock(super.getLightCoords(partialTick), 15);
            case WORLD -> super.getLightCoords(partialTick);
        };
    }

    /** World light, constant full-bright, or flame's age-based emission. */
    public enum Light {
        WORLD, FULL, FLAME
    }

    /**
     * How a preset looks. Motion is always flame; this only picks layer, light, and tint.
     */
    public enum Appearance {
        FLAME(SingleQuadParticle.Layer.OPAQUE, Light.FLAME),
        SOUL_FIRE(SingleQuadParticle.Layer.OPAQUE, Light.FLAME),
        INFEST(SingleQuadParticle.Layer.TRANSLUCENT, Light.WORLD),
        RAID_OMEN(SingleQuadParticle.Layer.TRANSLUCENT, Light.WORLD),
        GUST(SingleQuadParticle.Layer.OPAQUE, Light.FULL),
        DUST_PLUME(SingleQuadParticle.Layer.OPAQUE, Light.WORLD) {
            @Override
            void tint(SpellMoteParticle particle, RandomSource random) {
                float shift = random.nextFloat() * 0.2F;
                particle.setColor(
                        ARGB.red(DUST_PLUME_COLOR) / 255.0F - shift,
                        ARGB.green(DUST_PLUME_COLOR) / 255.0F - shift,
                        ARGB.blue(DUST_PLUME_COLOR) / 255.0F - shift
                );
            }
        };

        private static final int DUST_PLUME_COLOR = 12235202;

        private final SingleQuadParticle.Layer layer;
        private final Light light;

        Appearance(SingleQuadParticle.Layer layer, Light light) {
            this.layer = layer;
            this.light = light;
        }

        void tint(SpellMoteParticle particle, RandomSource random) {
        }
    }

    public static ParticleProvider<SimpleParticleType> simple(SpriteSet sprites, Appearance appearance) {
        return (type, level, x, y, z, xd, yd, zd, random) -> create(level, x, y, z, xd, yd, zd, sprites, appearance, random);
    }

    public static ParticleProvider<ColorParticleOption> tinted(SpriteSet sprites, SingleQuadParticle.Layer layer, Light light) {
        return (options, level, x, y, z, xd, yd, zd, random) -> {
            SpellMoteParticle particle = new SpellMoteParticle(
                    level, x, y, z, xd, yd, zd, sprites.get(random), layer, light
            );
            particle.setColor(options.getRed(), options.getGreen(), options.getBlue());
            particle.setAlpha(options.getAlpha());
            return particle;
        };
    }

    private static Particle create(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xd,
            double yd,
            double zd,
            SpriteSet sprites,
            Appearance appearance,
            RandomSource random
    ) {
        SpellMoteParticle particle = new SpellMoteParticle(
                level, x, y, z, xd, yd, zd, sprites.get(random), appearance.layer, appearance.light
        );
        appearance.tint(particle, random);
        return particle;
    }
}
