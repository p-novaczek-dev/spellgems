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
    DISPENSER,
    /** Nova (or other spell) triggered by hitting a living target with a socketed weapon. */
    WEAPON,
    /** Delayed second spell from a greater gem, centered on the struck target. */
    FOLLOW_UP;

    public boolean appliesPlayerItemCooldown() {
        return this == HAND;
    }

    public boolean isDispenser() {
        return this == DISPENSER;
    }

    public boolean isWand() {
        return this == WAND;
    }

    public boolean isFollowUp() {
        return this == FOLLOW_UP;
    }

    /** Area spells use {@link net.pnovaczek.spellgems.spell.SpellContext#origin()} instead of a look ray. */
    public boolean usesFixedOrigin() {
        return this == WEAPON || this == FOLLOW_UP;
    }

    /** Hand and wand spawn predicted particles locally; server skips the caster for those. */
    public boolean hasClientPrediction() {
        return this == HAND || this == WAND;
    }
}
