package net.pnovaczek.spellgems.platform.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;
import java.util.function.Consumer;

/**
 * Loader-agnostic client hooks (keys, tooltips, HUD, input, ticks, recipe sync).
 * Lives in the client source set so dedicated servers never load it.
 */
public interface PlatformClient {
    /**
     * Registers a key-mapping category for the controls screen sort order.
     * Fabric uses {@link KeyMapping.Category#register(Identifier)};
     * NeoForge queues it for {@code RegisterKeyMappingsEvent#registerCategory}.
     */
    KeyMapping.Category registerKeyCategory(Identifier id);

    KeyMapping registerKeyMapping(KeyMapping keyMapping);

    void onEndClientTick(Consumer<Minecraft> callback);

    void onClientDisconnect(Consumer<Minecraft> callback);

    void onItemTooltip(ItemTooltipCallback callback);

    /**
     * @return {@code true} to cancel the vanilla attack (consume the click)
     */
    void onPreAttack(PreAttackCallback callback);

    void attachHudAfterHeldItemTooltip(Identifier id, HudElement renderer);

    /**
     * Fires when multiplayer/custom recipe data becomes available on the client
     * (Fabric recipe sync, Neo {@code RecipesReceivedEvent}, etc.).
     */
    void onClientRecipesSynchronized(Runnable callback);

    /**
     * Recipes of the given type currently available on the multiplayer client
     * (not the integrated server). Empty if the loader has not synced them yet.
     */
    <I extends RecipeInput, T extends Recipe<I>> List<RecipeHolder<T>> getSyncedCustomRecipes(RecipeType<T> type);

    @FunctionalInterface
    interface ItemTooltipCallback {
        void append(ItemStack stack, TooltipContext context, TooltipFlag flag, List<Component> lines);
    }

    @FunctionalInterface
    interface PreAttackCallback {
        boolean onPreAttack(Minecraft client, LocalPlayer player, int clickCount);
    }

    @FunctionalInterface
    interface HudElement {
        void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker);
    }
}
