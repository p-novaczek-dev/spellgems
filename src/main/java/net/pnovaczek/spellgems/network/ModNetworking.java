package net.pnovaczek.spellgems.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.pnovaczek.spellgems.screen.SpellEnchantingMenu;

import java.util.List;

public final class ModNetworking {

    private ModNetworking() {
    }

    public static void registerPayloadTypes() {
        PayloadTypeRegistry.clientboundPlay().register(
                SpellEnchantingRecipeDescriptionsPayload.TYPE,
                SpellEnchantingRecipeDescriptionsPayload.CODEC
        );
    }

    public static void sendRecipeDescriptions(ServerPlayer player, SpellEnchantingMenu menu, List<String> descriptions) {
        if (!ServerPlayNetworking.canSend(player, SpellEnchantingRecipeDescriptionsPayload.TYPE)) {
            return;
        }

        ServerPlayNetworking.send(
                player,
                new SpellEnchantingRecipeDescriptionsPayload(menu.containerId, descriptions)
        );
    }
}