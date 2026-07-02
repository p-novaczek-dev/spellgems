package net.pnovaczek.spellgems.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.pnovaczek.spellgems.ModItems;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.item.data.SpellGemData;
import net.pnovaczek.spellgems.wand.WandSpellCaster;
import net.pnovaczek.spellgems.wand.WandSpellLabels;

public final class WandSpellHighlight {

    private static final Identifier HUD_ELEMENT_ID =
            Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "wand_spell_selection");

    private static int highlightTimer;
    private static Component highlightText = Component.empty();
    private static String lastHighlightKey = "";

    private WandSpellHighlight() {
    }

    public static void register() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.HELD_ITEM_TOOLTIP,
                HUD_ELEMENT_ID,
                WandSpellHighlight::render
        );
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (highlightTimer > 0) {
                highlightTimer--;
            }
        });
    }

    public static void onCycled(Minecraft client, int direction) {
        if (client.player == null) {
            return;
        }

        if (!WandSpellCaster.applyLocalCycle(client.player, direction)) {
            return;
        }

        SpellGemData data = WandSpellLabels.getSelectedGemData(client.player);
        if (data == null) {
            return;
        }

        showSelection(WandSpellLabels.formatSelection(data), client);
    }

    private static void showSelection(Component text, Minecraft client) {
        String key = text.getString();
        if (key.isEmpty()) {
            return;
        }

        int duration = (int) (40.0 * client.options.notificationDisplayTime().get());
        if (!key.equals(lastHighlightKey)) {
            lastHighlightKey = key;
        }

        highlightText = text;
        highlightTimer = duration;
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (highlightTimer <= 0 || highlightText.getString().isEmpty()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.player == null || !client.player.getMainHandItem().is(ModItems.WAND)) {
            return;
        }

        var font = client.font;
        int strWidth = font.width(highlightText);
        int x = (graphics.guiWidth() - strWidth) / 2;
        int y = graphics.guiHeight() - 59;
        if (!client.gameMode.canHurtPlayer()) {
            y += 14;
        }

        int alpha = (int) (highlightTimer * 256.0F / 10.0F);
        if (alpha > 255) {
            alpha = 255;
        }

        if (alpha > 0) {
            graphics.textWithBackdrop(font, highlightText, x, y, strWidth, ARGB.white(alpha));
        }
    }
}