package net.pnovaczek.spellgems.platform.client.neoforge;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.pnovaczek.spellgems.platform.client.PlatformClient;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class NeoForgeClient implements PlatformClient {
    private final List<KeyMapping> pendingKeyMappings = new ArrayList<>();
    private final List<HudAttachment> pendingHud = new ArrayList<>();
    private final List<Consumer<Minecraft>> endTickCallbacks = new ArrayList<>();
    private final List<Consumer<Minecraft>> disconnectCallbacks = new ArrayList<>();
    private final List<ItemTooltipCallback> tooltipCallbacks = new ArrayList<>();
    private final List<PreAttackCallback> preAttackCallbacks = new ArrayList<>();
    private final List<Runnable> recipeSyncCallbacks = new ArrayList<>();

    private @Nullable RecipeMap receivedRecipes = RecipeMap.EMPTY;

    public NeoForgeClient(IEventBus modBus) {
        modBus.addListener(this::onRegisterKeys);
        modBus.addListener(this::onRegisterGuiLayers);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
        NeoForge.EVENT_BUS.addListener(this::onLoggingOut);
        NeoForge.EVENT_BUS.addListener(this::onTooltipEvent);
        NeoForge.EVENT_BUS.addListener(this::onInteractionKey);
        NeoForge.EVENT_BUS.addListener(this::onRecipesReceived);
    }

    private void onRegisterKeys(RegisterKeyMappingsEvent event) {
        for (KeyMapping key : pendingKeyMappings) {
            event.register(key);
        }
        pendingKeyMappings.clear();
    }

    private void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        for (HudAttachment attachment : pendingHud) {
            event.registerAbove(
                    VanillaGuiLayers.SELECTED_ITEM_NAME,
                    attachment.id(),
                    (guiGraphics, deltaTracker) -> attachment.renderer().render(guiGraphics, deltaTracker)
            );
        }
        pendingHud.clear();
    }

    private void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        for (Consumer<Minecraft> callback : endTickCallbacks) {
            callback.accept(client);
        }
    }

    private void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        receivedRecipes = RecipeMap.EMPTY;
        Minecraft client = Minecraft.getInstance();
        for (Consumer<Minecraft> callback : disconnectCallbacks) {
            callback.accept(client);
        }
    }

    private void onTooltipEvent(ItemTooltipEvent event) {
        for (ItemTooltipCallback callback : tooltipCallbacks) {
            callback.append(event.getItemStack(), event.getContext(), event.getFlags(), event.getToolTip());
        }
    }

    private void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack()) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }
        for (PreAttackCallback callback : preAttackCallbacks) {
            if (callback.onPreAttack(client, client.player, 1)) {
                event.setCanceled(true);
                event.setSwingHand(false);
                break;
            }
        }
    }

    private void onRecipesReceived(RecipesReceivedEvent event) {
        receivedRecipes = event.getRecipeMap();
        for (Runnable callback : recipeSyncCallbacks) {
            callback.run();
        }
    }

    @Override
    public KeyMapping registerKeyMapping(KeyMapping keyMapping) {
        pendingKeyMappings.add(keyMapping);
        return keyMapping;
    }

    @Override
    public void onEndClientTick(Consumer<Minecraft> callback) {
        endTickCallbacks.add(callback);
    }

    @Override
    public void onClientDisconnect(Consumer<Minecraft> callback) {
        disconnectCallbacks.add(callback);
    }

    @Override
    public void onItemTooltip(ItemTooltipCallback callback) {
        tooltipCallbacks.add(callback);
    }

    @Override
    public void onPreAttack(PreAttackCallback callback) {
        preAttackCallbacks.add(callback);
    }

    @Override
    public void attachHudAfterHeldItemTooltip(Identifier id, HudElement renderer) {
        pendingHud.add(new HudAttachment(id, renderer));
    }

    @Override
    public void onClientRecipesSynchronized(Runnable callback) {
        recipeSyncCallbacks.add(callback);
    }

    @Override
    public <I extends RecipeInput, T extends Recipe<I>> List<RecipeHolder<T>> getSyncedCustomRecipes(RecipeType<T> type) {
        RecipeMap map = receivedRecipes;
        if (map == null || map == RecipeMap.EMPTY) {
            return List.of();
        }
        return new ArrayList<>(map.byType(type));
    }

    private record HudAttachment(Identifier id, HudElement renderer) {
    }
}
