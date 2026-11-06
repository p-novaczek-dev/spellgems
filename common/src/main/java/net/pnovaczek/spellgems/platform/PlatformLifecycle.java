package net.pnovaczek.spellgems.platform;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Collection;
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

    /**
     * Before health is removed, so a non-spell hit can drop its affix credit before death loot.
     */
    void onLivingDamagePre(LivingDamagePreCallback callback);

    /** Start of death, before drops. Neo snapshots equipment here. Fabric snapshots in its drop mixin. */
    void onLivingDeath(LivingDeathCallback callback);

    /** Neo rewrites the drop collection in place. Fabric leaves this unused. */
    void onLivingDrops(LivingDropsCallback callback);

    @FunctionalInterface
    interface PlayerAttackLivingCallback {
        void onAttack(Player player, LivingEntity target, ItemStack weapon);
    }

    @FunctionalInterface
    interface LivingDamagePreCallback {
        void beforeDamage(LivingEntity entity, DamageSource source);
    }

    @FunctionalInterface
    interface LivingDeathCallback {
        void onDeath(LivingEntity entity, DamageSource source);
    }

    @FunctionalInterface
    interface LivingDropsCallback {
        void onDrops(LivingEntity entity, DamageSource source, Collection<ItemEntity> drops);
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
