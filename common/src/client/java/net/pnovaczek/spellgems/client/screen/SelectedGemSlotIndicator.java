package net.pnovaczek.spellgems.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.pnovaczek.spellgems.ModComponents;
import net.pnovaczek.spellgems.ModItems;
import net.pnovaczek.spellgems.item.data.AstralBowData;
import net.pnovaczek.spellgems.item.data.WandData;

public final class SelectedGemSlotIndicator {

    public static final int GEM_SLOT_X = 8;
    public static final int GEM_SLOT_Y = 36;
    public static final int GEM_SLOT_STEP = 18;
    public static final int GEM_SLOT_COUNT = 9;

    private static final int SLOT_NUMBER_COLOR = 0xFFAAAAAA;
    private static final int SELECTED_SLOT_NUMBER_COLOR = 0xFF000000;
    private static final int SLOT_NUMBER_Y_OFFSET = -11;

    private SelectedGemSlotIndicator() {
    }

    public static int getWandSelectedSlot(Minecraft client) {
        return getSelectedSlot(client, ModItems.WAND,
                stack -> stack.getOrDefault(ModComponents.WAND_DATA, WandData.DEFAULT).selectedSlot());
    }

    public static int getAstralBowSelectedSlot(Minecraft client) {
        return getSelectedSlot(client, ModItems.ASTRAL_BOW,
                stack -> stack.getOrDefault(ModComponents.ASTRAL_BOW_DATA, AstralBowData.DEFAULT).selectedSlot());
    }

    private static int getSelectedSlot(Minecraft client, net.minecraft.world.item.Item item, SlotReader reader) {
        if (client.player == null) {
            return 0;
        }

        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = client.player.getItemInHand(hand);
            if (stack.is(item)) {
                return Mth.clamp(reader.read(stack), 0, GEM_SLOT_COUNT - 1);
            }
        }

        return 0;
    }

    public static void render(GuiGraphicsExtractor graphics, Font font, int selectedSlot) {
        int numberY = GEM_SLOT_Y + SLOT_NUMBER_Y_OFFSET;
        for (int slot = 0; slot < GEM_SLOT_COUNT; slot++) {
            int slotX = GEM_SLOT_X + slot * GEM_SLOT_STEP;
            int centerX = slotX + 8;
            int color = slot == selectedSlot ? SELECTED_SLOT_NUMBER_COLOR : SLOT_NUMBER_COLOR;
            String number = String.valueOf(slot + 1);
            graphics.text(font, number, centerX - font.width(number) / 2, numberY, color, false);
        }
    }

    @FunctionalInterface
    private interface SlotReader {
        int read(ItemStack stack);
    }
}