package net.pnovaczek.spellgems;

import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.spell.enchantment.ModifierEnchantment;
import net.pnovaczek.spellgems.spell.enchantment.ModifierEnchantments;

import java.util.HashMap;
import java.util.Map;

public final class ModModifierEnchantments {

    private static final Map<Identifier, ModifierEnchantment> REGISTRY = new HashMap<>();

    private ModModifierEnchantments() {}

    public static void register(Identifier id, ModifierEnchantment modifier) {
        if (REGISTRY.containsKey(id)) {
            throw new IllegalArgumentException("Modifier already registered: " + id);
        }
        REGISTRY.put(id, modifier);
    }

    public static ModifierEnchantment get(Identifier id) {
        return REGISTRY.get(id);
    }

    public static void initialize() {
        register(ModifierEnchantments.CHAINING, new ModifierEnchantment(ModifierEnchantments.CHAINING));
        register(ModifierEnchantments.MULTISHOT, new ModifierEnchantment(ModifierEnchantments.MULTISHOT));
        register(ModifierEnchantments.PIERCING, new ModifierEnchantment(ModifierEnchantments.PIERCING));
        register(ModifierEnchantments.POWER, new ModifierEnchantment(ModifierEnchantments.POWER));
        register(ModifierEnchantments.BURST, new ModifierEnchantment(ModifierEnchantments.BURST));
        register(ModifierEnchantments.EXPAND, new ModifierEnchantment(ModifierEnchantments.EXPAND));
    }
}

