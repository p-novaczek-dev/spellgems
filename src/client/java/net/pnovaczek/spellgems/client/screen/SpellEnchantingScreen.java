package net.pnovaczek.spellgems.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.screen.SpellEnchantingMenu;

public class SpellEnchantingScreen extends AbstractContainerScreen<SpellEnchantingMenu> {

    private static final Identifier GUI_TEXTURE = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "textures/gui/container/spell_enchanting_table.png");

    // Single button position (centered like vanilla's middle slot)
    private static final int BUTTON_X = 60;
    private static final int BUTTON_Y = 33;   // 14 + 19*1 (middle position)
    private static final int BUTTON_WIDTH = 108;
    private static final int BUTTON_HEIGHT = 19;

    public SpellEnchantingScreen(SpellEnchantingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void containerTick() {
        super.containerTick();
    }

    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;

        // Draw background
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, left, top, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

        // Draw single button area
        int btnX = left + BUTTON_X;
        int btnY = top + BUTTON_Y;

        boolean hovered = isHovering(BUTTON_X, BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT, mouseX, mouseY);

        if (hovered) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                    Identifier.withDefaultNamespace("container/enchanting_table/enchantment_slot_highlighted"),
                    btnX, btnY, BUTTON_WIDTH, BUTTON_HEIGHT);
        } else {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                    Identifier.withDefaultNamespace("container/enchanting_table/enchantment_slot"),
                    btnX, btnY, BUTTON_WIDTH, BUTTON_HEIGHT);
        }

        // TODO later: draw level cost text, enchantment preview name, etc.
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float ignored) {
        super.extractRenderState(graphics, mouseX, mouseY, ignored);
        // TODO later: tooltip showing what enchantments will be added
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;

        // Check if clicked inside our single button
        if (isHovering(BUTTON_X, BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT, event.x(), event.y())) {
            if (this.menu.clickMenuButton(this.minecraft.player, 0)) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }
}