package net.pnovaczek.spellgems.spell;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.entity.SpellProjectile;
import net.pnovaczek.spellgems.spell.enchantment.ModifierEnchantments;
import net.pnovaczek.spellgems.spell.enchantment.StrikeEnchantment;
import net.pnovaczek.spellgems.spell.enchantment.StrikeEnchantments;

import java.util.List;

public class Projectile extends AbstractSpell {

    @Override
    public Identifier id() {
        return Spells.PROJECTILE;
    }

    @Override
    public void cast(SpellContext context) {
        var level = context.level();
        var caster = context.caster();
        var data = context.data();
        var modifiers = data.modifierEffects();
        var strikes = data.strikeEffects();

        if (!level.isClientSide() && caster instanceof Player player) {
            var random = level.getRandom();
            var baseDirection = context.lookAngle();

            int shotCount = 1;
            boolean isBurst = false;
            boolean isMultishot = false;
            int chainCount = 0;

            for (var mod : modifiers) {
                if (mod.is(ModifierEnchantments.MULTISHOT)) {
                    isMultishot = true;
                    shotCount = 5;
                } else if (mod.is(ModifierEnchantments.BURST)) {
                    isBurst = true;
                    shotCount = 5;
                } else if (mod.is(ModifierEnchantments.CHAINING)) {
                    chainCount = 5;
                }
            }

            ProjectileHitHandler baseHandler = createHitHandler(context, strikes, chainCount);

            for (int i = 0; i < shotCount; i++) {
                Vec3 direction = baseDirection;

                if (isMultishot) {
                    float spreadAngle = 10.0F;
                    float angle = (i - (shotCount - 1) / 2.0F) * spreadAngle;
                    direction = baseDirection.yRot((float) Math.toRadians(angle));
                } else if (isBurst && i > 0) {
                    double spread = 0.03;
                    direction = baseDirection.add(
                            random.nextGaussian() * spread,
                            random.nextGaussian() * spread,
                            random.nextGaussian() * spread
                    ).normalize();
                }

                if (!isBurst || i == 0) {
                    spawnShot(context, direction, baseHandler, player, level, strikes);
                } else {
                    int delayTicks = i * 3;
                    SpellBurstScheduler.scheduleServer(level.getServer().getTickCount(), delayTicks, () -> {
                        if (!player.isAlive()) return;

                        Vec3 currentLook = player.getLookAngle();
                        Vec3 dir = currentLook.add(
                                level.getRandom().nextGaussian() * 0.03,
                                level.getRandom().nextGaussian() * 0.03,
                                level.getRandom().nextGaussian() * 0.03
                        ).normalize();

                        spawnShot(context, dir, baseHandler, player, level, strikes);
                    });
                }
            }

            applyCastCooldown(context, 20);
        }
    }

    private void spawnShot(SpellContext context, Vec3 direction, ProjectileHitHandler handler,
                           Player soundSource, Level level, List<StrikeEnchantment> strikes) {
        SpellProjectile projectile = new SpellProjectile(context, direction, handler);
        level.addFreshEntity(projectile);

        var sound = SoundEvents.BLAZE_SHOOT;
        float pitch = 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F);

        if (!strikes.isEmpty() && strikes.stream().anyMatch(s -> s.is(StrikeEnchantments.FROST))) {
            sound = SoundEvents.SNOWBALL_THROW;
            pitch = 1.0F;
        }

        level.playSound(
                null,
                soundSource.getX(), soundSource.getY(), soundSource.getZ(),
                sound,
                SoundSource.PLAYERS,
                0.5F,
                pitch
        );
    }

    private ProjectileHitHandler createHitHandler(SpellContext context, List<StrikeEnchantment> strikes, int maxChains) {
        return (projectile, result) -> {
            if (!(result.getEntity() instanceof LivingEntity living)) return;

            var lvl = projectile.level();
            if (!(lvl instanceof ServerLevel serverLevel)) return;

            var spellConfig = Spellgems.CONFIG.spells.projectile;

            living.hurtServer(serverLevel, projectile.damageSources().magic(), spellConfig.damage);

            for (var strike : strikes) {
                strike.applyTo(living, context.caster());
            }

            if (maxChains > 0) {
                var nearest = serverLevel.getNearestEntity(
                        LivingEntity.class,
                        TargetingConditions.forCombat().range(6.0).ignoreLineOfSight(),
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
                            createHitHandler(context, strikes, maxChains - 1)
                    );
                    lvl.addFreshEntity(chainProj);
                }
            }
        };
    }
}