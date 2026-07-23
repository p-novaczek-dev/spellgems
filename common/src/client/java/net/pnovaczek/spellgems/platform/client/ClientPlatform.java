package net.pnovaczek.spellgems.platform.client;

/**
 * Holds client-only platform services. Bootstrapped from the client mod entrypoint.
 */
public final class ClientPlatform {
    private static PlatformClient client;
    private static PlatformClientNetwork network;

    private ClientPlatform() {
    }

    public static void init(PlatformClient client, PlatformClientNetwork network) {
        ClientPlatform.client = client;
        ClientPlatform.network = network;
    }

    public static boolean isInitialized() {
        return client != null && network != null;
    }

    public static PlatformClient client() {
        return require(client, "client");
    }

    public static PlatformClientNetwork network() {
        return require(network, "network");
    }

    private static <T> T require(T value, String name) {
        if (value == null) {
            throw new IllegalStateException("ClientPlatform." + name + " not initialized");
        }
        return value;
    }
}
