package net.pnovaczek.spellgems.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.pnovaczek.spellgems.Spellgems;

/**
 * Thin helpers for registering into vanilla {@link Registry}s during
 * {@link ModRegistries#registerAll()}. On NeoForge these call sites can later
 * map to {@code DeferredRegister} without changing gameplay code.
 */
public final class ModRegistry {
    private ModRegistry() {
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, path);
    }

    /**
     * Matches vanilla {@link Registry#register(Registry, Identifier, Object)} generics so
     * callers can keep concrete types (e.g. {@code WandItem} under {@code Registry<Item>}).
     */
    public static <V, T extends V> T register(Registry<V> registry, String path, T value) {
        return Registry.register(registry, id(path), value);
    }

    public static <V, T extends V> T register(Registry<V> registry, ResourceKey<V> key, T value) {
        return Registry.register(registry, key, value);
    }
}
