package net.pnovaczek.spellgems.platform.client.fabric;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.recipe.v1.sync.ClientRecipeSynchronizedEvent;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.platform.client.PlatformClient;

import java.util.function.Consumer;

public final class FabricClient implements PlatformClient {
    @Override
    public KeyMapping registerKeyMapping(KeyMapping keyMapping) {
        return KeyMappingHelper.registerKeyMapping(keyMapping);
    }

    @Override
    public void onEndClientTick(Consumer<Minecraft> callback) {
        ClientTickEvents.END_CLIENT_TICK.register(callback::accept);
    }

    @Override
    public void onClientDisconnect(Consumer<Minecraft> callback) {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> callback.accept(client));
    }

    @Override
    public void onItemTooltip(PlatformClient.ItemTooltipCallback callback) {
        net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback.EVENT.register(callback::append);
    }

    @Override
    public void onPreAttack(PreAttackCallback callback) {
        ClientPreAttackCallback.EVENT.register(callback::onPreAttack);
    }

    @Override
    public void attachHudAfterHeldItemTooltip(Identifier id, HudElement renderer) {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.HELD_ITEM_TOOLTIP,
                id,
                renderer::render
        );
    }

    @Override
    public void onClientRecipesSynchronized(Runnable callback) {
        ClientRecipeSynchronizedEvent.EVENT.register((client, recipes) -> callback.run());
    }
}
