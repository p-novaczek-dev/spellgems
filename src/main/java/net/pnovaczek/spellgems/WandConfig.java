package net.pnovaczek.spellgems;

import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuration for wand behavior.
 */
public class WandConfig {

    public float spellEnchantmentDurabilityCostMultiplier = 6.0F;

    /**
     * User overrides for wand spell durability costs.
     *
     * Key = full spell Identifier as string (e.g. "spellgems:projectile").
     * If a key is absent, the cost declared by the Spell implementation
     * (via defaultDurabilityCost()) is used.
     *
     * This makes costs fully driven by Identifier and easy to extend.
     */
    public Map<String, Integer> spellCosts = new LinkedHashMap<>();

    public WandConfig() {
        // Starts empty. Defaults come from the Spell objects themselves.
        // We populate entries for discoverability after ModSpells is initialized.
    }

    public void validate() {
        spellEnchantmentDurabilityCostMultiplier = Math.max(1f, spellEnchantmentDurabilityCostMultiplier);

        // Clamp individual costs in the map if present
        if (spellCosts != null) {
            for (var entry : spellCosts.entrySet()) {
                if (entry.getValue() != null) {
                    spellCosts.put(entry.getKey(), Math.max(1, entry.getValue()));
                }
            }
        }
    }

    /**
     * Returns the effective durability cost for a spell.
     * 1. User-configured value in this map (if present)
     * 2. The value declared by the Spell via defaultDurabilityCost()
     * 3. 1 as last resort
     */
    public int getSpellCost(Identifier spellId) {
        String key = spellId.toString();
        Integer configured = spellCosts.get(key);
        if (configured != null) {
            return configured;
        }

        var spell = ModSpells.get(spellId);
        if (spell != null) {
            return spell.defaultDurabilityCost();
        }
        return 1;
    }

    /**
     * Seeds the map with the default cost declared by every currently registered spell.
     * This is called after ModSpells.initialize() so that the generated/loaded
     * config file contains useful entries.
     */
    public void seedDefaultsFromRegisteredSpells() {
        for (var spell : ModSpells.getAllSpells()) {
            String key = spell.id().toString();
            spellCosts.putIfAbsent(key, spell.defaultDurabilityCost());
        }
    }
}