package net.pnovaczek.spellgems.client.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.pnovaczek.spellgems.network.WandInputPayload;

/**
 * Client → server wand/bow input. Mirrors {@link WandInputPayload} actions.
 */
public final class WandClientNetworking {

    private WandClientNetworking() {
    }

    public static void sendCast() {
        send(WandInputPayload.cast());
    }

    public static void sendCycle(int direction) {
        if (direction == 0) {
            return;
        }
        send(WandInputPayload.cycle(direction));
    }

    public static void sendQuickCast(int slot) {
        send(WandInputPayload.quickCast(slot));
    }

    private static void send(WandInputPayload payload) {
        if (ClientPlayNetworking.canSend(WandInputPayload.TYPE)) {
            ClientPlayNetworking.send(payload);
        }
    }
}
