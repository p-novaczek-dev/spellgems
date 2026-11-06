package net.pnovaczek.spellgems.platform.fabric;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
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

    @Override
    public void onPlayerAttackLiving(PlayerAttackLivingCallback callback) {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClientSide() && entity instanceof LivingEntity living) {
                callback.onAttack(player, living, player.getItemInHand(hand));
            }
            return InteractionResult.PASS;
        });
    }

    @Override
    public void onLivingDamagePre(LivingDamagePreCallback callback) {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            callback.beforeDamage(entity, source);
            return true;
        });
    }

    @Override
    public void onLivingDeath(LivingDeathCallback callback) {
        // Equipment is snapshotted at the start of dropAllDeathLoot by the Fabric mixin.
    }

    @Override
    public void onLivingDrops(LivingDropsCallback callback) {
        // Drops are captured by the Fabric mixin. NeoForge edits LivingDropsEvent instead.
    }
}
