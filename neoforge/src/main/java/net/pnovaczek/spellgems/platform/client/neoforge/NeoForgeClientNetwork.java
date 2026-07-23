package net.pnovaczek.spellgems.platform.client.neoforge;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.pnovaczek.spellgems.platform.client.PlatformClientNetwork;

/**
 * Client networking on NeoForge.
 * Clientbound handlers are registered with payloads on the common network registrar
 * (see {@link net.pnovaczek.spellgems.platform.neoforge.NeoForgeNetwork}).
 */
public final class NeoForgeClientNetwork implements PlatformClientNetwork {
    @Override
    public void registerReceivers() {
        // Clientbound recipe-description handler is registered in NeoForgeNetwork
        // via RegisterPayloadHandlersEvent (playToClient).
    }

    @Override
    public boolean canSend(CustomPacketPayload.Type<?> type) {
        return Minecraft.getInstance().getConnection() != null;
    }

    @Override
    public void send(CustomPacketPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }
}
