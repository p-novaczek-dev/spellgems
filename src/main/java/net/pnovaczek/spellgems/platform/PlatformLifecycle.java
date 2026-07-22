package net.pnovaczek.spellgems.platform;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.function.Consumer;

/**
 * Loader-agnostic server lifecycle and datapack mutation hooks.
 */
public interface PlatformLifecycle {
    void onServerTickEnd(Consumer<MinecraftServer> callback);

    void onServerStopping(Consumer<MinecraftServer> callback);

    void onServerStopped(Consumer<MinecraftServer> callback);

    void onModifyLootTable(LootTableModifyCallback callback);

    @FunctionalInterface
    interface LootTableModifyCallback {
        void modify(
                ResourceKey<LootTable> key,
                LootTable.Builder tableBuilder,
                boolean builtin,
                HolderLookup.Provider registries
        );
    }
}
