package net.pnovaczek.spellgems.spell;

import net.minecraft.resources.Identifier;

public interface Spell {
    Identifier id();

    default String name() {
        return id().getPath();
    }

    void cast(SpellContext context);

    boolean canCast(SpellContext context);

    String tooltipNameKey();

    String tooltipDescriptionKey();
}
