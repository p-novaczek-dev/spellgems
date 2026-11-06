package net.pnovaczek.spellgems.spell.affix;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

/**
 * Spell damage that can carry affix credit through the hit, including a killing blow.
 */
public final class SpellDamage {

    private SpellDamage() {
    }

    public static boolean hurt(
            LivingEntity target,
            ServerLevel level,
            DamageSource source,
            float amount,
            @Nullable SpellKillCredit credit
    ) {
        if (!(target instanceof Player) && credit != null) {
            if (credit.active()) {
                AffixKills.record(target, credit);
            }
            // Magic damage has no causing entity. Vanilla drops experience only after a player hit.
            if (credit.player() != null) {
                target.setLastHurtByPlayer(credit.player(), 100);
            }
        }
        AffixKills.pushSpellDamage();
        try {
            return target.hurtServer(level, source, amount);
        } finally {
            AffixKills.popSpellDamage();
        }
    }
}
