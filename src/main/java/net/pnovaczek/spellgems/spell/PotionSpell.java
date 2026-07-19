package net.pnovaczek.spellgems.spell;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.pnovaczek.spellgems.spell.enchantment.PotionDeliveryType;
import net.pnovaczek.spellgems.spell.enchantment.PotionEnchantment;

public class PotionSpell extends AbstractSpell {

    @Override
    public Identifier id() {
        return SpellIds.POTION;
    }

    @Override
    public boolean canCast(SpellContext context) {
        return (context.caster() == null || context.caster().isAlive())
                && context.data() != null
                && !context.data().potionEffects().isEmpty();
    }

    @Override
    public boolean isSelfTargeting(SpellContext context) {
        if (context.data() == null || context.data().potionEffects().isEmpty()) {
            return false;
        }
        // Drink-only potions only affect the caster; splash/lingering apply at origin.
        return context.data().potionEffects().stream()
                .allMatch(e -> e.delivery() == PotionDeliveryType.DRINK);
    }

    @Override
    protected void performSelfTargetDispenserFx(SpellContext context) {
        for (PotionEnchantment enchantment : context.data().potionEffects()) {
            PotionDelivery.playEffectsAt(context.level(), context.origin(), enchantment);
        }
    }

    @Override
    protected boolean performCast(SpellContext context) {
        if (context.data() == null || context.data().potionEffects().isEmpty()) {
            return false;
        }
        if (context.caster() != null && !context.caster().isAlive()) {
            return false;
        }

        if (context.level().isClientSide()) {
            for (PotionEnchantment enchantment : context.data().potionEffects()) {
                PotionDelivery.playClientEffects(context, enchantment);
            }
            return false;
        }

        if (!(context.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        for (PotionEnchantment enchantment : context.data().potionEffects()) {
            PotionDelivery.apply(serverLevel, context, enchantment);
        }

        return true;
    }
}
