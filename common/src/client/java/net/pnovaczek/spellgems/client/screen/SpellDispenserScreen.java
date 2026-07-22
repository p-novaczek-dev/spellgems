package net.pnovaczek.spellgems.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.MenuTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.screen.SpellDispenserMenu;

import java.util.List;

public class SpellDispenserScreen extends AbstractContainerScreen<SpellDispenserMenu> {

    private static final int BACKGROUND_TEXTURE_WIDTH = 256;
    private static final int BACKGROUND_TEXTURE_HEIGHT = 256;

    private static final Identifier CONTAINER_TEXTURE =
            Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "textures/gui/container/spell_dispenser.png");
    private static final Identifier COOLDOWN_BAR_SPRITE =
            Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "container/spell_dispenser/cooldown_bar");
    private static final Identifier DISABLED_BAR_SPRITE =
            Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "container/spell_dispenser/disabled_bar");

    /** Blueprint: cooldown_bar at left 163, top 16 */
    private static final int BAR_X = 163;
    private static final int BAR_Y = 16;
    private static final int BAR_WIDTH = 6;
    private static final int BAR_HEIGHT = 54;

    public SpellDispenserScreen(SpellDispenserMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
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

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                CONTAINER_TEXTURE,
                xo,
                yo,
                0.0F,
                0.0F,
                this.imageWidth,
                this.imageHeight,
                BACKGROUND_TEXTURE_WIDTH,
                BACKGROUND_TEXTURE_HEIGHT
        );

        if (this.menu.isBurnedOut()) {
            drawVerticalBar(
                    graphics,
                    DISABLED_BAR_SPRITE,
                    xo + BAR_X,
                    yo + BAR_Y,
                    this.menu.getBurnoutRemaining(),
                    this.menu.getBurnoutMax()
            );
        } else if (this.menu.getCooldownRemaining() > 0) {
            drawVerticalBar(
                    graphics,
                    COOLDOWN_BAR_SPRITE,
                    xo + BAR_X,
                    yo + BAR_Y,
                    this.menu.getCooldownRemaining(),
                    this.menu.getCooldownMax()
            );
        }
    }

    /**
     * Draws a top-down shrinking bar: full when remaining == max, empty when remaining == 0.
     */
    private static void drawVerticalBar(
            GuiGraphicsExtractor graphics,
            Identifier sprite,
            int x,
            int y,
            int remaining,
            int max
    ) {
        if (remaining <= 0 || max <= 0) {
            return;
        }
        int height = Mth.clamp(Mth.ceil(remaining * (float) BAR_HEIGHT / (float) max), 1, BAR_HEIGHT);
        // Draw from the top of the bar region downward
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                sprite,
                BAR_WIDTH,
                BAR_HEIGHT,
                0,
                0,
                x,
                y,
                BAR_WIDTH,
                height
        );
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        this.extractTooltip(graphics, mouseX, mouseY);

        int xo = (this.width - this.imageWidth) / 2;
        int yo = (this.height - this.imageHeight) / 2;
        int barX = xo + BAR_X;
        int barY = yo + BAR_Y;

        if (mouseX < barX || mouseX >= barX + BAR_WIDTH || mouseY < barY || mouseY >= barY + BAR_HEIGHT) {
            return;
        }

        Component text;
        if (this.menu.isBurnedOut()) {
            int remaining = this.menu.getBurnoutRemaining();
            text = Component.translatable("tooltip.spellgems.spell_dispenser.burnout", formatTicks(remaining));
        } else {
            int remaining = this.menu.getCooldownRemaining();
            int max = this.menu.getCooldownMax();
            if (remaining > 0) {
                text = Component.translatable("tooltip.spellgems.spell_dispenser.cooldown", formatTicks(remaining));
            } else if (max > 0) {
                text = Component.translatable("tooltip.spellgems.spell_dispenser.ready");
            } else {
                text = Component.translatable("tooltip.spellgems.spell_dispenser.ready");
            }
        }

        ClientTooltipComponent tooltip = new ClientTextTooltip(
                FormattedCharSequence.forward(text.getString(), Style.EMPTY)
        );
        graphics.tooltip(
                this.font,
                List.of(tooltip),
                mouseX,
                mouseY,
                new MenuTooltipPositioner(new ScreenRectangle(mouseX, mouseY, 1, 1)),
                null
        );
    }

    private static String formatTicks(int ticks) {
        if (ticks >= 20) {
            float seconds = ticks / 20.0F;
            if (seconds >= 10) {
                return String.format("%.0fs", seconds);
            }
            return String.format("%.1fs", seconds);
        }
        return ticks + "t";
    }
}
