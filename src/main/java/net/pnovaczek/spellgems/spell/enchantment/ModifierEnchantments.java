package net.pnovaczek.spellgems.spell.enchantment;

import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.spell.SpellIds;

import java.util.List;

public class ModifierEnchantments {
    private ModifierEnchantments() {}

    public static final Identifier CHAINING = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "chaining");
    public static final Identifier MULTISHOT = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "multishot");
    public static final Identifier PIERCING = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "piercing");
    public static final Identifier POWER = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "power");
    public static final Identifier BURST = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "burst");
    public static final Identifier EXPAND = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "expand");

    public static List<Identifier> getAll() {
        return List.of(CHAINING, MULTISHOT, PIERCING, POWER, BURST, EXPAND);
    }

    public static List<Identifier> getCompatible(Identifier spellId) {
        if (spellId.equals(SpellIds.PROJECTILE)) {
            return List.of(CHAINING, MULTISHOT, PIERCING, POWER, BURST);
        } else if (spellId.equals(SpellIds.NOVA)) {
            return List.of(POWER, BURST, EXPAND);
        } else if (spellId.equals(SpellIds.VORTEX)) {
            return List.of(BURST, EXPAND);
        }
        return List.of();
    }
}