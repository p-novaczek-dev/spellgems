package net.pnovaczek.spellgems.spell;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge;
import net.minecraft.world.item.WindChargeItem;

public class WindChargeSpell extends AbstractSpell {

    private static final int COOLDOWN_TICKS = 10;

    @Override
    public Identifier id() {
        return Spells.WIND_CHARGE;
    }

    @Override
    public void cast(SpellContext context) {
        var level = context.level();
        var caster = context.caster();
        if (!caster.isAlive()) {
            return;
        }

        if (level instanceof ServerLevel serverLevel) {
            Projectile.spawnProjectileFromRotation(
                    (server, source, stack) -> createWindCharge(server, source),
                    serverLevel,
                    context.castingItem(),
                    caster,
                    0.0F,
                    WindChargeItem.PROJECTILE_SHOOT_POWER,
                    1.0F
            );
        }

        level.playSound(
                null,
                caster.getX(),
                caster.getY(),
                caster.getZ(),
                SoundEvents.WIND_CHARGE_THROW,
                SoundSource.NEUTRAL,
                0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
        );

        applyCastCooldown(context, COOLDOWN_TICKS);
    }

    private static WindCharge createWindCharge(ServerLevel level, net.minecraft.world.entity.LivingEntity source) {
        if (source instanceof Player player) {
            return new WindCharge(
                    player,
                    level,
                    player.position().x(),
                    player.getEyePosition().y(),
                    player.position().z()
            );
        }

        WindCharge windCharge = new WindCharge(
                level,
                source.getX(),
                source.getEyeY(),
                source.getZ(),
                source.getLookAngle()
        );
        windCharge.setOwner(source);
        return windCharge;
    }
}