package net.pnovaczek.spellgems.platform.neoforge;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.pnovaczek.spellgems.loot.VillageManaRootLoot;
import net.pnovaczek.spellgems.platform.PlatformLifecycle;

import java.util.function.Consumer;

public final class NeoForgeLifecycle implements PlatformLifecycle {
    @Override
    public void onServerTickEnd(Consumer<MinecraftServer> callback) {
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) ->
                callback.accept(event.getServer()));
    }

    @Override
    public void onServerStopping(Consumer<MinecraftServer> callback) {
        NeoForge.EVENT_BUS.addListener((ServerStoppingEvent event) ->
                callback.accept(event.getServer()));
    }

    @Override
    public void onServerStopped(Consumer<MinecraftServer> callback) {
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) ->
                callback.accept(event.getServer()));
    }

    @Override
    public void onModifyLootTable(LootTableModifyCallback callback) {
        // Neo fires a load event with a mutable table (addPool). Fabric uses a builder callback.
        // We implement village inject directly here and still accept the callback for API parity
        // (unused for Neo's load path beyond village inject).
        NeoForge.EVENT_BUS.addListener((LootTableLoadEvent event) -> {
            String path = event.getName().getPath();
            if (path.startsWith("chests/village/village_") && path.endsWith("_house")) {
                try {
                    event.getTable().addPool(
                            LootPool.lootPool()
                                    .add(NestedLootTable.lootTableReference(VillageManaRootLoot.MANA_ROOT_LOOT))
                                    .build()
                    );
                } catch (RuntimeException ignored) {
                    // Duplicate pool name / frozen table — ignore
                }
            }
            // Note: generic callback expects a Builder; Neo path handles village inject above.
        });
    }
}
