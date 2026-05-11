package net.pnovaczek.spellgems;


import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.spell.enchantment.UtilityEnchantment;
import net.pnovaczek.spellgems.spell.enchantment.UtilityEnchantments;

import java.util.HashMap;
import java.util.Map;

public class ModUtilityEnchantments {
    private static final Map<Identifier, UtilityEnchantment> REGISTRY = new HashMap<>();

    private ModUtilityEnchantments() {}

    public static void register(Identifier id, UtilityEnchantment modifier) {
        if (REGISTRY.containsKey(id)) {
            throw new IllegalArgumentException("Strike already registered: " + id);
        }
        REGISTRY.put(id, modifier);
    }

    public static UtilityEnchantment get(Identifier id) {
        return REGISTRY.get(id);
    }

    public static void initialize() {
        register(UtilityEnchantments.SMELT, new UtilityEnchantment(UtilityEnchantments.SMELT));
        register(UtilityEnchantments.SILK_TOUCH, new UtilityEnchantment(UtilityEnchantments.SILK_TOUCH));
    }
}
