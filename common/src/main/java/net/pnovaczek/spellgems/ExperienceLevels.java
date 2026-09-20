package net.pnovaczek.spellgems;

import net.minecraft.world.entity.player.Player;

import java.util.Locale;

/**
 * Converts an XP-point cost to a vanilla level amount, using the same per-level
 * curve as {@link Player#getXpNeededForNextLevel()}.
 * <p>
 * If the player cannot afford the cost, this is the level equivalent of that XP
 * from 0 (e.g. 1395 → 30). If they can, it is how many levels they would lose
 * from their current total.
 */
public final class ExperienceLevels {

    private ExperienceLevels() {
    }

    public static int xpNeededForNextLevel(int experienceLevel) {
        if (experienceLevel >= 30) {
            return 112 + (experienceLevel - 30) * 9;
        }
        return experienceLevel >= 15 ? 37 + (experienceLevel - 15) * 5 : 7 + experienceLevel * 2;
    }

    public static double levelsForXp(int xp) {
        if (xp <= 0) {
            return 0.0;
        }
        int remaining = xp;
        int level = 0;
        while (true) {
            int needed = xpNeededForNextLevel(level);
            if (remaining < needed) {
                return level + (double) remaining / needed;
            }
            remaining -= needed;
            level++;
        }
    }

    public static double relativeLevelsForXpCost(int xpCost, Player player) {
        int total = player.totalExperience;
        if (total < xpCost) {
            return levelsForXp(xpCost);
        }
        return levelsForXp(total) - levelsForXp(total - xpCost);
    }

    public static String formatLevelsForXpCost(int xpCost, Player player) {
        return String.format(Locale.ROOT, "%.1f", relativeLevelsForXpCost(xpCost, player));
    }
}
