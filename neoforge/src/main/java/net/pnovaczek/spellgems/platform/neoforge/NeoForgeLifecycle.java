package net.pnovaczek.spellgems.platform.neoforge;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
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
            if (!VillageManaRootLoot.isVillageHouse(event.getName())) {
                return;
            }
            try {
                event.getTable().addPool(VillageManaRootLoot.manaRootPool(event.getRegistries()).build());
            } catch (RuntimeException ignored) {
                // Duplicate pool name / frozen table — ignore
            }
        });
    }

    @Override
    public void onPlayerAttackLiving(PlayerAttackLivingCallback callback) {
        NeoForge.EVENT_BUS.addListener((AttackEntityEvent event) -> {
            if (event.getTarget() instanceof LivingEntity living && event.getEntity() instanceof Player player) {
                if (!player.level().isClientSide()) {
                    callback.onAttack(player, living, player.getMainHandItem());
                }
            }
        });
    }

    @Override
    public void onLivingDamagePre(LivingDamagePreCallback callback) {
        NeoForge.EVENT_BUS.addListener((LivingDamageEvent.Pre event) ->
                callback.beforeDamage(event.getEntity(), event.getSource()));
    }

    @Override
    public void onLivingDeath(LivingDeathCallback callback) {
        NeoForge.EVENT_BUS.addListener((LivingDeathEvent event) ->
                callback.onDeath(event.getEntity(), event.getSource()));
    }

    @Override
    public void onLivingDrops(LivingDropsCallback callback) {
        NeoForge.EVENT_BUS.addListener((LivingDropsEvent event) ->
                callback.onDrops(event.getEntity(), event.getSource(), event.getDrops()));
    }
}
