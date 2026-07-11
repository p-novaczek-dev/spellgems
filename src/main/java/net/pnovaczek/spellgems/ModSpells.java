package net.pnovaczek.spellgems;

import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.spell.*;

import java.util.HashMap;
import java.util.Map;

public final class ModSpells {

    private static final Map<Identifier, Spell> REGISTRY = new HashMap<>();

    private ModSpells() {}

    public static void register(Identifier id, Spell spell) {
        if (REGISTRY.containsKey(id)) {
            throw new IllegalArgumentException("Spell already registered: " + id);
        }
        REGISTRY.put(id, spell);
    }

    public static Spell get(Identifier id) {
        return REGISTRY.get(id);
    }

    public static void initialize() {
        register(Spells.PROJECTILE, new Projectile());
        register(Spells.NOVA, new Nova());
        register(Spells.VORTEX, new Vortex());
        register(Spells.BLINK, new Blink());
        register(Spells.WIND_CHARGE, new WindChargeSpell());
        register(Spells.MAGNET, new Magnet());
        register(Spells.PLACE_BLOCK, new PlaceBlock());
        register(Spells.BREAK_BLOCK, new BreakBlock());
        register(Spells.PLANT, new Plant());
        register(Spells.HARVEST, new Harvest());
        register(Spells.FEED, new Feed());
        register(Spells.GROW, new Grow());
        register(Spells.POTION, new PotionSpell());
    }
}