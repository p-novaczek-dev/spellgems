package net.pnovaczek.spellgems.spell.enchantment;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.AbstractWindCharge;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SimpleExplosionDamageCalculator;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.entity.AstralArrow;
import net.pnovaczek.spellgems.entity.FrostbiteCloud;
import net.pnovaczek.spellgems.entity.InfernoCloud;
import net.pnovaczek.spellgems.entity.PlagueCloud;
import net.pnovaczek.spellgems.spell.SpellParticles;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import org.jspecify.annotations.Nullable;

/**
 * Registry of {@link StrikeEffect} strategies keyed by strike id.
 */
public final class StrikeEffects {

    private static final int DEFAULT_TINT = 0xCCCCCC;
    private static final double VOLLEY_SPAWN_MIN_DISTANCE = 4.0;
    private static final double VOLLEY_SPAWN_DISTANCE_RANGE = 8.0;
    private static final int VOLLEY_SPAWN_ATTEMPTS = 16;

    /** Damages living entities only; does not break blocks or destroy dropped items. */
    private static final ExplosionDamageCalculator EXPLOSION_ENTITIES_ONLY = new SimpleExplosionDamageCalculator(
            false,
            true,
            Optional.empty(),
            Optional.empty()
    ) {
        @Override
        public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
            return entity instanceof LivingEntity;
        }
    };

    private static final Map<Identifier, StrikeEffect> BY_ID = new HashMap<>();

    static {
        register(StrikeEnchantments.POISON, statusEffect(MobEffects.POISON, 0x339933, ParticleTypes.GLOW_SQUID_INK, 0.35D));
        register(StrikeEnchantments.FLAME, ignite(0xFF5500, ParticleTypes.FLAME, 0.6D));
        register(StrikeEnchantments.FROST, freeze(0x88DDFF, ParticleTypes.SNOWFLAKE, 0.4D));
        register(StrikeEnchantments.SLOW, statusEffect(MobEffects.SLOWNESS, 0x5555FF, ParticleTypes.CLOUD, 0.3D));
        register(StrikeEnchantments.LEVITATE, statusEffect(MobEffects.LEVITATION, 0xAA88FF, ParticleTypes.END_ROD, 0.25D));

        register(StrikeEnchantments.INFERNO, conditionalCloud(
                0xCC3300,
                ParticleTypes.FLAME,
                0.6D,
                (living, caster) -> living.getRemainingFireTicks() > 0 || living.hasEffect(MobEffects.WITHER),
                (level, pos, caster) -> new InfernoCloud(level, pos.x(), pos.y() + 0.1F, pos.z(), caster),
                SoundEvents.FIRECHARGE_USE
        ));
        register(StrikeEnchantments.FROSTBITE, conditionalCloud(
                0x66BBDD,
                ParticleTypes.SNOWFLAKE,
                0.4D,
                (living, caster) -> living.getTicksFrozen() > 0 || living.hasEffect(MobEffects.WITHER),
                (level, pos, caster) -> new FrostbiteCloud(level, pos.x(), pos.y() + 0.1F, pos.z(), caster),
                SoundEvents.POWDER_SNOW_BREAK
        ));
        register(StrikeEnchantments.PLAGUE, conditionalCloud(
                0x227722,
                ParticleTypes.GLOW_SQUID_INK,
                0.35D,
                (living, caster) -> living.hasEffect(MobEffects.POISON) || living.hasEffect(MobEffects.WITHER),
                (level, pos, caster) -> new PlagueCloud(level, pos.x(), pos.y() + 0.1F, pos.z(), caster),
                SoundEvents.WITHER_AMBIENT
        ));

        register(StrikeEnchantments.LIGHTNING, new StrikeEffect() {
            @Override
            public void apply(LivingEntity target, LivingEntity caster) {
                Level level = target.level();
                if (level.isClientSide()) {
                    return;
                }
                if (!(target.hasEffect(MobEffects.SLOWNESS) || target.hasEffect(MobEffects.LEVITATION))) {
                    return;
                }
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
                if (bolt == null) {
                    return;
                }
                Vec3 pos = target.position();
                bolt.snapTo(pos.x, pos.y, pos.z);
                if (caster instanceof ServerPlayer serverPlayer) {
                    bolt.setCause(serverPlayer);
                }
                level.addFreshEntity(bolt);
            }

            @Override
            public int tintColor() {
                return 0xFFEE77;
            }

            @Override
            public void addParticle(Level level, @Nullable Entity exceptViewer, double x, double y, double z, RandomSource random, double dx, double dy, double dz) {
                particles(level, exceptViewer, ParticleTypes.ELECTRIC_SPARK, 0.2D, x, y, z, random, dx, dy, dz);
            }
        });

        register(StrikeEnchantments.EXPLOSION, new StrikeEffect() {
            @Override
            public void apply(LivingEntity target, LivingEntity caster) {
                Level level = target.level();
                if (level.isClientSide()) {
                    return;
                }
                if (!(target.hasEffect(MobEffects.SLOWNESS) || target.hasEffect(MobEffects.LEVITATION))) {
                    return;
                }
                Vec3 pos = target.position();
                level.explode(
                        caster,
                        null,
                        EXPLOSION_ENTITIES_ONLY,
                        pos.x,
                        pos.y,
                        pos.z,
                        2.0F,
                        false,
                        Level.ExplosionInteraction.NONE
                );
            }

            @Override
            public int tintColor() {
                return 0xFFAA00;
            }

            @Override
            public void addParticle(Level level, @Nullable Entity exceptViewer, double x, double y, double z, RandomSource random, double dx, double dy, double dz) {
                particles(level, exceptViewer, ParticleTypes.SMOKE, 0.5D, x, y, z, random, dx, dy, dz);
            }
        });

        register(StrikeEnchantments.DRAIN, new StrikeEffect() {
            @Override
            public void apply(LivingEntity target, LivingEntity caster) {
                if (target.level().isClientSide()) {
                    return;
                }
                caster.heal(Spellgems.CONFIG.drainHealPerTarget);
            }

            @Override
            public int tintColor() {
                return 0xCC2222;
            }

            @Override
            public void addParticle(Level level, @Nullable Entity exceptViewer, double x, double y, double z, RandomSource random, double dx, double dy, double dz) {
                particles(level, exceptViewer, ParticleTypes.SCULK_SOUL, 0.3D, x, y, z, random, dx, dy, dz);
            }
        });

        register(StrikeEnchantments.PURIFY, new StrikeEffect() {
            @Override
            public void apply(LivingEntity target, LivingEntity caster) {
                Level level = target.level();
                if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
                    return;
                }
                if (!(target.hasEffect(MobEffects.POISON) || target.hasEffect(MobEffects.WITHER))) {
                    return;
                }
                int duration = Spellgems.CONFIG.strikeEffectDuration;
                target.hurtServer(serverLevel, caster.damageSources().magic(), Spellgems.CONFIG.strikeCloudDamage);
                target.setRemainingFireTicks(duration);
                target.setTicksFrozen(duration);
                target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, duration, 0));
            }

            @Override
            public int tintColor() {
                return 0xEEFFEE;
            }

            @Override
            public void addParticle(Level level, @Nullable Entity exceptViewer, double x, double y, double z, RandomSource random, double dx, double dy, double dz) {
                particles(level, exceptViewer, ParticleTypes.SOUL_FIRE_FLAME, 0.3D, x, y, z, random, dx, dy, dz);
            }
        });

        register(StrikeEnchantments.VOLLEY, new StrikeEffect() {
            @Override
            public void apply(LivingEntity target, LivingEntity caster) {
                Level level = target.level();
                if (level.isClientSide()) {
                    return;
                }
                if (!(target.hasEffect(MobEffects.SLOWNESS) || target.hasEffect(MobEffects.LEVITATION))) {
                    return;
                }
                RandomSource random = level.getRandom();
                Vec3 targetCenter = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0);
                Vec3 pos = target.position();

                int arrowCount = Spellgems.CONFIG.volleyArrowCount;
                for (int i = 0; i < arrowCount; i++) {
                    Vec3 spawn = findVolleySpawn(level, targetCenter, random);
                    if (spawn == null) {
                        continue;
                    }

                    AstralArrow arrow = new AstralArrow(level, caster);
                    arrow.setPos(spawn.x, spawn.y, spawn.z);

                    double dx = targetCenter.x - spawn.x + (random.nextDouble() - 0.5) * 1.0;
                    double dy = targetCenter.y - spawn.y;
                    double dz = targetCenter.z - spawn.z + (random.nextDouble() - 0.5) * 1.0;
                    arrow.shoot(dx, dy, dz, 1.2F, 6.0F);

                    level.addFreshEntity(arrow);
                }

                level.playSound(
                        null,
                        pos.x(), pos.y(), pos.z(),
                        SoundEvents.ARROW_SHOOT,
                        SoundSource.PLAYERS,
                        0.6F,
                        0.8F + random.nextFloat() * 0.4F
                );
            }

            @Override
            public int tintColor() {
                return 0x77AAFF;
            }

            @Override
            public void addParticle(Level level, @Nullable Entity exceptViewer, double x, double y, double z, RandomSource random, double dx, double dy, double dz) {
                particles(level, exceptViewer, ParticleTypes.CRIT, 0.3D, x, y, z, random, dx, dy, dz);
            }
        });

        register(StrikeEnchantments.VENGEANCE, new StrikeEffect() {
            @Override
            public void apply(LivingEntity target, LivingEntity caster) {
                Level level = target.level();
                if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
                    return;
                }
                float missingHealth = caster.getMaxHealth() - caster.getHealth();
                if (missingHealth > 0.0F) {
                    target.hurtServer(serverLevel, caster.damageSources().magic(), missingHealth);
                }
            }

            @Override
            public int tintColor() {
                return 0xFF4444;
            }

            @Override
            public void addParticle(Level level, @Nullable Entity exceptViewer, double x, double y, double z, RandomSource random, double dx, double dy, double dz) {
                particles(level, exceptViewer, ParticleTypes.CRIT, 0.1D, x, y, z, random, dx, dy, dz);
            }
        });

        // Same gust explosion as a vanilla WindCharge projectile impact
        register(StrikeEnchantments.WIND_CHARGE, new StrikeEffect() {
            private static final float RADIUS = 1.2F;

            @Override
            public void apply(LivingEntity target, LivingEntity caster) {
                Level level = target.level();
                if (level.isClientSide()) {
                    return;
                }
                Vec3 pos = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0);
                level.explode(
                        caster,
                        null,
                        AbstractWindCharge.EXPLOSION_DAMAGE_CALCULATOR,
                        pos.x(),
                        pos.y(),
                        pos.z(),
                        RADIUS,
                        false,
                        Level.ExplosionInteraction.TRIGGER,
                        ParticleTypes.GUST_EMITTER_SMALL,
                        ParticleTypes.GUST_EMITTER_LARGE,
                        WeightedList.of(),
                        SoundEvents.WIND_CHARGE_BURST
                );
            }

            @Override
            public int tintColor() {
                return 0xC8E8FF;
            }

            @Override
            public void addParticle(Level level, @Nullable Entity exceptViewer, double x, double y, double z, RandomSource random, double dx, double dy, double dz) {
                particles(level, exceptViewer, ParticleTypes.SMALL_GUST, 0.4D, x, y, z, random, dx, dy, dz);
            }
        });
    }

    private StrikeEffects() {
    }

    public static StrikeEffect get(Identifier id) {
        return BY_ID.getOrDefault(id, NO_OP);
    }

    private static void register(Identifier id, StrikeEffect effect) {
        BY_ID.put(id, effect);
    }

    private static @Nullable Vec3 findVolleySpawn(Level level, Vec3 targetCenter, RandomSource random) {
        for (int attempt = 0; attempt < VOLLEY_SPAWN_ATTEMPTS; attempt++) {
            Vec3 spawn = randomVolleySpawn(targetCenter, random);
            if (hasClearVolleyPath(level, spawn, targetCenter)) {
                return spawn;
            }
        }
        return null;
    }

    private static Vec3 randomVolleySpawn(Vec3 center, RandomSource random) {
        double theta = random.nextDouble() * Math.PI * 2.0;
        double phi = Math.acos(2.0 * random.nextDouble() - 1.0);
        double radius = VOLLEY_SPAWN_MIN_DISTANCE + random.nextDouble() * VOLLEY_SPAWN_DISTANCE_RANGE;
        double sinPhi = Math.sin(phi);
        return center.add(
                radius * sinPhi * Math.cos(theta),
                radius * Math.cos(phi),
                radius * sinPhi * Math.sin(theta)
        );
    }

    private static boolean hasClearVolleyPath(Level level, Vec3 spawn, Vec3 targetCenter) {
        BlockPos spawnPos = BlockPos.containing(spawn);
        if (!level.isLoaded(spawnPos)) {
            return false;
        }
        if (!level.getBlockState(spawnPos).getCollisionShape(level, spawnPos).isEmpty()) {
            return false;
        }
        BlockHitResult hit = level.clip(new ClipContext(
                spawn,
                targetCenter,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                CollisionContext.empty()
        ));
        return hit.getType() == HitResult.Type.MISS;
    }

    private static final StrikeEffect NO_OP = new StrikeEffect() {
        @Override
        public void apply(LivingEntity target, LivingEntity caster) {
        }

        @Override
        public int tintColor() {
            return DEFAULT_TINT;
        }

        @Override
        public void addParticle(Level level, @Nullable Entity exceptViewer, double x, double y, double z, RandomSource random, double dx, double dy, double dz) {
                particles(level, exceptViewer, ParticleTypes.DUST_PLUME, 0.1D, x, y, z, random, dx, dy, dz);
        }
    };

    private static StrikeEffect statusEffect(
            net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect,
            int tint,
            ParticleOptions particle,
            double spread
    ) {
        return new StrikeEffect() {
            @Override
            public void apply(LivingEntity target, LivingEntity caster) {
                target.addEffect(new MobEffectInstance(effect, Spellgems.CONFIG.strikeEffectDuration, 0));
            }

            @Override
            public int tintColor() {
                return tint;
            }

            @Override
            public void addParticle(Level level, @Nullable Entity exceptViewer, double x, double y, double z, RandomSource random, double dx, double dy, double dz) {
                particles(level, exceptViewer, particle, spread, x, y, z, random, dx, dy, dz);
            }
        };
    }

    private static StrikeEffect ignite(int tint, ParticleOptions particle, double spread) {
        return new StrikeEffect() {
            @Override
            public void apply(LivingEntity target, LivingEntity caster) {
                target.setRemainingFireTicks(Spellgems.CONFIG.strikeEffectDuration);
            }

            @Override
            public int tintColor() {
                return tint;
            }

            @Override
            public void addParticle(Level level, @Nullable Entity exceptViewer, double x, double y, double z, RandomSource random, double dx, double dy, double dz) {
                particles(level, exceptViewer, particle, spread, x, y, z, random, dx, dy, dz);
            }
        };
    }

    private static StrikeEffect freeze(int tint, ParticleOptions particle, double spread) {
        return new StrikeEffect() {
            @Override
            public void apply(LivingEntity target, LivingEntity caster) {
                target.setTicksFrozen(Spellgems.CONFIG.strikeEffectDuration);
            }

            @Override
            public int tintColor() {
                return tint;
            }

            @Override
            public void addParticle(Level level, @Nullable Entity exceptViewer, double x, double y, double z, RandomSource random, double dx, double dy, double dz) {
                particles(level, exceptViewer, particle, spread, x, y, z, random, dx, dy, dz);
            }
        };
    }

    @FunctionalInterface
    private interface CloudFactory {
        net.minecraft.world.entity.Entity create(Level level, Vec3 pos, LivingEntity caster);
    }

    private static StrikeEffect conditionalCloud(
            int tint,
            ParticleOptions particle,
            double spread,
            BiPredicate<LivingEntity, LivingEntity> condition,
            CloudFactory cloudFactory,
            SoundEvent sound
    ) {
        return new StrikeEffect() {
            @Override
            public void apply(LivingEntity target, LivingEntity caster) {
                Level level = target.level();
                if (level.isClientSide() || !condition.test(target, caster)) {
                    return;
                }
                Vec3 pos = target.position();
                level.addFreshEntity(cloudFactory.create(level, pos, caster));
                level.playSound(
                        null,
                        pos.x(), pos.y(), pos.z(),
                        sound,
                        SoundSource.PLAYERS,
                        0.8F,
                        1.0F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
                );
            }

            @Override
            public int tintColor() {
                return tint;
            }

            @Override
            public void addParticle(Level level, @Nullable Entity exceptViewer, double x, double y, double z, RandomSource random, double dx, double dy, double dz) {
                particles(level, exceptViewer, particle, spread, x, y, z, random, dx, dy, dz);
            }
        };
    }

    private static void particles(
            Level level,
            @Nullable Entity exceptViewer,
            ParticleOptions particle,
            double spread,
            double x,
            double y,
            double z,
            RandomSource random,
            double dx,
            double dy,
            double dz
    ) {
        SpellParticles.add(
                level,
                exceptViewer,
                particle,
                x + (random.nextDouble() - 0.5) * spread,
                y + (random.nextDouble() - 0.5) * spread,
                z + (random.nextDouble() - 0.5) * spread,
                dx,
                dy,
                dz
        );
    }
}
