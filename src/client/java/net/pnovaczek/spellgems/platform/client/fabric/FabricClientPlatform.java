package net.pnovaczek.spellgems.platform.client.fabric;

import net.pnovaczek.spellgems.platform.client.ClientPlatform;
import net.pnovaczek.spellgems.platform.fabric.FabricPlatform;

/**
 * Fabric bootstrap for client platform services.
 */
public final class FabricClientPlatform {
    private static boolean bootstrapped;

    private FabricClientPlatform() {
    }

    public static void bootstrap() {
        if (bootstrapped) {
            return;
        }
        bootstrapped = true;
        // Client entrypoints (or JEI) may run without having touched the main entry class yet.
        FabricPlatform.bootstrapCommon();
        ClientPlatform.init(new FabricClient(), new FabricClientNetwork());
    }
}
