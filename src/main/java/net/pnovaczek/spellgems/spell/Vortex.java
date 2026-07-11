package net.pnovaczek.spellgems.spell;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.SpellgemsConfig;
import net.pnovaczek.spellgems.spell.enchantment.ModifierEnchantments;
import net.pnovaczek.spellgems.spell.enchantment.StrikeEnchantment;

import java.util.List;

public class Vortex extends AbstractSpell {

    private static final int BURST_PULSE_COUNT = 5;
    private static final int BURST_TICK_SPACING = 20;
    private static final int DEFAULT_DUST_COLOR = 0x888888;

    @Override
    public Identifier id() {
        return Spells.VORTEX;
    }

    @Override
    public void cast(SpellContext context) {
        var level = context.level();
        var caster = context.caster();
        if (!caster.isAlive()) return;

        var config = Spellgems.CONFIG.spells.vortex;
        Vec3 center = SpellTargeting.resolveCastCenter(caster, config.maxDistance);

        boolean hasExpand = false;
        boolean isBurst = false;

        for (var mod : context.data().modifierEffects()) {
            if (mod.is(ModifierEnchantments.EXPAND)) {
                hasExpand = true;
            } else if (mod.is(ModifierEnchantments.BURST)) {
                isBurst = true;
            }
        }

        boolean finalHasExpand = hasExpand;
        Vec3 finalCenter = center;
        Runnable pulse = () -> executePulse(context, finalCenter, finalHasExpand);

        if (isBurst) {
            for (int i = 0; i < BURST_PULSE_COUNT; i++) {
                if (i == 0) {
                    pulse.run();
                } else {
                    int delayTicks = i * BURST_TICK_SPACING;
                    if (level.isClientSide()) {
                        SpellBurstScheduler.scheduleClient(level.getGameTime(), delayTicks, pulse);
                    } else {
                        SpellBurstScheduler.scheduleServer(level.getServer().getTickCount(), delayTicks, pulse);
                    }
                }
            }
        } else {
            pulse.run();
        }

        applyCastCooldown(context, 20);
    }

    private void executePulse(SpellContext context, Vec3 center, boolean hasExpand) {
        var level = context.level();
        var caster = context.caster();
        if (!caster.isAlive()) return;

        if (level.isClientSide()) {
            spawnSphereParticles(context, center, hasExpand);
        } else if (level instanceof ServerLevel serverLevel) {
            applyVortexPull(context, serverLevel, center, hasExpand);
        }
    }

    private void applyVortexPull(SpellContext context, ServerLevel level, Vec3 center, boolean hasExpand) {
        var caster = context.caster();
        var config = Spellgems.CONFIG.spells.vortex;
        var strikes = context.data().strikeEffects();
        float radius = getEffectiveRadius(config, hasExpand);
        float damage = config.damage;

        AABB searchBox = new AABB(center, center).inflate(radius);
        List<Entity> targets = level.getEntities(caster, searchBox, entity -> isVortexTarget(entity, caster));

        for (Entity entity : targets) {
            if (center.distanceToSqr(entity.position()) > radius * radius) {
                continue;
            }

            displaceTowardCenter(entity, center, config.pullDistance, config.pullStrength);

            if (entity instanceof LivingEntity living) {
                if (damage > 0.0F) {
                    living.hurtServer(level, caster.damageSources().magic(), damage);
                }
                for (StrikeEnchantment strike : strikes) {
                    strike.applyTo(living, caster);
                }
            }
        }

        level.playSound(
                null,
                center.x, center.y, center.z,
                SoundEvents.SLIME_ATTACK,
                SoundSource.PLAYERS,
                0.6F,
                0.8F
        );
    }

    private void spawnSphereParticles(SpellContext context, Vec3 center, boolean hasExpand) {
        var level = context.level();
        var config = Spellgems.CONFIG.spells.vortex;
        var strikes = context.data().strikeEffects();
        var random = level.getRandom();
        float radius = getEffectiveRadius(config, hasExpand);
        int particleCount = config.particleCount;
        float particleSpeed = config.particleSpeed;

        int dustColor = context.data().getTintColor();
        if (dustColor == 0xFFFFFF) {
            dustColor = DEFAULT_DUST_COLOR;
        }
        var dustOptions = new DustParticleOptions(dustColor, 1.0F);

        for (int i = 0; i < particleCount; i++) {
            Vec3 pos = randomPointInSphere(center, radius, random);
            Vec3 velocity = center.subtract(pos);
            double speed = velocity.length();
            if (speed < 1.0E-8) {
                continue;
            }
            velocity = velocity.scale(particleSpeed / speed);

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

    private static void displaceTowardCenter(Entity entity, Vec3 center, float pullDistance, float pullStrength) {
        Vec3 offset = center.subtract(entity.position());
        if (offset.lengthSqr() < 1.0E-8) {
            return;
        }

        if (entity instanceof LivingEntity living) {
            // knockback() subtracts the direction vector from velocity, so pass away-from-center
            // (opposite of Nova's outward push, which passes toward-center)
            double dx = entity.getX() - center.x;
            double dz = entity.getZ() - center.z;
            living.knockback(pullStrength, dx, dz);
        } else {
            Vec3 delta = offset.normalize().scale(pullDistance);
            entity.setPos(entity.getX() + delta.x, entity.getY() + delta.y, entity.getZ() + delta.z);
        }
    }

    private static Vec3 randomPointInSphere(Vec3 center, float radius, net.minecraft.util.RandomSource random) {
        double theta = Math.PI * 2 * random.nextDouble();
        double phi = Math.acos(2 * random.nextDouble() - 1);
        double r = radius * Math.cbrt(random.nextDouble());
        double sinPhi = Math.sin(phi);

        return center.add(
                r * sinPhi * Math.cos(theta),
                r * Math.cos(phi),
                r * sinPhi * Math.sin(theta)
        );
    }

    private static float getEffectiveRadius(SpellgemsConfig.VortexSpellConfig config, boolean hasExpand) {
        return hasExpand ? config.radius * config.expandRadiusMultiplier : config.radius;
    }

    private static boolean isVortexTarget(Entity entity, LivingEntity caster) {
        if (entity == caster || !entity.isAlive() || entity.isSpectator()) {
            return false;
        }
        return entity instanceof LivingEntity || entity instanceof Projectile;
    }
}