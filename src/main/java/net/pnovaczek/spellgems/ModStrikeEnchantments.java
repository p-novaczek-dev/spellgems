package net.pnovaczek.spellgems;

import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.spell.enchantment.StrikeEnchantment;
import net.pnovaczek.spellgems.spell.enchantment.StrikeEnchantments;

import java.util.HashMap;
import java.util.Map;

public class ModStrikeEnchantments {
    private static final Map<Identifier, StrikeEnchantment> REGISTRY = new HashMap<>();

    private ModStrikeEnchantments() {}

    public static void register(Identifier id, StrikeEnchantment strike) {
        if (REGISTRY.containsKey(id)) {
            throw new IllegalArgumentException("Strike already registered: " + id);
        }
        REGISTRY.put(id, strike);
    }

    public static StrikeEnchantment get(Identifier id) {
        return REGISTRY.get(id);
    }

    public static void initialize() {
        register(StrikeEnchantments.FLAME, new StrikeEnchantment(StrikeEnchantments.FLAME));
        register(StrikeEnchantments.POISON, new StrikeEnchantment(StrikeEnchantments.POISON));
        register(StrikeEnchantments.FROST, new StrikeEnchantment(StrikeEnchantments.FROST));
        register(StrikeEnchantments.SLOW, new StrikeEnchantment(StrikeEnchantments.SLOW));
        register(StrikeEnchantments.LEVITATE, new StrikeEnchantment(StrikeEnchantments.LEVITATE));
        register(StrikeEnchantments.INFERNO, new StrikeEnchantment(StrikeEnchantments.INFERNO));
        register(StrikeEnchantments.FROSTBITE, new StrikeEnchantment(StrikeEnchantments.FROSTBITE));
        register(StrikeEnchantments.PLAGUE, new StrikeEnchantment(StrikeEnchantments.PLAGUE));
        register(StrikeEnchantments.LIGHTNING, new StrikeEnchantment(StrikeEnchantments.LIGHTNING));
        register(StrikeEnchantments.EXPLOSION, new StrikeEnchantment(StrikeEnchantments.EXPLOSION));
        register(StrikeEnchantments.DRAIN, new StrikeEnchantment(StrikeEnchantments.DRAIN));
        register(StrikeEnchantments.THERMAL_INVERSION, new StrikeEnchantment(StrikeEnchantments.THERMAL_INVERSION));
        register(StrikeEnchantments.PURIFY, new StrikeEnchantment(StrikeEnchantments.PURIFY));
        register(StrikeEnchantments.VOLLEY, new StrikeEnchantment(StrikeEnchantments.VOLLEY));
        register(StrikeEnchantments.VENGEANCE, new StrikeEnchantment(StrikeEnchantments.VENGEANCE));
    }
}
