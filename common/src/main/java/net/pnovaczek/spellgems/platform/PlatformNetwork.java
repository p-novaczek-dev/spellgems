package net.pnovaczek.spellgems.platform;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/**
 * Loader-agnostic play networking: payload type registration, server receivers, and send helpers.
 */
public interface PlatformNetwork {
    void registerPayloadTypes();

    void registerServerReceivers();

    boolean canSend(ServerPlayer player, CustomPacketPayload.Type<?> type);

    void send(ServerPlayer player, CustomPacketPayload payload);
}
