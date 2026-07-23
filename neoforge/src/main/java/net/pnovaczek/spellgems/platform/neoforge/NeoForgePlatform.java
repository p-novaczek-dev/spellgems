package net.pnovaczek.spellgems.platform.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.pnovaczek.spellgems.platform.Platform;
import net.pnovaczek.spellgems.recipe.ManaInfuserRecipe;
import net.pnovaczek.spellgems.recipe.SpellEnchantingRecipe;
import net.pnovaczek.spellgems.registry.ModRegistries;

/**
 * NeoForge bootstrap for common platform services.
 * Must run before any code that resolves {@link Platform} (e.g. config load).
 * <p>
 * Content registration is bound to {@link RegisterEvent} (NeoForge registry lifecycle),
 * not static class-init or {@code BuiltInRegistries} writes outside that event.
 */
public final class NeoForgePlatform {
    private static boolean commonBootstrapped;

    private NeoForgePlatform() {
    }

    public static void bootstrapCommon(IEventBus modBus) {
        if (commonBootstrapped) {
            return;
        }
        commonBootstrapped = true;
        Platform.init(
                new NeoForgePaths(),
                new NeoForgeNetwork(modBus),
                new NeoForgeLifecycle(),
                new NeoPlatformRegistries()
        );
        modBus.addListener(NeoForgePlatform::onRegisterEvent);
        // Anvil tome+gem combine via event (no AnvilMenuMixin on Neo).
        NeoForgeAnvilHandler.register();
        // Request custom recipe types be sent to clients (JEI / multiplayer).
        NeoForge.EVENT_BUS.addListener(NeoForgePlatform::onDatapackSync);
    }

    private static void onRegisterEvent(RegisterEvent event) {
        ModRegistries.registerFor(event.getRegistryKey());
    }

    private static void onDatapackSync(OnDatapackSyncEvent event) {
        event.sendRecipes(ManaInfuserRecipe.TYPE, SpellEnchantingRecipe.TYPE);
    }
}
