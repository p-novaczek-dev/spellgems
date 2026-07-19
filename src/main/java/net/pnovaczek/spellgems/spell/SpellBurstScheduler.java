package net.pnovaczek.spellgems.spell;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.pnovaczek.spellgems.Spellgems;

import java.util.ArrayList;
import java.util.List;

/**
 * Delayed runnables for burst-style spell pulses.
 * <p>
 * Server and client queues are process-global but cleared on server stop and
 * client disconnect / world unload so actions do not leak across worlds.
 * Delayed runnables should re-check caster/level validity (e.g. {@code isAlive()}).
 */
public final class SpellBurstScheduler {

    private record PendingAction(long targetTick, Runnable action) {}

    private static final List<PendingAction> PENDING_SERVER = new ArrayList<>();
    private static final List<PendingAction> PENDING_CLIENT = new ArrayList<>();

    private static boolean serverHooksRegistered = false;
    /** Server whose tick count is used for {@link #PENDING_SERVER} targets. */
    private static MinecraftServer boundServer = null;
    /** Last client world game time; used to detect time resets / world changes. */
    private static long lastClientGameTime = Long.MIN_VALUE;

    private SpellBurstScheduler() {}

    /**
     * Registers server tick + lifecycle hooks. Safe to call once from mod init
     * (not lazily on first schedule).
     */
    public static void initialize() {
        if (serverHooksRegistered) {
            return;
        }
        serverHooksRegistered = true;

        ServerTickEvents.END_SERVER_TICK.register(SpellBurstScheduler::onServerTick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> clearServer());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> clearServer());
    }

    public static void scheduleServer(int currentTick, int delayTicks, Runnable action) {
        if (action == null || delayTicks < 0) {
            return;
        }
        ensureServerHooks();
        PENDING_SERVER.add(new PendingAction((long) currentTick + delayTicks, action));
    }

    public static void scheduleClient(long currentTick, int delayTicks, Runnable action) {
        if (action == null || delayTicks < 0) {
            return;
        }
        PENDING_CLIENT.add(new PendingAction(currentTick + delayTicks, action));
    }

    /**
     * Client end-tick. Pass the current client level's game time, or call
     * {@link #clearClient()} when there is no world.
     */
    public static void tickClient(long currentTick) {
        // World change or time went backwards (new world / reload) → drop stale pulses
        if (lastClientGameTime != Long.MIN_VALUE && currentTick < lastClientGameTime) {
            clearClient();
        }
        lastClientGameTime = currentTick;
        runDue(PENDING_CLIENT, currentTick);
    }

    public static void clearServer() {
        PENDING_SERVER.clear();
        boundServer = null;
    }

    public static void clearClient() {
        PENDING_CLIENT.clear();
        lastClientGameTime = Long.MIN_VALUE;
    }

    private static void ensureServerHooks() {
        if (!serverHooksRegistered) {
            // Fallback if a spell schedules before initialize() (should not happen in normal load).
            initialize();
        }
    }

    private static void onServerTick(MinecraftServer server) {
        if (boundServer != null && boundServer != server) {
            // Different server instance (e.g. integrated restart) — drop old queue
            PENDING_SERVER.clear();
        }
        boundServer = server;
        runDue(PENDING_SERVER, server.getTickCount());
    }

    private static void runDue(List<PendingAction> queue, long currentTick) {
        if (queue.isEmpty()) {
            return;
        }

        // Collect due first so nested schedule* during run is safe.
        List<PendingAction> due = new ArrayList<>();
        queue.removeIf(pending -> {
            if (currentTick >= pending.targetTick()) {
                due.add(pending);
                return true;
            }
            return false;
        });

        for (PendingAction pending : due) {
            try {
                pending.action().run();
            } catch (Exception e) {
                Spellgems.LOGGER.error("Spell burst delayed action failed", e);
            }
        }
    }
}
