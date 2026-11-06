package net.pnovaczek.spellgems.spell.enchantment;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.SpellgemsConfig;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility affixes for combat spell gems. Visual and proc affixes are not registered;
 * {@link Kind} leaves a place for them.
 */
public final class SpellAffixes {

    public enum Kind {
        UTILITY
    }

    public record Definition(
            Identifier id,
            Kind kind,
            boolean leveled,
            @Nullable Identifier exclusiveWith,
            int order
    ) {
        public String nameKey() {
            return "tooltip.spellgems.spell_affix." + id.getPath() + ".name";
        }

        public String descriptionKey() {
            return "tooltip.spellgems.spell_affix." + id.getPath() + ".description";
        }
    }

    public static final Identifier LOOTING = id("looting");
    public static final Identifier NO_LOOT = id("no_loot");
    public static final Identifier XP_BOOST = id("xp_boost");
    public static final Identifier NO_XP = id("no_xp");
    public static final Identifier DECAPITATE = id("decapitate");
    public static final Identifier SCAVENGE = id("scavenge");
    public static final Identifier MAGNET = id("magnet");

    private static final List<Definition> DEFINITIONS = List.of(
            def(LOOTING, true, NO_LOOT, 0),
            def(NO_LOOT, false, LOOTING, 1),
            def(XP_BOOST, true, NO_XP, 2),
            def(NO_XP, false, XP_BOOST, 3),
            def(DECAPITATE, false, null, 4),
            def(SCAVENGE, false, null, 5),
            def(MAGNET, false, null, 6)
    );

    private static final Map<Identifier, Definition> BY_ID = index();

    private SpellAffixes() {
    }

    public static List<Definition> utility() {
        return DEFINITIONS;
    }

    public static @Nullable Definition get(Identifier id) {
        return BY_ID.get(id);
    }

    public static boolean isKnown(Identifier id) {
        return BY_ID.containsKey(id);
    }

    public static boolean isLeveled(Identifier id) {
        Definition definition = BY_ID.get(id);
        return definition != null && definition.leveled();
    }

    public static int maxLevel(Identifier id) {
        Definition definition = BY_ID.get(id);
        if (definition == null) {
            return 1;
        }
        if (!definition.leveled()) {
            return 1;
        }
        SpellgemsConfig.AffixConfig config = config();
        if (LOOTING.equals(id)) {
            return Math.max(1, config.maxLootingLevel);
        }
        if (XP_BOOST.equals(id)) {
            return Math.max(1, config.maxXpBoostLevel);
        }
        return 1;
    }

    public static @Nullable Identifier exclusivePartner(Identifier id) {
        Definition definition = BY_ID.get(id);
        return definition == null ? null : definition.exclusiveWith();
    }

    public static int displayIndex(Identifier id) {
        Definition definition = BY_ID.get(id);
        return definition == null ? Integer.MAX_VALUE : definition.order();
    }

    public static int level(List<SpellAffix> affixes, Identifier id) {
        if (affixes == null) {
            return 0;
        }
        for (SpellAffix affix : affixes) {
            if (affix.is(id)) {
                return affix.level();
            }
        }
        return 0;
    }

    public static boolean has(List<SpellAffix> affixes, Identifier id) {
        return level(affixes, id) > 0;
    }

    public static MutableComponent displayName(SpellAffix affix) {
        Definition definition = BY_ID.get(affix.id());
        String key = definition != null
                ? definition.nameKey()
                : "tooltip.spellgems.spell_affix." + affix.id().getPath() + ".name";
        MutableComponent name = Component.translatable(key).withStyle(ChatFormatting.GOLD);
        if (definition != null && definition.leveled()) {
            name.append(Component.literal(" ").withStyle(ChatFormatting.GOLD));
            name.append(Component.translatable("enchantment.level." + affix.level()).withStyle(ChatFormatting.GOLD));
        }
        return name;
    }

    public static @Nullable String descriptionKey(Identifier id) {
        Definition definition = BY_ID.get(id);
        return definition == null ? null : definition.descriptionKey();
    }

    public static List<SpellAffix> inDisplayOrder(Map<Identifier, Integer> levels) {
        List<SpellAffix> ordered = new ArrayList<>();
        for (Definition definition : DEFINITIONS) {
            Integer level = levels.get(definition.id());
            if (level != null && level > 0) {
                ordered.add(new SpellAffix(definition.id(), level));
            }
        }
        for (Map.Entry<Identifier, Integer> entry : levels.entrySet()) {
            if (BY_ID.containsKey(entry.getKey()) || entry.getValue() == null || entry.getValue() <= 0) {
                continue;
            }
            ordered.add(new SpellAffix(entry.getKey(), entry.getValue()));
        }
        return List.copyOf(ordered);
    }

    public static Map<Identifier, Integer> asMap(List<SpellAffix> affixes) {
        Map<Identifier, Integer> levels = new LinkedHashMap<>();
        if (affixes == null) {
            return levels;
        }
        for (SpellAffix affix : affixes) {
            levels.put(affix.id(), affix.level());
        }
        return levels;
    }

    public static boolean same(List<SpellAffix> left, List<SpellAffix> right) {
        return asMap(left).equals(asMap(right));
    }

    private static SpellgemsConfig.AffixConfig config() {
        if (Spellgems.CONFIG == null || Spellgems.CONFIG.affixes == null) {
            return new SpellgemsConfig.AffixConfig();
        }
        return Spellgems.CONFIG.affixes;
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, path);
    }

    private static Definition def(Identifier id, boolean leveled, @Nullable Identifier exclusiveWith, int order) {
        return new Definition(id, Kind.UTILITY, leveled, exclusiveWith, order);
    }

    private static Map<Identifier, Definition> index() {
        Map<Identifier, Definition> map = new LinkedHashMap<>();
        for (Definition definition : DEFINITIONS) {
            map.put(definition.id(), definition);
        }
        return Map.copyOf(map);
    }
}
