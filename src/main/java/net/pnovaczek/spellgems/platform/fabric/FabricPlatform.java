package net.pnovaczek.spellgems.platform.fabric;

import net.pnovaczek.spellgems.platform.Platform;

/**
 * Fabric bootstrap for common platform services.
 * Must run before any code that resolves {@link Platform} (e.g. config load).
 */
public final class FabricPlatform {
    private static boolean commonBootstrapped;

    private FabricPlatform() {
    }

    public static void bootstrapCommon() {
        if (commonBootstrapped) {
            return;
        }
        commonBootstrapped = true;
        Platform.init(
                new FabricPaths(),
                new FabricNetwork(),
                new FabricLifecycle(),
                new FabricRegistries()
        );
    }
}
