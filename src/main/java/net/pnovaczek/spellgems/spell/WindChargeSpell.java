package net.pnovaczek.spellgems.spell;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge;
import net.minecraft.world.item.WindChargeItem;
import net.minecraft.world.phys.Vec3;

public class WindChargeSpell extends AbstractSpell {

    @Override
    public Identifier id() {
        return SpellIds.WIND_CHARGE;
    }

    @Override
    protected int getCooldownTicks() {
        return 10;
    }

    @Override
    protected boolean performCast(SpellContext context) {
        var level = context.level();
        var caster = context.caster();
        Vec3 origin = context.eyeOrigin();
        Vec3 look = context.lookAngle();

        if (level instanceof ServerLevel serverLevel) {
            if (caster != null) {
                Projectile.spawnProjectileFromRotation(
                        (server, source, stack) -> createWindCharge(server, source, context),
                        serverLevel,
                        context.castingItem(),
                        caster,
                        0.0F,
                        WindChargeItem.PROJECTILE_SHOOT_POWER,
                        1.0F
                );
            } else {
                WindCharge windCharge = new WindCharge(
                        serverLevel,
                        origin.x,
                        origin.y,
                        origin.z,
                        look
                );
                windCharge.shoot(look.x, look.y, look.z, WindChargeItem.PROJECTILE_SHOOT_POWER, 1.0F);
                serverLevel.addFreshEntity(windCharge);
            }
        }

        level.playSound(
                null,
                origin.x,
                origin.y,
                origin.z,
                SoundEvents.WIND_CHARGE_THROW,
                SoundSource.NEUTRAL,
                0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
        );

        return true;
    }

    private static WindCharge createWindCharge(ServerLevel level, LivingEntity source, SpellContext context) {
        if (source instanceof Player player) {
            return new WindCharge(
                    player,
                    level,
                    player.position().x(),
                    player.getEyePosition().y(),
                    player.position().z()
            );
        }

        Vec3 origin = context.eyeOrigin();
        WindCharge windCharge = new WindCharge(
                level,
                origin.x,
                origin.y,
                origin.z,
                context.lookAngle()
        );
        windCharge.setOwner(source);
        return windCharge;
    }
}
