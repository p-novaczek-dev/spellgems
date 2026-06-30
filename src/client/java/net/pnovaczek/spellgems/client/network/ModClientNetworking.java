package net.pnovaczek.spellgems.client.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.pnovaczek.spellgems.network.SpellEnchantingRecipeDescriptionsPayload;
import net.pnovaczek.spellgems.screen.SpellEnchantingMenu;

public final class ModClientNetworking {

    private ModClientNetworking() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(
                SpellEnchantingRecipeDescriptionsPayload.TYPE,
                (payload, context) -> context.client().execute(() -> {
                    if (context.client().player == null) {
                        return;
                    }

                    if (context.client().player.containerMenu.containerId != payload.containerId()) {
                        return;
                    }

                    if (context.client().player.containerMenu instanceof SpellEnchantingMenu menu) {
                        menu.setRecipeDescriptions(payload.descriptions());
                    }
                })
        );
    }
}