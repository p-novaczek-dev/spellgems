package net.pnovaczek.spellgems.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.pnovaczek.spellgems.screen.SpellEnchantingMenu;
import net.pnovaczek.spellgems.wand.WandSpellCaster;

import java.util.List;

public final class ModNetworking {

    private ModNetworking() {
    }

    public static void registerPayloadTypes() {
        PayloadTypeRegistry.clientboundPlay().register(
                SpellEnchantingRecipeDescriptionsPayload.TYPE,
                SpellEnchantingRecipeDescriptionsPayload.CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(WandCastPayload.TYPE, WandCastPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(WandCycleSpellPayload.TYPE, WandCycleSpellPayload.CODEC);
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(WandCastPayload.TYPE, (payload, context) ->
                context.server().execute(() -> WandSpellCaster.tryCast(context.player())));

        ServerPlayNetworking.registerGlobalReceiver(WandCycleSpellPayload.TYPE, (payload, context) ->
                context.server().execute(() -> WandSpellCaster.cycleSelectedSpell(
                        context.player(),
                        payload.direction()
                )));
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