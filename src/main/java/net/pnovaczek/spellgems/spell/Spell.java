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

    /**
     * The suggested default durability cost when this spell is cast from a wand.
     * The actual cost is taken from config (WandConfig.spellCosts) and may be
     * overridden by the player or further multiplied by enchantments.
     */
    default int defaultDurabilityCost() {
        return 1;
    }
}
