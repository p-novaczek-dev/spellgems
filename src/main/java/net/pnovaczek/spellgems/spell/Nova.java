package net.pnovaczek.spellgems.spell;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.SpellgemsConfig;
import net.pnovaczek.spellgems.spell.enchantment.ModifierEnchantments;
import net.pnovaczek.spellgems.spell.enchantment.StrikeEnchantment;

import java.util.List;

public class Nova extends AbstractSpell {

    private static final int BURST_PULSE_COUNT = 5;
    private static final int BURST_TICK_SPACING = 3;

    @Override
    public Identifier id() {
        return SpellIds.NOVA;
    }

    @Override
    public int defaultDurabilityCost() {
        return 16;
    }

    @Override
    protected boolean performCast(SpellContext context) {
        var level = context.level();
        var caster = context.caster();
        // alive handled by base

        boolean hasPower = false;
        boolean hasExpand = false;
        boolean isBurst = false;

        for (var mod : context.data().modifierEffects()) {
            if (mod.is(ModifierEnchantments.POWER)) {
                hasPower = true;
            } else if (mod.is(ModifierEnchantments.EXPAND)) {
                hasExpand = true;
            } else if (mod.is(ModifierEnchantments.BURST)) {
                isBurst = true;
            }
        }

        boolean finalHasPower = hasPower;
        boolean finalHasExpand = hasExpand;
        Runnable pulse = () -> {
            if (!caster.isAlive()) return;
            if (level.isClientSide()) {
                spawnNovaParticles(context, finalHasExpand);
            } else if (level instanceof ServerLevel serverLevel) {
                applyNovaDamage(context, serverLevel, finalHasPower, finalHasExpand);
            }
        };

        if (isBurst) {
            scheduleBurst(context, BURST_PULSE_COUNT, BURST_TICK_SPACING, pulse);
        } else {
            pulse.run();
        }

        return true;
    }

    private void applyNovaDamage(SpellContext context, ServerLevel level, boolean hasPower, boolean hasExpand) {
        var caster = context.caster();
        var config = Spellgems.CONFIG.spells.nova;
        var strikes = context.data().strikeEffects();

        Vec3 center = getEffectCenter(caster, config);
        float radius = getEffectiveRadius(config, hasExpand);
        float damage = getEffectiveDamage(config, hasPower);

        AABB searchBox = new AABB(center, center).inflate(radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, searchBox, entity -> !shouldSkipTarget(entity, caster));

        for (LivingEntity target : targets) {
            if (center.distanceToSqr(target.position()) > radius * radius) {
                continue;
            }

            target.hurtServer(level, caster.damageSources().magic(), damage);

            for (StrikeEnchantment strike : strikes) {
                strike.applyTo(target, caster);
            }

            // knockback() subtracts the direction vector from velocity, so pass toward-center
            // (same convention as hurt knockback: sourcePosition - targetPosition)
            double dx = center.x - target.getX();
            double dz = center.z - target.getZ();
            target.knockback(config.knockbackStrength, dx, dz);
        }

        level.playSound(
                null,
                center.x, center.y, center.z,
                SoundEvents.DRAGON_FIREBALL_EXPLODE,
                SoundSource.PLAYERS,
                0.5F,
                1.0F
        );
    }

    private void spawnNovaParticles(SpellContext context, boolean hasExpand) {
        var caster = context.caster();
        var level = context.level();
        var config = Spellgems.CONFIG.spells.nova;
        var strikes = context.data().strikeEffects();
        var random = level.getRandom();

        Vec3 center = getEffectCenter(caster, config);
        float radius = getEffectiveRadius(config, hasExpand);
        int particleCount = getEffectiveParticleCount(config, hasExpand);
        float particleSpeed = config.particleSpeed;

        int dustColor = context.data().getTintColor();
        if (dustColor == 0xFFFFFF) {
            dustColor = DEFAULT_DUST_COLOR;
        }
        var dustOptions = new DustParticleOptions(dustColor, 1.0F);

        for (int i = 0; i < particleCount; i++) {
            Vec3 pos = randomPointInSphere(center, radius, random);
            Vec3 velocity = pos.subtract(center);
            double len = velocity.length();
            if (len < 1.0E-8) {
                continue;
            }
            velocity = velocity.scale(particleSpeed / len);

            if (strikes.isEmpty()) {
                level.addParticle(
                        dustOptions,
                        pos.x, pos.y, pos.z,
                        velocity.x, velocity.y, velocity.z
                );
            } else {
                for (StrikeEnchantment strike : strikes) {
                    strike.addParticle(level, pos.x, pos.y, pos.z, random, velocity.x, velocity.y, velocity.z);
                }
            }
        }
    }

    private static Vec3 getEffectCenter(LivingEntity caster, SpellgemsConfig.NovaSpellConfig config) {
        return new Vec3(caster.getX(), caster.getY() + config.centerYOffset, caster.getZ());
    }

    private static float getEffectiveRadius(SpellgemsConfig.NovaSpellConfig config, boolean hasExpand) {
        return hasExpand ? config.radius * config.expandRadiusMultiplier : config.radius;
    }

    private static int getEffectiveParticleCount(SpellgemsConfig.NovaSpellConfig config, boolean hasExpand) {
        return hasExpand
                ? Math.max(1, (int) (config.particleCount * config.expandRadiusMultiplier))
                : config.particleCount;
    }

    private static float getEffectiveDamage(SpellgemsConfig.NovaSpellConfig config, boolean hasPower) {
        return hasPower ? config.damage * config.powerDamageMultiplier : config.damage;
    }

    private static boolean shouldSkipTarget(LivingEntity target, LivingEntity caster) {
        return target == caster || target.isSpectator() || !target.isAlive();
    }
}