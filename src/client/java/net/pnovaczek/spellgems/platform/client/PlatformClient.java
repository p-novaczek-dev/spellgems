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

import java.util.List;
import java.util.function.Consumer;

/**
 * Loader-agnostic client hooks (keys, tooltips, HUD, input, ticks).
 * Lives in the client source set so dedicated servers never load it.
 */
public interface PlatformClient {
    KeyMapping registerKeyMapping(KeyMapping keyMapping);

    void onEndClientTick(Consumer<Minecraft> callback);

    void onClientDisconnect(Consumer<Minecraft> callback);

    void onItemTooltip(ItemTooltipCallback callback);

    /**
     * @return {@code true} to cancel the vanilla attack (consume the click)
     */
    void onPreAttack(PreAttackCallback callback);

    void attachHudAfterHeldItemTooltip(Identifier id, HudElement renderer);

    void onClientRecipesSynchronized(Runnable callback);

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
