package net.pnovaczek.spellgems.spell.affix;

import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.SpellgemsConfig;
import net.pnovaczek.spellgems.spell.enchantment.SpellAffix;
import net.pnovaczek.spellgems.spell.enchantment.SpellAffixes;

import java.util.List;

/**
 * Deterministic affix XP. {@code cost(n) = base * (2^n - 1)} where each leveled rank counts as its own affix.
 */
public final class AffixCosts {

    private AffixCosts() {
    }

    public static int weight(List<SpellAffix> affixes) {
        if (affixes == null || affixes.isEmpty()) {
            return 0;
        }
        int weight = 0;
        for (SpellAffix affix : affixes) {
            if (!SpellAffixes.isKnown(affix.id())) {
                continue;
            }
            weight += SpellAffixes.isLeveled(affix.id()) ? affix.level() : 1;
        }
        return weight;
    }

    public static int cost(int weight) {
        long value = costLong(weight);
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }

    public static int charge(List<SpellAffix> before, List<SpellAffix> after) {
        return charge(weight(before), weight(after));
    }

    public static int charge(int oldWeight, int newWeight) {
        long delta = costLong(newWeight) - costLong(oldWeight);
        if (delta > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (delta < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) delta;
    }

    public static int scaleXp(int base, int xpBoostLevel) {
        if (base <= 0 || xpBoostLevel <= 0) {
            return Math.max(0, base);
        }
        double scaled = base * (1.0 + config().xpBoostPerLevel * xpBoostLevel);
        if (scaled >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) Math.round(scaled);
    }

    public static int levelOf(List<SpellAffix> affixes, Identifier id) {
        return SpellAffixes.level(affixes, id);
    }

    private static long costLong(int weight) {
        if (weight <= 0) {
            return 0L;
        }
        int base = Math.max(1, config().baseXpCost);
        if (weight >= 30) {
            return Integer.MAX_VALUE;
        }
        long value = (long) base * ((1L << weight) - 1L);
        return Math.min(value, Integer.MAX_VALUE);
    }

    private static SpellgemsConfig.AffixConfig config() {
        if (Spellgems.CONFIG == null || Spellgems.CONFIG.affixes == null) {
            return new SpellgemsConfig.AffixConfig();
        }
        return Spellgems.CONFIG.affixes;
    }
}
