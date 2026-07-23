package net.pnovaczek.spellgems.platform.client.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.pnovaczek.spellgems.platform.client.ClientPlatform;
import net.pnovaczek.spellgems.platform.neoforge.NeoForgePlatform;

/**
 * NeoForge bootstrap for client platform services.
 */
public final class NeoForgeClientPlatform {
    private static boolean bootstrapped;

    private NeoForgeClientPlatform() {
    }

    public static void bootstrap(IEventBus modBus) {
        if (bootstrapped) {
            return;
        }
        bootstrapped = true;
        NeoForgePlatform.bootstrapCommon(modBus);
        ClientPlatform.init(new NeoForgeClient(modBus), new NeoForgeClientNetwork());
    }
}
