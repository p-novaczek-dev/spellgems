package net.pnovaczek.spellgems.spell;

/**
 * Where a spell cast originated. Controls cooldown ownership and dispenser-specific rules.
 */
public enum CastSource {
    /** Casting a spell gem from the hotbar / hand. Applies vanilla item cooldowns. */
    HAND,
    /** Casting from a wand. Durability cost is handled by the wand; no item cooldown. */
    WAND,
    /** Casting from a spell dispenser block. Cooldown/burnout owned by the block entity. */
    DISPENSER;

    public boolean appliesPlayerItemCooldown() {
        return this == HAND;
    }

    public boolean isDispenser() {
        return this == DISPENSER;
    }

    public boolean isWand() {
        return this == WAND;
    }
}
