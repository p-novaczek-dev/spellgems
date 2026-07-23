package net.pnovaczek.spellgems.platform.neoforge;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.pnovaczek.spellgems.anvil.SpellTomeAnvilHandler;

/**
 * NeoForge anvil integration without {@code AnvilMenuMixin}.
 * Uses {@link AnvilUpdateEvent} so tome+gem combining stays event-based on Neo.
 */
public final class NeoForgeAnvilHandler {
    private static boolean registered;

    private NeoForgeAnvilHandler() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        NeoForge.EVENT_BUS.addListener(NeoForgeAnvilHandler::onAnvilUpdate);
    }

    private static void onAnvilUpdate(AnvilUpdateEvent event) {
        SpellTomeAnvilHandler.tryCombine(event.getLeft(), event.getRight()).ifPresent(combine -> {
            event.setOutput(combine.result());
            event.setXpCost(combine.xpCost());
            // Consume one tome from the right slot (matches typical anvil book apply).
            event.setMaterialCost(1);
        });
    }
}
