package net.pnovaczek.spellgems.spell;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.spell.enchantment.PotionEnchantment;

public class PotionSpell extends AbstractSpell {

    @Override
    public Identifier id() {
        return SpellIds.POTION;
    }

    @Override
    public boolean canCast(SpellContext context) {
        return context.caster().isAlive() && !context.data().potionEffects().isEmpty();
    }

    @Override
    protected boolean performCast(SpellContext context) {
        if (!context.caster().isAlive() || context.data().potionEffects().isEmpty()) {
            return false;
        }

        if (context.level().isClientSide()) {
            for (PotionEnchantment enchantment : context.data().potionEffects()) {
                PotionDelivery.playClientEffects(context.level(), context.caster(), enchantment);
            }
            return false;
        }

        if (!(context.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        for (PotionEnchantment enchantment : context.data().potionEffects()) {
            PotionDelivery.apply(serverLevel, context.caster(), enchantment);
        }

        return true;
    }
}