package net.pnovaczek.spellgems.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.screen.SpellEnchantingMenu;

import com.google.common.collect.Lists;
import java.util.List;

public class SpellEnchantingScreen extends AbstractContainerScreen<SpellEnchantingMenu> {

    // Use the vanilla enchanting table GUI background + button sprites for consistent look
    private static final Identifier ENCHANTING_TABLE_LOCATION =
            Identifier.withDefaultNamespace("textures/gui/container/enchanting_table.png");
    private static final Identifier ENCHANTING_BOOK_LOCATION =
            Identifier.withDefaultNamespace("textures/entity/enchantment/enchanting_table_book.png");

    private static final Identifier ENCHANTMENT_SLOT_DISABLED_SPRITE =
            Identifier.withDefaultNamespace("container/enchanting_table/enchantment_slot_disabled");
    private static final Identifier ENCHANTMENT_SLOT_HIGHLIGHTED_SPRITE =
            Identifier.withDefaultNamespace("container/enchanting_table/enchantment_slot_highlighted");
    private static final Identifier ENCHANTMENT_SLOT_SPRITE =
            Identifier.withDefaultNamespace("container/enchanting_table/enchantment_slot");

    private static final Identifier[] ENABLED_LEVEL_SPRITES = new Identifier[]{
            Identifier.withDefaultNamespace("container/enchanting_table/level_1"),
            Identifier.withDefaultNamespace("container/enchanting_table/level_2"),
            Identifier.withDefaultNamespace("container/enchanting_table/level_3")
    };
    private static final Identifier[] DISABLED_LEVEL_SPRITES = new Identifier[]{
            Identifier.withDefaultNamespace("container/enchanting_table/level_1_disabled"),
            Identifier.withDefaultNamespace("container/enchanting_table/level_2_disabled"),
            Identifier.withDefaultNamespace("container/enchanting_table/level_3_disabled")
    };

    private final RandomSource random = RandomSource.create();
    private BookModel bookModel;
    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public float open;
    public float oOpen;
    private ItemStack last = ItemStack.EMPTY;

    // Vanilla 3-button layout constants
    private static final int BUTTON_X = 60;
    private static final int BUTTON_WIDTH = 108;
    private static final int BUTTON_HEIGHT = 19;
    private static final int BUTTON_START_Y = 14;
    private static final int BUTTON_SPACING = 19;

    public SpellEnchantingScreen(SpellEnchantingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.bookModel = new BookModel(this.minecraft.getEntityModels().bakeLayer(ModelLayers.BOOK));
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.minecraft.player.experienceDisplayStartTick = this.minecraft.player.tickCount;
        this.tickBook();
    }

    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;

        // Draw the vanilla enchanting table background
        graphics.blit(RenderPipelines.GUI_TEXTURED, ENCHANTING_TABLE_LOCATION, left, top, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

        // Draw the animated book (same as vanilla enchanting table)
        this.renderBook(graphics, left, top);

        ItemStack target = this.menu.getSlot(0).getItem();

        // Three buttons in vanilla layout
        for (int i = 0; i < 3; i++) {
            int btnX = left + BUTTON_X;
            int btnY = top + BUTTON_START_Y + BUTTON_SPACING * i;

            int req = this.menu.getLevelRequirement(i);
            int cost = this.menu.getXpCost(i);
            boolean hasPreview = req > 0 || cost > 0;

            boolean hovered = isHovering(BUTTON_X, BUTTON_START_Y + BUTTON_SPACING * i, BUTTON_WIDTH, BUTTON_HEIGHT, mouseX, mouseY);

            // Determine if this button should be active based on gem type (user requirement)
            boolean buttonAllowedByType = isButtonAllowedForCurrentItem(i, target);

            boolean canAfford = this.minecraft.player.experienceLevel >= req && this.minecraft.player.totalExperience >= cost;
            boolean active = hasPreview && buttonAllowedByType;

            Identifier slotSprite;
            if (!active) {
                slotSprite = ENCHANTMENT_SLOT_DISABLED_SPRITE;
            } else if (hovered) {
                slotSprite = ENCHANTMENT_SLOT_HIGHLIGHTED_SPRITE;
            } else {
                slotSprite = ENCHANTMENT_SLOT_SPRITE;
            }

            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, slotSprite, btnX, btnY, BUTTON_WIDTH, BUTTON_HEIGHT);

            if (active) {
                // Level indicator sprite (use the button index for visual variety, like vanilla)
                Identifier levelSprite = canAfford ? ENABLED_LEVEL_SPRITES[i] : DISABLED_LEVEL_SPRITES[i];
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, levelSprite, btnX + 1, btnY + 1, 16, 16);

                int leftPosText = btnX + 20;
                int col;
                if (!canAfford) {
                    col = -12550384;
                } else if (hovered) {
                    col = -128;
                } else {
                    col = -9937334;
                }

                // Level requirement number (upper)
                String reqText = String.valueOf(req);
                graphics.text(this.font, reqText, leftPosText, btnY + 2, col);

                // XP cost number (lower, right aligned)
                String costText = String.valueOf(cost);
                int costX = leftPosText + 66 - this.font.width(costText);
                int costCol = (!canAfford) ? -12550384 : (hovered ? -128 : -8323296);
                graphics.text(this.font, costText, costX, btnY + 9, costCol);
            }
        }
    }

    private void renderBook(GuiGraphicsExtractor graphics, int left, int top) {
        float partial = this.minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float open = Mth.lerp(partial, this.oOpen, this.open);
        float flip = Mth.lerp(partial, this.oFlip, this.flip);
        int x0 = left + 14;
        int y0 = top + 14;
        int x1 = x0 + 38;
        int y1 = y0 + 31;
        graphics.book(this.bookModel, ENCHANTING_BOOK_LOCATION, 40.0F, open, flip, x0, y0, x1, y1);
    }

    private boolean isButtonAllowedForCurrentItem(int buttonIndex, ItemStack target) {
        if (target.isEmpty()) return false;

        // Top button (0) → utility spell gems
        if (buttonIndex == 0) {
            return target.is(net.pnovaczek.spellgems.ModTags.UTILITY_SPELL_GEMS);
        }
        // Middle button (1) → combat spell gems
        if (buttonIndex == 1) {
            return target.is(net.pnovaczek.spellgems.ModTags.COMBAT_SPELL_GEMS);
        }
        // Bottom button (2) → disabled for now (future: catalyst books / tomes)
        return false;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float ignored) {
        super.extractRenderState(graphics, mouseX, mouseY, ignored);

        ItemStack target = this.menu.getSlot(0).getItem();

        for (int i = 0; i < 3; i++) {
            int btnY = BUTTON_START_Y + BUTTON_SPACING * i;

            if (!isHovering(BUTTON_X, btnY, BUTTON_WIDTH, BUTTON_HEIGHT, mouseX, mouseY)) {
                continue;
            }

            int req = this.menu.getLevelRequirement(i);
            int cost = this.menu.getXpCost(i);
            if (req <= 0 && cost <= 0) {
                continue;
            }

            // Only show rich tooltip for the button that is allowed for this item type
            if (!isButtonAllowedForCurrentItem(i, target)) {
                continue;
            }

            List<Component> texts = Lists.<Component>newArrayList();
            if (this.minecraft.player.experienceLevel < req) {
                texts.add(
                        Component.translatable("container.enchant.level.requirement", req)
                                .withStyle(ChatFormatting.RED)
                );
            } else {
                texts.add(Component.literal(cost + " XP").withStyle(ChatFormatting.GRAY));
            }
            graphics.setComponentTooltipForNextFrame(this.font, texts, mouseX, mouseY);
            break; // only one tooltip at a time
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int xo = (this.width - this.imageWidth) / 2;
        int yo = (this.height - this.imageHeight) / 2;

        // Check all three buttons (vanilla layout)
        for (int i = 0; i < 3; i++) {
            double xx = event.x() - (xo + BUTTON_X);
            double yy = event.y() - (yo + BUTTON_START_Y + BUTTON_SPACING * i);

            if (xx >= 0.0 && yy >= 0.0 && xx < BUTTON_WIDTH && yy < BUTTON_HEIGHT) {
                // Only allow click if the button is allowed for the current item type
                ItemStack target = this.menu.getSlot(0).getItem();
                if (isButtonAllowedForCurrentItem(i, target)) {
                    if (this.menu.clickMenuButton(this.minecraft.player, i)) {
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, i);
                        return true;
                    }
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    public void tickBook() {
        ItemStack current = this.menu.getSlot(0).getItem();
        if (!ItemStack.matches(current, this.last)) {
            this.last = current;

            do {
                this.flipT = this.flipT + (this.random.nextInt(4) - this.random.nextInt(4));
            } while (this.flip <= this.flipT + 1.0F && this.flip >= this.flipT - 1.0F);
        }

        this.oFlip = this.flip;
        this.oOpen = this.open;
        boolean shouldBeOpen = false;

        for (int i = 0; i < 3; i++) {
            if (this.menu.getXpCost(i) != 0) {
                shouldBeOpen = true;
                break;
            }
        }

        if (shouldBeOpen) {
            this.open += 0.2F;
        } else {
            this.open -= 0.2F;
        }

        this.open = Mth.clamp(this.open, 0.0F, 1.0F);
        float diff = (this.flipT - this.flip) * 0.4F;
        float max = 0.2F;
        diff = Mth.clamp(diff, -0.2F, 0.2F);
        this.flipA = this.flipA + (diff - this.flipA) * 0.9F;
        this.flip = this.flip + this.flipA;
    }
}