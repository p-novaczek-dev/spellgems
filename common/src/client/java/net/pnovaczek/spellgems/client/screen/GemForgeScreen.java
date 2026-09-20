package net.pnovaczek.spellgems.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.pnovaczek.spellgems.ExperienceLevels;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.screen.GemForgeMenu;

public class GemForgeScreen extends AbstractContainerScreen<GemForgeMenu> {

    private static final Identifier CONTAINER_TEXTURE =
            Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "textures/gui/container/gem_forge.png");
    private static final Identifier ERROR_SPRITE =
            Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "container/gem_forge/error");

    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int ERROR_X = 65;
    private static final int ERROR_Y = 46;
    private static final int ERROR_WIDTH = 28;
    private static final int ERROR_HEIGHT = 21;

    public GemForgeScreen(GemForgeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.minecraft.player.experienceDisplayStartTick = this.minecraft.player.tickCount;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_TEXTURE, x, y, 0.0F, 0.0F,
                this.imageWidth, this.imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        if (this.menu.hasRecipeError()) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ERROR_SPRITE, x + ERROR_X, y + ERROR_Y, ERROR_WIDTH, ERROR_HEIGHT);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        if (!this.menu.getSlot(GemForgeMenu.RESULT_SLOT).hasItem()) {
            return;
        }

        int xpCost = this.menu.getXpCost();
        if (xpCost <= 0) {
            return;
        }
        Component line = Component.translatable(
                "container.spellgems.gem_forge.level_cost",
                ExperienceLevels.formatLevelsForXpCost(xpCost, this.minecraft.player)
        );
        int color = this.menu.getSlot(GemForgeMenu.RESULT_SLOT).mayPickup(this.minecraft.player)
                ? -8323296
                : -40864;
        int tx = this.imageWidth - 8 - this.font.width(line) - 2;
        graphics.fill(tx - 2, 67, this.imageWidth - 8, 79, 1325400064);
        graphics.text(this.font, line, tx, 69, color);
    }
}
