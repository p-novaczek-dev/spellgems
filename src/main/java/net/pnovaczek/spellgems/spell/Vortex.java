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
        return SpellIds.VORTEX;
    }

    @Override
    protected boolean performCast(SpellContext context) {
        var level = context.level();
        var caster = context.caster();
        // alive handled by base

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
            scheduleBurst(context, BURST_PULSE_COUNT, BURST_TICK_SPACING, pulse);
        } else {
            pulse.run();
        }

        return true;
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
                SoundEvents.ENDERMAN_TELEPORT,
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
        int particleCount = getEffectiveParticleCount(config, hasExpand);
        float particleSpeed = config.particleSpeed;

        int dustColor = context.data().getTintColor();
        if (dustColor == 0xFFFFFF) {
            dustColor = DEFAULT_DUST_COLOR;
        }
        var dustOptions = new DustParticleOptions(dustColor, 1.0F);

        for (int i = 0; i < particleCount; i++) {
            Vec3 pos = randomPointInSphere(center, radius, random);
            Vec3 velocity = center.subtract(pos);
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

    private static float getEffectiveRadius(SpellgemsConfig.VortexSpellConfig config, boolean hasExpand) {
        return hasExpand ? config.radius * config.expandRadiusMultiplier : config.radius;
    }

    private static int getEffectiveParticleCount(SpellgemsConfig.VortexSpellConfig config, boolean hasExpand) {
        return hasExpand
                ? Math.max(1, (int) (config.particleCount * config.expandRadiusMultiplier))
                : config.particleCount;
    }

    private static boolean isVortexTarget(Entity entity, LivingEntity caster) {
        if (entity == caster || !entity.isAlive() || entity.isSpectator()) {
            return false;
        }
        return entity instanceof LivingEntity || entity instanceof Projectile;
    }
}