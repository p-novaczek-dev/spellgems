package net.pnovaczek.spellgems.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.pnovaczek.spellgems.ModItems;
import net.pnovaczek.spellgems.astralbow.AstralBowCaster;
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
        PayloadTypeRegistry.serverboundPlay().register(WandInputPayload.TYPE, WandInputPayload.CODEC);
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(WandInputPayload.TYPE, (payload, context) ->
                context.server().execute(() -> handleWandInput(context.player(), payload)));
    }

    private static void handleWandInput(ServerPlayer player, WandInputPayload payload) {
        switch (payload.action()) {
            case CAST -> WandSpellCaster.tryCast(player);
            case QUICK_CAST -> WandSpellCaster.tryCastFromSlot(player, payload.value());
            case CYCLE -> {
                ItemStack held = player.getMainHandItem();
                if (held.is(ModItems.WAND)) {
                    WandSpellCaster.cycleSelectedSpell(player, payload.value());
                } else if (held.is(ModItems.ASTRAL_BOW)) {
                    AstralBowCaster.cycleSelectedGem(player, payload.value());
                }
            }
        }
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
