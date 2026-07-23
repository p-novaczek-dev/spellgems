package net.pnovaczek.spellgems.platform.neoforge;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.network.ModNetworking;
import net.pnovaczek.spellgems.network.SpellEnchantingRecipeDescriptionsPayload;
import net.pnovaczek.spellgems.network.WandInputPayload;
import net.pnovaczek.spellgems.platform.PlatformNetwork;
import net.pnovaczek.spellgems.screen.SpellEnchantingMenu;

public final class NeoForgeNetwork implements PlatformNetwork {
    private boolean registered;

    public NeoForgeNetwork(IEventBus modBus) {
        modBus.addListener(this::onRegisterPayloads);
    }

    private void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Spellgems.MOD_ID).versioned("1");

        registrar.playToClient(
                SpellEnchantingRecipeDescriptionsPayload.TYPE,
                SpellEnchantingRecipeDescriptionsPayload.CODEC,
                NeoForgeNetwork::handleRecipeDescriptions
        );
        registrar.playToServer(
                WandInputPayload.TYPE,
                WandInputPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer serverPlayer) {
                        ModNetworking.handleWandInput(serverPlayer, payload);
                    }
                })
        );
        registered = true;
    }

    private static void handleRecipeDescriptions(
            SpellEnchantingRecipeDescriptionsPayload payload,
            IPayloadContext context
    ) {
        context.enqueueWork(() -> {
            var player = context.player();
            if (player == null) {
                return;
            }
            if (player.containerMenu.containerId != payload.containerId()) {
                return;
            }
            if (player.containerMenu instanceof SpellEnchantingMenu menu) {
                menu.setRecipeDescriptions(payload.descriptions());
            }
        });
    }

    @Override
    public void registerPayloadTypes() {
        // Handled by RegisterPayloadHandlersEvent (mod bus listener registered in ctor).
    }

    @Override
    public void registerServerReceivers() {
        // Combined with payload registration on NeoForge.
    }

    @Override
    public boolean canSend(ServerPlayer player, CustomPacketPayload.Type<?> type) {
        // NeoForge connection tracks registered payloads; assume yes on dedicated play connection.
        return player.connection != null;
    }

    @Override
    public void send(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }
}
