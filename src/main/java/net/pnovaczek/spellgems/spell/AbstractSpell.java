package net.pnovaczek.spellgems.spell;

import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.SpellgemsConfig;

public abstract class AbstractSpell implements Spell {

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
