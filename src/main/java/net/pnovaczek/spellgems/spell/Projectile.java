package net.pnovaczek.spellgems.spell;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.pnovaczek.spellgems.entity.SpellProjectile;

public class Projectile extends AbstractSpell {

    @Override
    public Identifier id() {
        return Spells.PROJECTILE;
    }

    @Override
    public void cast(SpellContext context) {
        var level = context.level();
        var caster = context.caster();
        var castingItem = context.castingItem();
        var direction = context.lookAngle();
        var data = context.data();

        if (!level.isClientSide()) {
            // hard coded for now
            if (caster instanceof Player player){
                SpellProjectile projectile = new SpellProjectile(context);
                level.addFreshEntity(projectile);
                level.playSound(
                        null,
                        player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLAZE_SHOOT,
                        SoundSource.PLAYERS,
                        0.5F,
                        0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
                );
                player.getCooldowns().addCooldown(castingItem, 20);
            }
        }
    }
}
