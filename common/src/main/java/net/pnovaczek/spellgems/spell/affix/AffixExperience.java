package net.pnovaczek.spellgems.spell.affix;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gamerules.GameRules;
import net.pnovaczek.spellgems.spell.enchantment.SpellAffixes;
import org.jspecify.annotations.Nullable;

/**
 * Replaces vanilla experience for XP Boost and No XP, including dispenser kills that vanilla would skip.
 */
public final class AffixExperience {

    private AffixExperience() {
    }

    /**
     * @return true when vanilla {@code dropExperience} should be cancelled
     */
    public static boolean redirect(LivingEntity entity, ServerLevel level, @Nullable Entity killer) {
        if (entity instanceof Player) {
            return false;
        }
        SpellKillCredit credit = AffixKills.creditOf(entity);
        if (credit == null || !credit.active()) {
            return false;
        }
        boolean noXp = SpellAffixes.has(credit.affixes(), SpellAffixes.NO_XP);
        int boost = SpellAffixes.level(credit.affixes(), SpellAffixes.XP_BOOST);
        if (!noXp && boost <= 0) {
            AffixKills.scheduleMagnet(entity);
            return false;
        }
        if (entity.wasExperienceConsumed()
                || !entity.shouldDropExperience()
                || !level.getGameRules().get(GameRules.MOB_DROPS)) {
            AffixKills.scheduleMagnet(entity);
            return true;
        }
        if (!noXp) {
            int scaled = AffixCosts.scaleXp(entity.getExperienceReward(level, killer), boost);
            if (scaled > 0) {
                ExperienceOrb.award(level, entity.position(), scaled);
            }
        }
        AffixKills.scheduleMagnet(entity);
        return true;
    }
}
