package net.pnovaczek.spellgems.platform;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

    /**
     * Server-side: the player attacked a living entity (not air or a block).
     */
    void onPlayerAttackLiving(PlayerAttackLivingCallback callback);

    @FunctionalInterface
    interface PlayerAttackLivingCallback {
        void onAttack(Player player, LivingEntity target, ItemStack weapon);
    }

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
