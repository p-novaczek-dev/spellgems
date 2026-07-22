package net.pnovaczek.spellgems.platform.client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client-side play networking (send to server + receive clientbound payloads).
 */
public interface PlatformClientNetwork {
    void registerReceivers();

    boolean canSend(CustomPacketPayload.Type<?> type);

    void send(CustomPacketPayload payload);
}
