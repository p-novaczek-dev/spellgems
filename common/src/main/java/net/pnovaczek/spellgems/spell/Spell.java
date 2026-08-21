package net.pnovaczek.spellgems.spell;

import net.minecraft.resources.Identifier;

public interface Spell {
    Identifier id();

    default String name() {
        return id().getPath();
    }

    /**
     * Authoritative cast (server). Applies world effects and hand-item cooldowns.
     * On the client this is a no-op for mutation; use {@link #castPredicted} for local FX.
     */
    void cast(SpellContext context);

    /**
     * Client-only predicted FX for responsive feel (wand left-click, optional hand use).
     * Must not mutate world state or apply cooldowns; server {@link #cast} is authoritative.
     * May desync briefly if the server later rejects the cast.
     */
    void castPredicted(SpellContext context);

    boolean canCast(SpellContext context);

    /**
     * Spells that only affect the caster (blink, drink potions).
     * Spell dispensers play FX only and skip world mutation for these.
     */
    default boolean isSelfTargeting(SpellContext context) {
        return false;
    }

    /**
     * When false, wand left-click casts once per press instead of repeating while the button is held.
     * Spell dispensers and quick-cast keys are unaffected.
     */
    default boolean repeatWhileHeld() {
        return true;
    }

    String tooltipNameKey();

    String tooltipDescriptionKey();
}
