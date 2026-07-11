package net.pnovaczek.spellgems.spell;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public abstract class AbstractSpell implements Spell {

    protected static void applyCastCooldown(SpellContext context, int ticks) {
        if (context.isWandCast()) {
            return;
        }
        if (!context.level().isClientSide() && context.caster() instanceof Player player) {
            player.getCooldowns().addCooldown(context.castingItem(), ticks);
        }
    }

    @Override
    public abstract Identifier id();

    @Override
    public final void cast(SpellContext context) {
        if (!context.caster().isAlive()) {
            return;
        }

        if (performCast(context)) {
            applyCastCooldown(context, getCooldownTicks());
        }
    }

    /**
     * Perform the spell's logic. Return true if the spell actually did something
     * meaningful (so that cooldown should be applied).
     */
    protected abstract boolean performCast(SpellContext context);

    protected int getCooldownTicks() {
        return 20;
    }

    /**
     * Helper for burst scheduling (used by spells with the BURST modifier).
     * Runs the first pulse immediately; schedules the rest with increasing delay.
     * Handles client vs server scheduling transparently.
     */
    protected void scheduleBurst(SpellContext context, int count, int tickSpacing, Runnable pulse) {
        scheduleBurst(context, count, tickSpacing, i -> pulse);
    }

    /**
     * General version allowing per-index pulse actions (useful when delayed pulses
     * need to capture current state like look direction at execution time).
     */
    protected void scheduleBurst(SpellContext context, int count, int tickSpacing,
                                 java.util.function.IntFunction<Runnable> pulseForIndex) {
        if (count <= 0) return;
        var level = context.level();
        boolean isClient = level.isClientSide();
        for (int i = 0; i < count; i++) {
            Runnable action = pulseForIndex.apply(i);
            if (action == null) continue;
            if (i == 0) {
                action.run();
            } else {
                int delayTicks = i * tickSpacing;
                if (isClient) {
                    SpellBurstScheduler.scheduleClient(level.getGameTime(), delayTicks, action);
                } else {
                    SpellBurstScheduler.scheduleServer(level.getServer().getTickCount(), delayTicks, action);
                }
            }
        }
    }

    @Override
    public boolean canCast(SpellContext context) {
        return true;
    }

    @Override
    public final String tooltipNameKey() {
        return "tooltip.spellgems.spell." + name() + ".name";
    }

    @Override
    public final  String tooltipDescriptionKey() {
        return "tooltip.spellgems.spell." + name() + ".description";
    }
}
