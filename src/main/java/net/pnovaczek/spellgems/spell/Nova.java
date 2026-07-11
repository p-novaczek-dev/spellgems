package net.pnovaczek.spellgems.spell;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
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
        return Spells.NOVA;
    }

    @Override
    public void cast(SpellContext context) {
        var level = context.level();
        var caster = context.caster();
        if (!caster.isAlive()) return;

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
                SoundEvents.GENERIC_EXPLODE,
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
        int particleCount = Mth.clamp(24 + (int) (radius * 4), 24, 64);

        for (int i = 0; i < particleCount; i++) {
            double angle = (Math.PI * 2 * i) / particleCount + (random.nextDouble() - 0.5) * 0.2;
            double dirX = Mth.cos((float) angle);
            double dirZ = Mth.sin((float) angle);

            // Spawn near the center with small random offset
            double startJitter = (random.nextDouble() - 0.5) * 0.3;
            double px = center.x + dirX * startJitter * 0.4 + (random.nextDouble() - 0.5) * 0.2;
            double py = center.y + (random.nextDouble() - 0.5) * 0.15;
            double pz = center.z + dirZ * startJitter * 0.4 + (random.nextDouble() - 0.5) * 0.2;

            // Outward velocity so particles travel from center toward (and past) the radius edge
            double speed = 0.8 + random.nextDouble() * 0.1;
            double dx = dirX * speed;
            double dz = dirZ * speed;
            double dy = (random.nextDouble() - 0.1) * 0.1;

            if (strikes.isEmpty()) {
                level.addParticle(ParticleTypes.POOF, px, py, pz, dx, dy, dz);
            } else {
                for (StrikeEnchantment strike : strikes) {
                    strike.addParticle(level, px, py, pz, random, dx, dy, dz);
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

    private static float getEffectiveDamage(SpellgemsConfig.NovaSpellConfig config, boolean hasPower) {
        return hasPower ? config.damage * config.powerDamageMultiplier : config.damage;
    }

    private static boolean shouldSkipTarget(LivingEntity target, LivingEntity caster) {
        return target == caster || target.isSpectator() || !target.isAlive();
    }
}