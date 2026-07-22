package net.pnovaczek.spellgems.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.pnovaczek.spellgems.ModComponents;
import net.pnovaczek.spellgems.ModItems;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.astralbow.AstralBowCaster;
import net.pnovaczek.spellgems.inventory.AstralBowContainer;
import net.pnovaczek.spellgems.inventory.WandContainer;
import net.pnovaczek.spellgems.item.data.AstralBowData;
import net.pnovaczek.spellgems.item.data.SpellGemData;
import net.pnovaczek.spellgems.item.data.WandData;
import net.pnovaczek.spellgems.platform.client.ClientPlatform;
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
        ClientPlatform.client().attachHudAfterHeldItemTooltip(HUD_ELEMENT_ID, WandSpellHighlight::render);
        ClientPlatform.client().onEndClientTick(client -> {
            if (highlightTimer > 0) {
                highlightTimer--;
            }
        });
    }

    public static void onCycled(Minecraft client, int direction) {
        if (client.player == null) {
            return;
        }

        ItemStack held = client.player.getMainHandItem();
        boolean cycled;
        if (held.is(ModItems.WAND)) {
            cycled = WandSpellCaster.applyLocalCycle(client.player, direction);
        } else if (held.is(ModItems.ASTRAL_BOW)) {
            cycled = AstralBowCaster.applyLocalCycle(client.player, direction);
        } else {
            return;
        }

        if (!cycled) {
            return;
        }

        SpellGemData data = held.is(ModItems.WAND)
                ? WandSpellLabels.getSelectedGemData(client.player)
                : AstralBowCaster.getSelectedGemData(client.player);
        if (data == null) {
            return;
        }

        int selectedSlot = held.is(ModItems.WAND)
                ? Mth.clamp(held.getOrDefault(ModComponents.WAND_DATA, WandData.DEFAULT).selectedSlot(), 0, WandContainer.SIZE - 1)
                : Mth.clamp(held.getOrDefault(ModComponents.ASTRAL_BOW_DATA, AstralBowData.DEFAULT).selectedSlot(), 0, AstralBowContainer.SIZE - 1);
        showSelection(WandSpellLabels.formatSelection(data, selectedSlot), client);
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
        if (client.player == null) {
            return;
        }

        ItemStack held = client.player.getMainHandItem();
        if (!held.is(ModItems.WAND) && !held.is(ModItems.ASTRAL_BOW)) {
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