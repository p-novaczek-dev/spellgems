package net.pnovaczek.spellgems.platform.client.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.pnovaczek.spellgems.client.network.ModClientNetworking;
import net.pnovaczek.spellgems.network.SpellEnchantingRecipeDescriptionsPayload;
import net.pnovaczek.spellgems.platform.client.PlatformClientNetwork;

public final class FabricClientNetwork implements PlatformClientNetwork {
    @Override
    public void registerReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(
                SpellEnchantingRecipeDescriptionsPayload.TYPE,
                (payload, context) -> context.client().execute(() ->
                        ModClientNetworking.handleRecipeDescriptions(payload, context.client()))
        );
    }

    @Override
    public boolean canSend(CustomPacketPayload.Type<?> type) {
        return ClientPlayNetworking.canSend(type);
    }

    @Override
    public void send(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }
}
