package net.pnovaczek.spellgems.platform.fabric;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.server.MinecraftServer;
import net.pnovaczek.spellgems.platform.PlatformLifecycle;

import java.util.function.Consumer;

public final class FabricLifecycle implements PlatformLifecycle {
    @Override
    public void onServerTickEnd(Consumer<MinecraftServer> callback) {
        ServerTickEvents.END_SERVER_TICK.register(callback::accept);
    }

    @Override
    public void onServerStopping(Consumer<MinecraftServer> callback) {
        ServerLifecycleEvents.SERVER_STOPPING.register(callback::accept);
    }

    @Override
    public void onServerStopped(Consumer<MinecraftServer> callback) {
        ServerLifecycleEvents.SERVER_STOPPED.register(callback::accept);
    }

    @Override
    public void onModifyLootTable(LootTableModifyCallback callback) {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) ->
                callback.modify(key, tableBuilder, source.isBuiltin(), registries));
    }
}
