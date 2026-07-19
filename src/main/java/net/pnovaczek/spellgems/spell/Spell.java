package net.pnovaczek.spellgems.spell;

import net.minecraft.resources.Identifier;

public interface Spell {
    Identifier id();

    default String name() {
        return id().getPath();
    }

    void cast(SpellContext context);

    boolean canCast(SpellContext context);

    /**
     * Spells that only affect the caster (blink, drink potions).
     * Spell dispensers play FX only and skip world mutation for these.
     */
    default boolean isSelfTargeting(SpellContext context) {
        return false;
    }

    String tooltipNameKey();

    String tooltipDescriptionKey();
}
