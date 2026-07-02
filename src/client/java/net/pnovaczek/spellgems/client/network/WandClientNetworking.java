package net.pnovaczek.spellgems.client.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.pnovaczek.spellgems.network.WandCastPayload;
import net.pnovaczek.spellgems.network.WandCycleSpellPayload;

public final class WandClientNetworking {

    private WandClientNetworking() {
    }

    public static void sendCast() {
        if (ClientPlayNetworking.canSend(WandCastPayload.TYPE)) {
            ClientPlayNetworking.send(new WandCastPayload());
        }
    }

    public static void sendCycle(int direction) {
        if (direction != 0 && ClientPlayNetworking.canSend(WandCycleSpellPayload.TYPE)) {
            ClientPlayNetworking.send(new WandCycleSpellPayload(direction));
        }
    }
}