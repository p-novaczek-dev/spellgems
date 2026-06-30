package net.pnovaczek.spellgems.spell;

import com.google.common.collect.Lists;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.List;

public final class SpellBurstScheduler {

    private record PendingAction(long targetTick, Runnable action) {}

    private static final List<PendingAction> PENDING_SERVER = Lists.newArrayList();
    private static final List<PendingAction> PENDING_CLIENT = Lists.newArrayList();
    private static boolean serverRegistered = false;

    private SpellBurstScheduler() {}

    public static void scheduleServer(int currentTick, int delayTicks, Runnable action) {
        ensureServerRegistered();
        PENDING_SERVER.add(new PendingAction(currentTick + delayTicks, action));
    }

    public static void scheduleClient(long currentTick, int delayTicks, Runnable action) {
        PENDING_CLIENT.add(new PendingAction(currentTick + delayTicks, action));
    }

    public static void tickClient(long currentTick) {
        PENDING_CLIENT.removeIf(pending -> {
            if (currentTick >= pending.targetTick) {
                pending.action.run();
                return true;
            }
            return false;
        });
    }

    private static void ensureServerRegistered() {
        if (serverRegistered) return;
        serverRegistered = true;

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            int tick = server.getTickCount();
            PENDING_SERVER.removeIf(pending -> {
                if (tick >= pending.targetTick) {
                    pending.action.run();
                    return true;
                }
                return false;
            });
        });
    }
}