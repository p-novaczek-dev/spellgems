package net.pnovaczek.spellgems.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.pnovaczek.spellgems.screen.SpellEnchantingMenu;
import net.pnovaczek.spellgems.spell.enchantment.PotionEnchantments;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;

public class SpellEnchantingScreen extends AbstractContainerScreen<SpellEnchantingMenu> {

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

    private static final Identifier SCROLLER_SPRITE =
            Identifier.withDefaultNamespace("widget/scroller");
    private static final Identifier SCROLLER_BACKGROUND_SPRITE =
            Identifier.withDefaultNamespace("widget/scroller_background");

    private static final int BUTTON_X = 60;
    private static final int BUTTON_WIDTH = 108;
    private static final int BUTTON_HEIGHT = 19;
    private static final int BUTTON_START_Y = 14;
    private static final int BUTTON_SPACING = 19;
    private static final int VISIBLE_BUTTON_COUNT = 3;
    private static final int SCROLLBAR_WIDTH = 6;

    private final RandomSource random = RandomSource.create();
    private BookModel bookModel;
    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public float open;
    public float oOpen;
    private ItemStack last = ItemStack.EMPTY;
    private int scrollOffset;

    public SpellEnchantingScreen(SpellEnchantingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.bookModel = new BookModel(this.minecraft.getEntityModels().bakeLayer(ModelLayers.BOOK));
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

        graphics.blit(RenderPipelines.GUI_TEXTURED, ENCHANTING_TABLE_LOCATION, left, top, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        this.renderBook(graphics, left, top);

        int recipeCount = this.menu.getRecipeCount();
        boolean scrollable = recipeCount > VISIBLE_BUTTON_COUNT;
        int buttonWidth = scrollable ? BUTTON_WIDTH - SCROLLBAR_WIDTH : BUTTON_WIDTH;

        for (int visibleIndex = 0; visibleIndex < VISIBLE_BUTTON_COUNT; visibleIndex++) {
            int recipeIndex = this.scrollOffset + visibleIndex;
            int btnX = left + BUTTON_X;
            int btnY = top + BUTTON_START_Y + BUTTON_SPACING * visibleIndex;

            if (recipeIndex >= recipeCount) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_SLOT_DISABLED_SPRITE, btnX, btnY, buttonWidth, BUTTON_HEIGHT);
                continue;
            }

            int req = this.menu.getLevelRequirement(recipeIndex);
            boolean hovered = isHoveringRecipeButton(visibleIndex, mouseX, mouseY);
            boolean canAfford = meetsLevelRequirement(recipeIndex) && meetsXpRequirement(recipeIndex) && meetsCatalystRequirement(recipeIndex);

            Identifier slotSprite;
            if (!canAfford) {
                slotSprite = ENCHANTMENT_SLOT_DISABLED_SPRITE;
            } else if (hovered) {
                slotSprite = ENCHANTMENT_SLOT_HIGHLIGHTED_SPRITE;
            } else {
                slotSprite = ENCHANTMENT_SLOT_SPRITE;
            }

            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, slotSprite, btnX, btnY, buttonWidth, BUTTON_HEIGHT);

            String reqText = String.valueOf(req);
            int leftPosText = btnX + 20;
            int textWidth = 86 - this.font.width(reqText);
            EnchantmentNames.getInstance().initSeed(this.minecraft.player.getEnchantmentSeed() + recipeIndex);
            FormattedText glyphName = EnchantmentNames.getInstance().getRandomName(this.font, textWidth);

            int nameCol = -9937334;
            if (!canAfford) {
                graphics.textWithWordWrap(
                        this.font, glyphName, leftPosText, btnY + 2, textWidth,
                        ARGB.opaque((nameCol & 16711422) >> 1), false
                );
            } else {
                graphics.textWithWordWrap(
                        this.font, glyphName, leftPosText, btnY + 2, textWidth,
                        hovered ? -128 : nameCol, false
                );
            }

            int reqX = leftPosText + 86 - this.font.width(reqText);
            int col = !canAfford ? -12550384 : (hovered ? -128 : -8323296);
            graphics.text(this.font, reqText, reqX, btnY + 9, col);
        }

        if (scrollable) {
            renderScrollbar(graphics, left, top, mouseX, mouseY);
        }
    }

    private void renderScrollbar(GuiGraphicsExtractor graphics, int left, int top, int mouseX, int mouseY) {
        int scrollbarX = left + BUTTON_X + BUTTON_WIDTH - SCROLLBAR_WIDTH;
        int scrollbarY = top + BUTTON_START_Y;
        int scrollbarHeight = BUTTON_SPACING * VISIBLE_BUTTON_COUNT;
        int maxScroll = maxScrollOffset();
        int scrollerHeight = Math.max(32, scrollbarHeight * VISIBLE_BUTTON_COUNT / Math.max(VISIBLE_BUTTON_COUNT, this.menu.getRecipeCount()));
        int scrollerY = maxScroll == 0
                ? scrollbarY
                : scrollbarY + this.scrollOffset * (scrollbarHeight - scrollerHeight) / maxScroll;

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_BACKGROUND_SPRITE, scrollbarX, scrollbarY, SCROLLBAR_WIDTH, scrollbarHeight);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_SPRITE, scrollbarX, scrollerY, SCROLLBAR_WIDTH, scrollerHeight);
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

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float ignored) {
        super.extractRenderState(graphics, mouseX, mouseY, ignored);

        for (int visibleIndex = 0; visibleIndex < VISIBLE_BUTTON_COUNT; visibleIndex++) {
            int recipeIndex = this.scrollOffset + visibleIndex;
            if (recipeIndex >= this.menu.getRecipeCount()) {
                continue;
            }

            if (!isHoveringRecipeButton(visibleIndex, mouseX, mouseY)) {
                continue;
            }

            List<Component> texts = buildRecipeTooltip(recipeIndex);
            graphics.setComponentTooltipForNextFrame(this.font, texts, mouseX, mouseY);
            break;
        }
    }

    private List<Component> buildRecipeTooltip(int recipeIndex) {
        List<Component> texts = Lists.newArrayList();

        String descriptionKey = this.menu.getRecipeDescriptionKey(recipeIndex);
        if (!descriptionKey.isEmpty()) {
            texts.add(Component.translatable(descriptionKey).withStyle(ChatFormatting.WHITE));
            texts.add(CommonComponents.EMPTY);
        }

        int levelRequirement = this.menu.getLevelRequirement(recipeIndex);
        if (!meetsLevelRequirement(recipeIndex)) {
            texts.add(Component.translatable("container.spellgems.spell_enchanting.level_requirement", levelRequirement)
                    .withStyle(ChatFormatting.RED));
        } else {
            texts.add(Component.translatable("container.spellgems.spell_enchanting.level_requirement", levelRequirement)
                    .withStyle(ChatFormatting.GRAY));
        }

        String relativeLevelCost = formatRelativeLevelCost(this.menu.getXpCost(recipeIndex));
        if (!meetsXpRequirement(recipeIndex)) {
            texts.add(Component.translatable("container.spellgems.spell_enchanting.level_cost", relativeLevelCost)
                    .withStyle(ChatFormatting.RED));
        } else {
            texts.add(Component.translatable("container.spellgems.spell_enchanting.level_cost", relativeLevelCost)
                    .withStyle(ChatFormatting.GRAY));
        }

        int catalystCount = this.menu.getCatalystCount(recipeIndex);
        MutableComponent catalystLabel;
        if (this.menu.getCatalystKind(recipeIndex) == SpellEnchantingMenu.CATALYST_KIND_ANY_POTION) {
            catalystLabel = Component.translatable(
                    "container.spellgems.spell_enchanting.catalyst.any_potion",
                    catalystCount
            );
        } else {
            Item catalystItem = Item.byId(this.menu.getCatalystItemId(recipeIndex));
            catalystLabel = Component.translatable(
                    "container.spellgems.spell_enchanting.catalyst",
                    catalystCount,
                    catalystItem.getName(new ItemStack(catalystItem))
            );
        }
        texts.add(meetsCatalystRequirement(recipeIndex)
                ? catalystLabel.withStyle(ChatFormatting.GRAY)
                : catalystLabel.withStyle(ChatFormatting.RED));

        return texts;
    }

    private String formatRelativeLevelCost(int xpCost) {
        int xpPerLevel = this.minecraft.player.getXpNeededForNextLevel();
        double relativeLevels = (double) xpCost / xpPerLevel;
        return String.format(Locale.ROOT, "%.1f", relativeLevels);
    }

    private boolean meetsLevelRequirement(int recipeIndex) {
        return this.minecraft.player.experienceLevel >= this.menu.getLevelRequirement(recipeIndex);
    }

    private boolean meetsXpRequirement(int recipeIndex) {
        return this.minecraft.player.totalExperience >= this.menu.getXpCost(recipeIndex);
    }

    private boolean meetsCatalystRequirement(int recipeIndex) {
        ItemStack catalystStack = this.menu.getSlot(1).getItem();
        int catalystCount = this.menu.getCatalystCount(recipeIndex);
        if (catalystStack.isEmpty() || catalystStack.getCount() < catalystCount) {
            return false;
        }
        if (this.menu.getCatalystKind(recipeIndex) == SpellEnchantingMenu.CATALYST_KIND_ANY_POTION) {
            return PotionEnchantments.isValidCatalyst(catalystStack);
        }
        Item catalystItem = Item.byId(this.menu.getCatalystItemId(recipeIndex));
        return catalystStack.is(catalystItem);
    }

    private boolean isHoveringRecipeButton(int visibleIndex, int mouseX, int mouseY) {
        int buttonWidth = this.menu.getRecipeCount() > VISIBLE_BUTTON_COUNT
                ? BUTTON_WIDTH - SCROLLBAR_WIDTH
                : BUTTON_WIDTH;
        return isHovering(BUTTON_X, BUTTON_START_Y + BUTTON_SPACING * visibleIndex, buttonWidth, BUTTON_HEIGHT, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int xo = (this.width - this.imageWidth) / 2;
        int yo = (this.height - this.imageHeight) / 2;

        for (int visibleIndex = 0; visibleIndex < VISIBLE_BUTTON_COUNT; visibleIndex++) {
            int recipeIndex = this.scrollOffset + visibleIndex;
            if (recipeIndex >= this.menu.getRecipeCount()) {
                continue;
            }

            int buttonWidth = this.menu.getRecipeCount() > VISIBLE_BUTTON_COUNT
                    ? BUTTON_WIDTH - SCROLLBAR_WIDTH
                    : BUTTON_WIDTH;
            double xx = event.x() - (xo + BUTTON_X);
            double yy = event.y() - (yo + BUTTON_START_Y + BUTTON_SPACING * visibleIndex);

            if (xx >= 0.0 && yy >= 0.0 && xx < buttonWidth && yy < BUTTON_HEIGHT) {
                if (this.menu.clickMenuButton(this.minecraft.player, recipeIndex)) {
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, recipeIndex);
                    return true;
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.menu.getRecipeCount() > VISIBLE_BUTTON_COUNT
                && isHovering(BUTTON_X, BUTTON_START_Y, BUTTON_WIDTH, BUTTON_SPACING * VISIBLE_BUTTON_COUNT, mouseX, mouseY)) {
            int previousOffset = this.scrollOffset;
            this.scrollOffset = Mth.clamp(this.scrollOffset - (int) Math.signum(scrollY), 0, maxScrollOffset());
            return this.scrollOffset != previousOffset;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private int maxScrollOffset() {
        return Math.max(0, this.menu.getRecipeCount() - VISIBLE_BUTTON_COUNT);
    }

    public void tickBook() {
        this.scrollOffset = Mth.clamp(this.scrollOffset, 0, maxScrollOffset());

        ItemStack current = this.menu.getSlot(0).getItem();
        if (!ItemStack.matches(current, this.last)) {
            this.last = current;
            this.scrollOffset = 0;

            do {
                this.flipT = this.flipT + (this.random.nextInt(4) - this.random.nextInt(4));
            } while (this.flip <= this.flipT + 1.0F && this.flip >= this.flipT - 1.0F);
        }

        this.oFlip = this.flip;
        this.oOpen = this.open;
        boolean shouldBeOpen = this.menu.getRecipeCount() > 0;

        if (shouldBeOpen) {
            this.open += 0.2F;
        } else {
            this.open -= 0.2F;
        }

        this.open = Mth.clamp(this.open, 0.0F, 1.0F);
        float diff = (this.flipT - this.flip) * 0.4F;
        diff = Mth.clamp(diff, -0.2F, 0.2F);
        this.flipA = this.flipA + (diff - this.flipA) * 0.9F;
        this.flip = this.flip + this.flipA;
    }
}