package net.pnovaczek.spellgems.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class SelectedGemSlotIndicator {

    public static final int GEM_SLOT_X = 8;
    public static final int GEM_SLOT_Y = 36;
    public static final int GEM_SLOT_STEP = 18;
    public static final int GEM_SLOT_COUNT = 9;

    private static final int SLOT_NUMBER_COLOR = 0xFFAAAAAA;
    private static final int SELECTED_SLOT_NUMBER_COLOR = 0xFF000000;
    private static final int SLOT_NUMBER_Y_OFFSET = -11;
    private static final int NUMBER_HIT_TOP = GEM_SLOT_Y + SLOT_NUMBER_Y_OFFSET - 4;
    private static final int NUMBER_HIT_BOTTOM = GEM_SLOT_Y - 1;

    private SelectedGemSlotIndicator() {
    }

    /**
     * @return slot index whose number contains the point, or -1. Coordinates are relative to the GUI.
     */
    public static int hoveredSlot(double relativeX, double relativeY) {
        if (relativeY < NUMBER_HIT_TOP || relativeY >= NUMBER_HIT_BOTTOM) {
            return -1;
        }
        for (int slot = 0; slot < GEM_SLOT_COUNT; slot++) {
            int left = GEM_SLOT_X + slot * GEM_SLOT_STEP + 1;
            if (relativeX >= left && relativeX < left + GEM_SLOT_STEP - 2) {
                return slot;
            }
        }
        return -1;
    }

    /**
     * @return true when the click selected a slot number and should not reach the slots
     */
    public static boolean mouseClicked(
            AbstractContainerMenu menu,
            MouseButtonEvent event,
            int leftPos,
            int topPos,
            Minecraft minecraft
    ) {
        if (event.button() != InputConstants.MOUSE_BUTTON_LEFT || minecraft.player == null || minecraft.gameMode == null) {
            return false;
        }
        int slot = hoveredSlot(event.x() - leftPos, event.y() - topPos);
        if (slot < 0) {
            return false;
        }
        if (menu.clickMenuButton(minecraft.player, slot)) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, slot);
            return true;
        }
        return false;
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
}