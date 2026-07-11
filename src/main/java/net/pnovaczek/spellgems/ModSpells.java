package net.pnovaczek.spellgems;

import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.spell.*;

import java.util.Collection;
import java.util.Collections;
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

    public static Collection<Spell> getAllSpells() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    public static void initialize() {
        register(SpellIds.PROJECTILE, new Projectile());
        register(SpellIds.NOVA, new Nova());
        register(SpellIds.VORTEX, new Vortex());
        register(SpellIds.BLINK, new Blink());
        register(SpellIds.WIND_CHARGE, new WindChargeSpell());
        register(SpellIds.MAGNET, new Magnet());
        register(SpellIds.PLACE_BLOCK, new PlaceBlock());
        register(SpellIds.BREAK_BLOCK, new BreakBlock());
        register(SpellIds.PLANT, new Plant());
        register(SpellIds.HARVEST, new Harvest());
        register(SpellIds.FEED, new Feed());
        register(SpellIds.GROW, new Grow());
        register(SpellIds.POTION, new PotionSpell());
    }
}