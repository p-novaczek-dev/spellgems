package net.pnovaczek.spellgems.client.network;

import net.minecraft.client.Minecraft;
import net.pnovaczek.spellgems.network.SpellEnchantingRecipeDescriptionsPayload;
import net.pnovaczek.spellgems.platform.client.ClientPlatform;
import net.pnovaczek.spellgems.screen.SpellEnchantingMenu;

/**
 * Common client networking handlers. Registration is done by the platform network impl.
 */
public final class ModClientNetworking {

    private ModClientNetworking() {
    }

    public static void register() {
        ClientPlatform.network().registerReceivers();
    }

    public static void handleRecipeDescriptions(SpellEnchantingRecipeDescriptionsPayload payload, Minecraft client) {
        if (client.player == null) {
            return;
        }

        if (client.player.containerMenu.containerId != payload.containerId()) {
            return;
        }

        if (client.player.containerMenu instanceof SpellEnchantingMenu menu) {
            menu.setRecipeDescriptions(payload.descriptions());
        }
    }
}
