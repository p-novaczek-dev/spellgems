package net.pnovaczek.spellgems.spell;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractSpell implements Spell {

    protected static void applyCastCooldown(SpellContext context, int ticks) {
        if (!context.appliesPlayerItemCooldown()) {
            return;
        }
        if (!context.level().isClientSide() && context.caster() instanceof Player player) {
            player.getCooldowns().addCooldown(context.castingItem(), ticks);
        }
    }

    @Override
    public abstract Identifier id();

    /**
     * Server-authoritative cast. Client calls are ignored so prediction never runs the
     * full mutation path by accident (use {@link #castPredicted} on the client).
     */
    @Override
    public final void cast(SpellContext context) {
        if (context.level().isClientSide()) {
            return;
        }
        if (context.caster() != null && !context.caster().isAlive()) {
            return;
        }
        // Dispenser self-target: FX only (blink, drink potions). No world mutation.
        if (context.isDispenserCast() && isSelfTargeting(context)) {
            performSelfTargetDispenserFx(context);
            return;
        }

        if (performCast(context)) {
            applyCastCooldown(context, getCooldownTicks());
        }
    }

    /**
     * Client prediction: particles/sounds only. Does not apply cooldowns or run if not client.
     */
    @Override
    public final void castPredicted(SpellContext context) {
        if (!context.level().isClientSide()) {
            return;
        }
        if (context.caster() != null && !context.caster().isAlive()) {
            return;
        }
        performPredictedFx(context);
    }

    /**
     * Perform the spell's logic. Return true if the spell actually did something
     * meaningful (so that cooldown should be applied). Called on the server only
     * from {@link #cast}, except subclasses may call client branches from
     * {@link #performPredictedFx}.
     */
    protected abstract boolean performCast(SpellContext context);

    /**
     * Client-only predicted feedback. Default runs {@link #performCast}, which existing
     * spells already gate with {@code isClientSide()} for particles/sounds only.
     * Override to supply lighter FX without reusing full cast logic.
     */
    protected void performPredictedFx(SpellContext context) {
        performCast(context);
    }

    /**
     * Client/server FX when a self-targeting spell is cast from a dispenser.
     * Override when particles/sounds should still play; default is no-op.
     */
    protected void performSelfTargetDispenserFx(SpellContext context) {
    }

    protected int getCooldownTicks() {
        return 20;
    }

    /**
     * Helper for burst scheduling (used by spells with the BURST modifier).
     * Runs the first pulse immediately; schedules the rest with increasing delay.
     * Handles client vs server scheduling transparently.
     * Delayed pulses should re-check caster/level validity when they run.
     */
    protected void scheduleBurst(SpellContext context, int count, int tickSpacing, Runnable pulse) {
        scheduleBurst(context, count, tickSpacing, i -> pulse);
    }

    /**
     * General version allowing per-index pulse actions (useful when delayed pulses
     * need to capture current state like look direction at execution time).
     * Delayed pulses should re-check caster/level validity when they run.
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
                } else if (level.getServer() != null) {
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
    public final String tooltipDescriptionKey() {
        return "tooltip.spellgems.spell." + name() + ".description";
    }

    /** Default gray dust color used by sphere-style spell particles when no custom tint is set. */
    static final int DEFAULT_DUST_COLOR = 0x888888;

    /**
     * Returns a uniformly random point inside a sphere of the given radius centered at 'center'.
     * Uses volume-uniform sampling (cube root of radius).
     */
    static Vec3 randomPointInSphere(Vec3 center, float radius, net.minecraft.util.RandomSource random) {
        double theta = Math.PI * 2 * random.nextDouble();
        double phi = Math.acos(2 * random.nextDouble() - 1);
        double r = radius * Math.cbrt(random.nextDouble());
        double sinPhi = Math.sin(phi);

        return center.add(
                r * sinPhi * Math.cos(theta),
                r * Math.cos(phi),
                r * sinPhi * Math.sin(theta)
        );
    }
}
