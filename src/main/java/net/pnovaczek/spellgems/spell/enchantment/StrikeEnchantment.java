package net.pnovaczek.spellgems.spell.enchantment;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.entity.AstralArrow;
import net.pnovaczek.spellgems.entity.FrostbiteCloud;
import net.pnovaczek.spellgems.entity.InfernoCloud;
import net.pnovaczek.spellgems.entity.PlagueCloud;

public record StrikeEnchantment(Identifier id) {

    private static final int VOLLEY_ARROW_COUNT = 8;

    public static final Codec<StrikeEnchantment> CODEC = Identifier.CODEC.xmap(
            StrikeEnchantment::new,
            StrikeEnchantment::id
    );

    public void applyTo(LivingEntity living, LivingEntity caster) {
        var duration = Spellgems.CONFIG.strikeEffectDuration;
        Level level = living.level();
        Vec3 pos = living.position();

        if (is(StrikeEnchantments.POISON)) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, duration, 0));
        } else if (is(StrikeEnchantments.FLAME)) {
            living.setRemainingFireTicks(duration);
        } else if (is(StrikeEnchantments.FROST)) {
            living.setTicksFrozen(duration);
        } else if (is(StrikeEnchantments.SLOW)) {
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, 0));
        } else if (is(StrikeEnchantments.LEVITATE)) {
            living.addEffect(new MobEffectInstance(MobEffects.LEVITATION, duration, 0));
        } else if (is(StrikeEnchantments.INFERNO)) {
            if (living.getRemainingFireTicks() > 0 && !level.isClientSide()) {
                InfernoCloud cloud = new InfernoCloud(level, pos.x(), pos.y() + 0.1F, pos.z(), caster);
                level.addFreshEntity(cloud);
                level.playSound(
                        null,
                        pos.x(), pos.y(), pos.z(),
                        SoundEvents.FIRECHARGE_USE,
                        SoundSource.PLAYERS,
                        0.8F,
                        1.0F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
                );
            }
        } else if (is(StrikeEnchantments.FROSTBITE)) {
            if (living.getTicksFrozen() > 0 && !level.isClientSide()) {
                FrostbiteCloud cloud = new FrostbiteCloud(level, pos.x(), pos.y() + 0.1F, pos.z(), caster);
                level.addFreshEntity(cloud);
                level.playSound(
                        null,
                        pos.x(), pos.y(), pos.z(),
                        SoundEvents.POWDER_SNOW_BREAK,
                        SoundSource.PLAYERS,
                        0.8F,
                        1.0F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
                );
            }
        } else if (is(StrikeEnchantments.PLAGUE)) {
            if (living.hasEffect(MobEffects.POISON) && !level.isClientSide()) {
                PlagueCloud cloud = new PlagueCloud(level, pos.x(), pos.y() + 0.1F, pos.z(), caster);
                level.addFreshEntity(cloud);
                level.playSound(
                        null,
                        pos.x(), pos.y(), pos.z(),
                        SoundEvents.SLIME_SQUISH,
                        SoundSource.PLAYERS,
                        0.8F,
                        1.0F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
                );
            }
        } else if (is(StrikeEnchantments.LIGHTNING)) {
            if (living.hasEffect(MobEffects.SLOWNESS) && !level.isClientSide()) {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
                if (bolt != null) {
                    bolt.snapTo(pos.x, pos.y, pos.z);
                    if (caster instanceof ServerPlayer serverPlayer) {
                        bolt.setCause(serverPlayer);
                    }
                    level.addFreshEntity(bolt);
                }
            }
        } else if (is(StrikeEnchantments.EXPLOSION)) {
            if (living.hasEffect(MobEffects.LEVITATION) && !level.isClientSide()) {
                level.explode(caster, pos.x, pos.y, pos.z, 2.0F, false, Level.ExplosionInteraction.MOB);
            }
        } else if (is(StrikeEnchantments.DRAIN)) {
            if (!level.isClientSide()) {
                float amount = Spellgems.CONFIG.drainHealPerTarget;
                caster.heal(amount);
                if (living.hasEffect(MobEffects.WITHER) && level instanceof ServerLevel serverLevel) {
                    living.hurtServer(serverLevel, caster.damageSources().magic(), amount);
                }
            }
        } else if (is(StrikeEnchantments.THERMAL_INVERSION)) {
            if (living.getRemainingFireTicks() > 0) {
                living.setTicksFrozen(duration);
                living.addEffect(new MobEffectInstance(MobEffects.LEVITATION, duration, 0));
            }
        } else if (is(StrikeEnchantments.PURIFY)) {
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                living.hurtServer(serverLevel, caster.damageSources().magic(), Spellgems.CONFIG.strikeCloudDamage);
                if (living.hasEffect(MobEffects.POISON)) {
                    living.setRemainingFireTicks(duration);
                }
            }
        } else if (is(StrikeEnchantments.VOLLEY)) {
            if (!level.isClientSide()
                    && (living.hasEffect(MobEffects.SLOWNESS) || living.hasEffect(MobEffects.LEVITATION))) {
                RandomSource random = level.getRandom();
                double targetCenterY = living.getY() + living.getBbHeight() * 0.5;

                for (int i = 0; i < VOLLEY_ARROW_COUNT; i++) {
                    double spawnX = living.getX() + (random.nextDouble() - 0.5) * 4.0;
                    double spawnZ = living.getZ() + (random.nextDouble() - 0.5) * 4.0;
                    double spawnY = targetCenterY + 8.0 + random.nextDouble() * 4.0;

                    AstralArrow arrow = new AstralArrow(level, caster);
                    arrow.setPos(spawnX, spawnY, spawnZ);

                    double dx = living.getX() - spawnX + (random.nextDouble() - 0.5) * 2.0;
                    double dy = targetCenterY - spawnY;
                    double dz = living.getZ() - spawnZ + (random.nextDouble() - 0.5) * 2.0;
                    arrow.shoot(dx, dy, dz, 1.2F, 10.0F);

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
        } else if (is(StrikeEnchantments.VENGEANCE)) {
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                float missingHealth = caster.getMaxHealth() - caster.getHealth();
                if (missingHealth > 0.0F) {
                    living.hurtServer(serverLevel, caster.damageSources().magic(), missingHealth);
                }
            }
        }
    }

    public boolean is(Identifier strikeId) {
        return id.equals(strikeId);
    }

    public String tooltipNameKey() {
        return "tooltip.spellgems.spell_enchantment." + id.getPath() + ".name";
    }

    public String tooltipDescriptionKey() {
        return "tooltip.spellgems.spell_enchantment." + id.getPath() + ".description";
    }

    public Integer getTintColor() {
        if (is(StrikeEnchantments.FLAME)) return 0xFF5500;
        if (is(StrikeEnchantments.INFERNO)) return 0xCC3300;
        if (is(StrikeEnchantments.FROST)) return 0x88DDFF;
        if (is(StrikeEnchantments.FROSTBITE)) return 0x66BBDD;
        if (is(StrikeEnchantments.POISON)) return 0x339933;
        if (is(StrikeEnchantments.PLAGUE)) return 0x227722;
        if (is(StrikeEnchantments.SLOW)) return 0x5555FF;
        if (is(StrikeEnchantments.LEVITATE)) return 0xAA88FF;
        if (is(StrikeEnchantments.LIGHTNING)) return 0xFFEE77;
        if (is(StrikeEnchantments.EXPLOSION)) return 0xFFAA00;
        if (is(StrikeEnchantments.DRAIN)) return 0xCC2222;
        if (is(StrikeEnchantments.THERMAL_INVERSION)) return 0x55CCFF;
        if (is(StrikeEnchantments.PURIFY)) return 0xEEFFEE;
        if (is(StrikeEnchantments.VOLLEY)) return 0x77AAFF;
        if (is(StrikeEnchantments.VENGEANCE)) return 0xFF4444;
        return 0xCCCCCC;
    }

    public void addParticle(Level level, double x, double y, double z, RandomSource random) {
        addParticle(level, x, y, z, random, 0.0, 0.0, 0.0);
    }

    public void addParticle(Level level, double x, double y, double z, RandomSource random, double dx, double dy, double dz) {
        var randomSpread = 0.1D;
        SimpleParticleType particleType = ParticleTypes.CRIT;

        if (is(StrikeEnchantments.FROST)) {
            particleType = ParticleTypes.SNOWFLAKE;
            randomSpread = 0.4D;
        }
        else if (is(StrikeEnchantments.FROSTBITE)) {
            particleType = ParticleTypes.SNOWFLAKE;
            randomSpread = 0.4D;
        }
        else if (is(StrikeEnchantments.FLAME)) {
            particleType = ParticleTypes.FLAME;
            randomSpread = 0.6D;
        }
        else if (is(StrikeEnchantments.INFERNO)) {
            particleType = ParticleTypes.LARGE_SMOKE;
            randomSpread = 0.6D;
        }
        else if (is(StrikeEnchantments.POISON)) {
            particleType = ParticleTypes.WITCH;
            randomSpread = 0.35D;
        }
        else if (is(StrikeEnchantments.PLAGUE)) {
            particleType = ParticleTypes.WITCH;
            randomSpread = 0.35D;
        }
        else if (is(StrikeEnchantments.LEVITATE)) {
            particleType = ParticleTypes.END_ROD;
            randomSpread = 0.25D;
        }
        else if (is(StrikeEnchantments.LIGHTNING)) {
            particleType = ParticleTypes.ELECTRIC_SPARK;
            randomSpread = 0.2D;
        }
        else if (is(StrikeEnchantments.EXPLOSION)) {
            particleType = ParticleTypes.EXPLOSION;
            randomSpread = 0.5D;
        }
        else if (is(StrikeEnchantments.VENGEANCE)) {
            particleType = ParticleTypes.DAMAGE_INDICATOR;
        }
        else if (is(StrikeEnchantments.PURIFY)) {
            particleType = ParticleTypes.HAPPY_VILLAGER;
            randomSpread = 0.3D;
        }
        else if (is(StrikeEnchantments.VOLLEY)) {
            particleType = ParticleTypes.ENCHANTED_HIT;
            randomSpread = 0.3D;
        }
        else if (is(StrikeEnchantments.DRAIN)) {
            particleType = ParticleTypes.ANGRY_VILLAGER;
            randomSpread = 0.3D;
        }
        else if (is(StrikeEnchantments.SLOW)) {
            particleType = ParticleTypes.BUBBLE;
            randomSpread = 0.3D;
        }
        else if (is(StrikeEnchantments.THERMAL_INVERSION)) {
            particleType = ParticleTypes.POOF;
            randomSpread = 0.3D;
        }

        level.addParticle(
                particleType,
                x + (random.nextDouble() - 0.5) * randomSpread,
                y + (random.nextDouble() - 0.5) * randomSpread,
                z + (random.nextDouble() - 0.5) * randomSpread,
                dx, dy, dz
        );
    }
}