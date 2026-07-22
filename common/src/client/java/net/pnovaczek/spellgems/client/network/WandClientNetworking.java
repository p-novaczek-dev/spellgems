package net.pnovaczek.spellgems.client.network;

import net.pnovaczek.spellgems.network.WandInputPayload;
import net.pnovaczek.spellgems.platform.client.ClientPlatform;

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
        if (ClientPlatform.network().canSend(WandInputPayload.TYPE)) {
            ClientPlatform.network().send(payload);
        }
    }
}
