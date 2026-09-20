package net.pnovaczek.spellgems.spell.enchantment;

import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.Spellgems;

import java.util.List;

public class StrikeEnchantments {
    private StrikeEnchantments() {}

    public static final Identifier FLAME = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "flame");
    public static final Identifier POISON = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "poison");
    public static final Identifier FROST = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "frost");
    public static final Identifier SLOW = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "slow");
    public static final Identifier LEVITATE = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "levitate");
    public static final Identifier WEAKEN = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "weaken");
    public static final Identifier GLOW = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "glow");
    public static final Identifier SHATTER = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "shatter");
    public static final Identifier COMBUST = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "combust");
    public static final Identifier JUDGEMENT = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "judgement");
    public static final Identifier ROOT = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "root");
    public static final Identifier VAMPIRIC_MIST = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "vampiric_mist");
    public static final Identifier INFERNO = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "inferno");
    public static final Identifier FROSTBITE = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "frostbite");
    public static final Identifier PLAGUE = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "plague");
    public static final Identifier LIGHTNING = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "lightning");
    public static final Identifier EXPLOSION = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "explosion");
    public static final Identifier DRAIN = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "drain");
    public static final Identifier PURIFY = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "purify");
    public static final Identifier VOLLEY = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "volley");
    public static final Identifier VENGEANCE = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "vengeance");
    public static final Identifier WIND_CHARGE = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "wind_charge");

    public static List<Identifier> getAll() {
        return List.of(
                FLAME, POISON, FROST, SLOW, LEVITATE, WEAKEN, GLOW, SHATTER, COMBUST, JUDGEMENT, ROOT, VAMPIRIC_MIST, INFERNO, FROSTBITE, PLAGUE,
                LIGHTNING, EXPLOSION, DRAIN, PURIFY, VOLLEY, VENGEANCE, WIND_CHARGE
        );
    }
}
