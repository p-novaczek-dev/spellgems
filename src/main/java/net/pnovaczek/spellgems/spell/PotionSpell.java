package net.pnovaczek.spellgems.spell;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.spell.enchantment.PotionEnchantment;

public class PotionSpell extends AbstractSpell {

    private static final int COOLDOWN_TICKS = 20;

    @Override
    public Identifier id() {
        return Spells.POTION;
    }

    @Override
    public boolean canCast(SpellContext context) {
        return context.caster().isAlive() && !context.data().potionEffects().isEmpty();
    }

    @Override
    public void cast(SpellContext context) {
        if (!context.caster().isAlive() || context.data().potionEffects().isEmpty()) {
            return;
        }

        if (context.level().isClientSide()) {
            for (PotionEnchantment enchantment : context.data().potionEffects()) {
                PotionDelivery.playClientEffects(context.level(), context.caster(), enchantment);
            }
            return;
        }

        if (!(context.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        for (PotionEnchantment enchantment : context.data().potionEffects()) {
            PotionDelivery.apply(serverLevel, context.caster(), enchantment);
        }

        if (context.caster() instanceof Player player) {
            player.getCooldowns().addCooldown(context.castingItem(), COOLDOWN_TICKS);
        }
    }
}