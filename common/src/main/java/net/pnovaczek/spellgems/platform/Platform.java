package net.pnovaczek.spellgems.platform;

/**
 * Holds common (server + shared) platform services.
 * Bootstrapped once from the loader entrypoint before other static init that needs paths.
 */
public final class Platform {
    private static PlatformPaths paths;
    private static PlatformNetwork network;
    private static PlatformLifecycle lifecycle;
    private static PlatformRegistries registries;

    private Platform() {
    }

    public static void init(
            PlatformPaths paths,
            PlatformNetwork network,
            PlatformLifecycle lifecycle,
            PlatformRegistries registries
    ) {
        Platform.paths = paths;
        Platform.network = network;
        Platform.lifecycle = lifecycle;
        Platform.registries = registries;
    }

    public static PlatformPaths paths() {
        return require(paths, "paths");
    }

    public static PlatformNetwork network() {
        return require(network, "network");
    }

    public static PlatformLifecycle lifecycle() {
        return require(lifecycle, "lifecycle");
    }

    public static PlatformRegistries registries() {
        return require(registries, "registries");
    }

    private static <T> T require(T value, String name) {
        if (value == null) {
            throw new IllegalStateException("Platform." + name + " not initialized (loader bootstrap missing)");
        }
        return value;
    }
}
