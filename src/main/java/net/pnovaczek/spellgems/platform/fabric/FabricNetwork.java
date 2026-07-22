package net.pnovaczek.spellgems.platform.fabric;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.pnovaczek.spellgems.network.ModNetworking;
import net.pnovaczek.spellgems.network.SpellEnchantingRecipeDescriptionsPayload;
import net.pnovaczek.spellgems.network.WandInputPayload;
import net.pnovaczek.spellgems.platform.PlatformNetwork;

public final class FabricNetwork implements PlatformNetwork {
    @Override
    public void registerPayloadTypes() {
        PayloadTypeRegistry.clientboundPlay().register(
                SpellEnchantingRecipeDescriptionsPayload.TYPE,
                SpellEnchantingRecipeDescriptionsPayload.CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(WandInputPayload.TYPE, WandInputPayload.CODEC);
    }

    @Override
    public void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(WandInputPayload.TYPE, (payload, context) ->
                context.server().execute(() -> ModNetworking.handleWandInput(context.player(), payload)));
    }

    @Override
    public boolean canSend(ServerPlayer player, CustomPacketPayload.Type<?> type) {
        return ServerPlayNetworking.canSend(player, type);
    }

    @Override
    public void send(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }
}
