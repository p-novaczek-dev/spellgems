package net.pnovaczek.spellgems.spell;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.SpellgemsConfig;
import net.pnovaczek.spellgems.entity.SpellProjectile;
import net.pnovaczek.spellgems.spell.enchantment.ModifierEnchantments;
import net.pnovaczek.spellgems.spell.enchantment.StrikeEnchantment;
import net.pnovaczek.spellgems.spell.enchantment.StrikeEnchantments;

import java.util.List;

public class Projectile extends AbstractSpell {

    @Override
    public Identifier id() {
        return SpellIds.PROJECTILE;
    }

    @Override
    protected boolean performCast(SpellContext context) {
        var level = context.level();
        var data = context.data();
        var modifiers = data.modifierEffects();
        var strikes = data.strikeEffects();

        if (level.isClientSide()) {
            return false;
        }

        var random = level.getRandom();
        var baseDirection = context.lookAngle();

        int shotCount = 1;
        boolean isBurst = false;
        boolean isMultishot = false;
        boolean hasPower = false;
        int chainCount = 0;
        int remainingSplits = 0;

        for (var mod : modifiers) {
            if (mod.is(ModifierEnchantments.MULTISHOT)) {
                isMultishot = true;
                shotCount = Spellgems.CONFIG.multishotCount;
            } else if (mod.is(ModifierEnchantments.BURST)) {
                isBurst = true;
                shotCount = 5;
            } else if (mod.is(ModifierEnchantments.CHAINING)) {
                chainCount = Spellgems.CONFIG.chainingCount;
            } else if (mod.is(ModifierEnchantments.POWER)) {
                hasPower = true;
            } else if (mod.is(ModifierEnchantments.SPLIT)) {
                remainingSplits = Spellgems.CONFIG.splitDepth;
            }
        }

        ProjectileHitHandler baseHandler = createHitHandler(context, strikes, chainCount, hasPower, remainingSplits);

        for (int i = 0; i < shotCount; i++) {
            Vec3 direction = baseDirection;

            if (isMultishot) {
                direction = horizontalSpread(baseDirection, i, shotCount, 10.0F);
            } else if (isBurst && i > 0) {
                double spread = 0.03;
                direction = baseDirection.add(
                        random.nextGaussian() * spread,
                        random.nextGaussian() * spread,
                        random.nextGaussian() * spread
                ).normalize();
            }

            if (!isBurst || i == 0) {
                spawnShot(context, direction, baseHandler, level, strikes);
            } else {
                int delayTicks = i * 3;
                SpellBurstScheduler.scheduleServer(level.getServer().getTickCount(), delayTicks, () -> {
                    if (context.caster() != null && !context.caster().isAlive()) {
                        return;
                    }

                    Vec3 currentLook = context.lookAngle();
                    if (context.caster() != null) {
                        currentLook = context.caster().getLookAngle();
                    }
                    Vec3 dir = currentLook.add(
                            level.getRandom().nextGaussian() * 0.03,
                            level.getRandom().nextGaussian() * 0.03,
                            level.getRandom().nextGaussian() * 0.03
                    ).normalize();

                    spawnShot(context, dir, baseHandler, level, strikes);
                });
            }
        }

        return true;
    }

    private void spawnShot(
            SpellContext context,
            Vec3 direction,
            ProjectileHitHandler handler,
            Level level,
            List<StrikeEnchantment> strikes
    ) {
        SpellProjectile projectile = new SpellProjectile(context, direction, handler);
        level.addFreshEntity(projectile);

        var sound = SoundEvents.ENDER_DRAGON_SHOOT;
        float pitch = 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F);

        Vec3 soundPos = context.eyeOrigin();
        level.playSound(
                null,
                soundPos.x, soundPos.y, soundPos.z,
                sound,
                SoundSource.PLAYERS,
                0.5F,
                pitch
        );
    }

    private ProjectileHitHandler createHitHandler(
            SpellContext context,
            List<StrikeEnchantment> strikes,
            int maxChains,
            boolean hasPower,
            int remainingSplits
    ) {
        return (projectile, result) -> {
            if (!(result.getEntity() instanceof LivingEntity living)) return;

            var lvl = projectile.level();
            if (!(lvl instanceof ServerLevel serverLevel)) return;

            var spellConfig = Spellgems.CONFIG.spells.projectile;
            float damage = getEffectiveDamage(spellConfig, hasPower);

            living.hurtServer(serverLevel, projectile.damageSources().magic(), damage);

            LivingEntity strikeSource = context.caster() != null ? context.caster() : living;
            for (var strike : strikes) {
                strike.applyTo(living, strikeSource);
            }

            if (remainingSplits > 0) {
                spawnSplitProjectiles(context, projectile, living, strikes, maxChains, hasPower, remainingSplits - 1);
            }

            if (maxChains > 0) {
                LivingEntity caster = context.caster();
                var conditions = TargetingConditions.forCombat()
                        .range(6.0)
                        .ignoreLineOfSight();
                if (caster != null) {
                    conditions.selector((candidate, ignored) -> candidate != caster);
                }

                var nearest = serverLevel.getNearestEntity(
                        LivingEntity.class,
                        conditions,
                        living,
                        living.getX(),
                        living.getY(),
                        living.getZ(),
                        living.getBoundingBox().inflate(6.0)
                );

                if (nearest != null && nearest != living && nearest.distanceTo(living) < 6.0) {
                    Vec3 newDir = nearest.position().add(0, nearest.getEyeHeight() * 0.6, 0)
                            .subtract(projectile.position()).normalize();

                    SpellProjectile chainProj = new SpellProjectile(
                            context, newDir, projectile.position(),
                            createHitHandler(context, strikes, maxChains - 1, hasPower, remainingSplits)
                    );
                    lvl.addFreshEntity(chainProj);
                }
            }
        };
    }

    private void spawnSplitProjectiles(
            SpellContext context,
            SpellProjectile source,
            LivingEntity hitTarget,
            List<StrikeEnchantment> strikes,
            int maxChains,
            boolean hasPower,
            int remainingSplits
    ) {
        Vec3 movement = source.getDeltaMovement();
        Vec3 baseDirection = movement.lengthSqr() < 1.0E-6
                ? context.lookAngle()
                : movement.normalize();

        int count = Spellgems.CONFIG.splitCount;
        ProjectileHitHandler childHandler = createHitHandler(context, strikes, maxChains, hasPower, remainingSplits);

        for (int i = 0; i < count; i++) {
            Vec3 direction = horizontalSpread(baseDirection, i, count, 20.0F);
            SpellProjectile split = new SpellProjectile(context, direction, source.position(), childHandler);
            split.ignoreHitEntity(hitTarget);
            source.level().addFreshEntity(split);
        }
    }

    private static Vec3 horizontalSpread(Vec3 baseDirection, int index, int count, float spreadAngle) {
        float angle = (index - (count - 1) / 2.0F) * spreadAngle;
        return baseDirection.yRot((float) Math.toRadians(angle));
    }

    private static float getEffectiveDamage(SpellgemsConfig.SpellCombatConfig config, boolean hasPower) {
        return hasPower ? config.damage * config.powerDamageMultiplier : config.damage;
    }
}
