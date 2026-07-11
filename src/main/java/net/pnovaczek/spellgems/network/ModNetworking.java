package net.pnovaczek.spellgems.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.pnovaczek.spellgems.astralbow.AstralBowCaster;
import net.pnovaczek.spellgems.screen.SpellEnchantingMenu;
import net.pnovaczek.spellgems.wand.WandSpellCaster;
import net.pnovaczek.spellgems.ModItems;
import net.minecraft.world.item.ItemStack;

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
        PayloadTypeRegistry.serverboundPlay().register(WandQuickCastPayload.TYPE, WandQuickCastPayload.CODEC);
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(WandCastPayload.TYPE, (payload, context) ->
                context.server().execute(() -> WandSpellCaster.tryCast(context.player())));

        ServerPlayNetworking.registerGlobalReceiver(WandCycleSpellPayload.TYPE, (payload, context) ->
                context.server().execute(() -> {
                    ItemStack held = context.player().getMainHandItem();
                    if (held.is(ModItems.WAND)) {
                        WandSpellCaster.cycleSelectedSpell(context.player(), payload.direction());
                    } else if (held.is(ModItems.ASTRAL_BOW)) {
                        AstralBowCaster.cycleSelectedGem(context.player(), payload.direction());
                    }
                }));

        ServerPlayNetworking.registerGlobalReceiver(WandQuickCastPayload.TYPE, (payload, context) ->
                context.server().execute(() -> WandSpellCaster.tryCastFromSlot(context.player(), payload.slot())));
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