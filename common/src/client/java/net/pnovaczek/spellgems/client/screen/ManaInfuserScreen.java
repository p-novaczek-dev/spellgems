package net.pnovaczek.spellgems.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.*;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.block.entity.ManaInfuserBlockEntity;
import net.pnovaczek.spellgems.screen.ManaInfuserMenu;

import java.util.List;

public class ManaInfuserScreen extends AbstractContainerScreen<ManaInfuserMenu> {

    private static final int BACKGROUND_TEXTURE_WIDTH = 256;
    private static final int BACKGROUND_TEXTURE_HEIGHT = 256;

    private static final Identifier CONTAINER_TEXTURE = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "textures/gui/container/mana_infuser.png");
    private static final Identifier MANA_BAR_SPRITE = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "container/mana_infuser/mana_bar");
    private static final Identifier PROGRESS_ARROW_SPRITE = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "container/mana_infuser/progress");

    public ManaInfuserScreen(ManaInfuserMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int xo = (this.width - this.imageWidth) / 2;
        int yo = (this.height - this.imageHeight) / 2;

        // Background
        graphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_TEXTURE, xo, yo, 0.0F, 0.0F,
                this.imageWidth, this.imageHeight, BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);

        // === MANA BUFFER BAR (vertical, left side) ===
        int manaLevel = this.menu.getManaLevel();
        int maxManaLevel = ManaInfuserBlockEntity.MAX_MANA;
        int barWidth = 6;
        int barHeightMax = 54;
        int barHeight = Mth.clamp(manaLevel * barHeightMax / maxManaLevel, 0, barHeightMax);
        if (barHeight > 0) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, MANA_BAR_SPRITE, barWidth, barHeightMax, 0, barHeightMax - barHeight,
                    xo + 13, yo + 16 + barHeightMax - barHeight, barWidth, barHeight);
        }

        // === PROGRESS ARROW (horizontal, right side) ===
        int progressScaled = Mth.clamp(this.menu.getProgress() * 24 / 100, 0, 24); // 24 pixels wide
        if (progressScaled > 0) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESS_ARROW_SPRITE, 24, 16, 0, 0,
                    xo + 79, yo + 34, progressScaled, 16);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        this.extractTooltip(graphics, mouseX, mouseY);

        // Mana bar tooltip
        int xo = (this.width - this.imageWidth) / 2;
        int yo = (this.height - this.imageHeight) / 2;

        int barX = xo + 13;
        int barY = yo + 16;
        int barW = 6;
        int barH = 54;

        if (mouseX >= barX && mouseX < barX + barW && mouseY >= barY && mouseY < barY + barH) {
            int mana = this.menu.getManaLevel();
            var manaText = Component.translatable("tooltip.spellgems.mana_infuser.mana_level", mana).getString();
            var manaTextFormat = Style.EMPTY;
            var manaTextFormatted = FormattedCharSequence.forward(manaText, manaTextFormat);
            ClientTooltipComponent manaTooltipText = new ClientTextTooltip(manaTextFormatted);
            var manaTooltipTextRows = List.of(manaTooltipText);
            var currentScreenRectangle = new ScreenRectangle(mouseX, mouseY, 1, 1);
            var manaTooltipPositioner = new MenuTooltipPositioner(currentScreenRectangle);
            Identifier manaTooltipStyle = null;
            graphics.tooltip(this.font, manaTooltipTextRows, mouseX, mouseY, manaTooltipPositioner, manaTooltipStyle);
        }
    }
}