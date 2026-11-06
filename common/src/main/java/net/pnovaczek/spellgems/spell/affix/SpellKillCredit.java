package net.pnovaczek.spellgems.spell.affix;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.pnovaczek.spellgems.spell.enchantment.SpellAffix;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Affixes from the cast that last hurt a mob, plus where a magnet affix should pull the drops.
 */
public record SpellKillCredit(
        List<SpellAffix> affixes,
        @Nullable Player player,
        Vec3 pullPosition,
        boolean dispenser
) {
    public static final SpellKillCredit NONE = new SpellKillCredit(List.of(), null, Vec3.ZERO, false);

    public SpellKillCredit {
        affixes = affixes == null ? List.of() : List.copyOf(affixes);
        if (pullPosition == null) {
            pullPosition = Vec3.ZERO;
        }
    }

    public boolean active() {
        return !affixes.isEmpty();
    }
}
