package net.pnovaczek.spellgems.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.screen.AstralBowMenu;

public class AstralBowScreen extends AbstractContainerScreen<AstralBowMenu> {

    private static final Identifier CONTAINER_TEXTURE =
            Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "textures/gui/container/astral_bow.png");

    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;

    public AstralBowScreen(AstralBowMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_TEXTURE, x, y, 0.0F, 0.0F,
                this.imageWidth, this.imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractContents(graphics, mouseX, mouseY, delta);

        graphics.pose().pushMatrix();
        graphics.pose().translate(this.leftPos, this.topPos);
        SelectedGemSlotIndicator.render(graphics, this.font, this.menu.getSelectedSlot());
        graphics.pose().popMatrix();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (SelectedGemSlotIndicator.mouseClicked(this.menu, event, this.leftPos, this.topPos, this.minecraft)) {
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }
}