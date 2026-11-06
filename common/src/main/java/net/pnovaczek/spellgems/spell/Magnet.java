package net.pnovaczek.spellgems.spell;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.spell.enchantment.UtilityEnchantment;
import net.pnovaczek.spellgems.spell.enchantment.UtilityEnchantments;

import java.util.List;

public class Magnet extends AbstractSpell {

    @Override
    public Identifier id() {
        return SpellIds.MAGNET;
    }

    @Override
    protected void performPredictedFx(SpellContext context) {
        // No local FX. The default would run the pull on the client.
    }

    @Override
    protected boolean performCast(SpellContext context) {
        var level = context.level();
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        float baseRange = Spellgems.CONFIG.spells.magnet.range;
        List<UtilityEnchantment> utilities = (context.data() != null) ? context.data().utilityEffects() : List.of();
        boolean hasExtend = utilities.stream().anyMatch(u -> u.is(UtilityEnchantments.EXTEND));
        float range = hasExtend
                ? (float) (baseRange * Spellgems.CONFIG.spells.magnet.extendMultiplier)
                : baseRange;

        Vec3 pullTarget = MagnetPull.target(context);
        Player pullPlayer = context.caster() instanceof Player player && !context.isDispenserCast() ? player : null;
        boolean pulledAny = MagnetPull.pullInRange(serverLevel, pullTarget, range, pullPlayer, pullPlayer != null);

        if (pulledAny) {
            SpellSounds.play(
                    context,
                    pullTarget,
                    SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS,
                    0.35F,
                    1.2F
            );
        }

        return true;
    }
}
