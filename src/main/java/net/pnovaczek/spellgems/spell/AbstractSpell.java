package net.pnovaczek.spellgems.spell;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public abstract class AbstractSpell implements Spell {

    protected static void applyCastCooldown(SpellContext context, int ticks) {
        if (context.isWandCast()) {
            return;
        }
        if (!context.level().isClientSide() && context.caster() instanceof Player player) {
            player.getCooldowns().addCooldown(context.castingItem(), ticks);
        }
    }

    @Override
    public abstract Identifier id();

    @Override
    public abstract void cast(SpellContext context);

    @Override
    public boolean canCast(SpellContext context) {
        return true;
    }

    @Override
    public final String tooltipNameKey() {
        return "tooltip.spellgems.spell." + name() + ".name";
    }

    @Override
    public final  String tooltipDescriptionKey() {
        return "tooltip.spellgems.spell." + name() + ".description";
    }
}
