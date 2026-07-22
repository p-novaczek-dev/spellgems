package net.pnovaczek.spellgems.loot;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.pnovaczek.spellgems.Spellgems;

/**
 * Injects mana root into vanilla village house chests.
 * Wiring to a loader loot-modify event lives in the platform layer.
 */
public final class VillageManaRootLoot {
    public static final ResourceKey<LootTable> MANA_ROOT_LOOT = ResourceKey.create(
            Registries.LOOT_TABLE,
            Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "chests/village_mana_root")
    );

    private VillageManaRootLoot() {
    }

    public static void tryInject(ResourceKey<LootTable> key, LootTable.Builder tableBuilder, boolean builtin) {
        if (!builtin) {
            return;
        }
        String path = key.identifier().getPath();
        if (path.startsWith("chests/village/village_") && path.endsWith("_house")) {
            tableBuilder.withPool(
                    LootPool.lootPool()
                            .add(NestedLootTable.lootTableReference(MANA_ROOT_LOOT))
            );
        }
    }
}
