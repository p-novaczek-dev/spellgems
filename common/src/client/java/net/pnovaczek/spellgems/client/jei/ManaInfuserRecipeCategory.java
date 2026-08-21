package net.pnovaczek.spellgems.client.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.pnovaczek.spellgems.ModBlocks;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.recipe.ManaInfuserRecipe;
import org.jspecify.annotations.Nullable;

public class ManaInfuserRecipeCategory implements IRecipeCategory<RecipeHolder<ManaInfuserRecipe>> {

    @SuppressWarnings("unchecked")
    public static final IRecipeType<RecipeHolder<ManaInfuserRecipe>> TYPE =
            IRecipeType.create(Spellgems.MOD_ID, "mana_infusing", (Class<RecipeHolder<ManaInfuserRecipe>>)(Class<?>) RecipeHolder.class);

    private static final Identifier BACKGROUND_LOCATION =
            Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "textures/gui/jei/mana_infuser.png");
    /** Same progress sprite used by {@link net.pnovaczek.spellgems.client.screen.ManaInfuserScreen}. */
    private static final Identifier PROGRESS_TEXTURE =
            Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "textures/gui/sprites/container/mana_infuser/progress.png");

    private static final int PROGRESS_X = 26;
    private static final int PROGRESS_Y = 20;
    private static final int PROGRESS_WIDTH = 24;
    private static final int PROGRESS_HEIGHT = 16;
    /** Default craft length (matches most recipes / block-entity default). */
    private static final int PROGRESS_ANIMATION_TICKS = 200;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated progressArrow;

    public ManaInfuserRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(BACKGROUND_LOCATION, 0, 0, 86, 58)
                .setTextureSize(86, 58)
                .build();
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.MANA_INFUSER));

        IDrawableStatic progressFull = guiHelper.drawableBuilder(PROGRESS_TEXTURE, 0, 0, PROGRESS_WIDTH, PROGRESS_HEIGHT)
                .setTextureSize(PROGRESS_WIDTH, PROGRESS_HEIGHT)
                .build();
        this.progressArrow = guiHelper.createAnimatedDrawable(
                progressFull,
                PROGRESS_ANIMATION_TICKS,
                IDrawableAnimated.StartDirection.LEFT,
                false
        );
    }

    @Override
    public IRecipeType<RecipeHolder<ManaInfuserRecipe>> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.spellgems.mana_infuser");
    }

    @Override
    public int getWidth() {
        return 86;
    }

    @Override
    public int getHeight() {
        return 58;
    }

    @Override
    public boolean needsRecipeBorder() {
        return true;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<ManaInfuserRecipe> recipeHolder, IFocusGroup focuses) {
        ManaInfuserRecipe recipe = recipeHolder.value();

        // Layout (shifted for JEI 0,0):
        // infusing ingredient (top, like lapis/coal)
        builder.addSlot(RecipeIngredientRole.INPUT, 3, 3)
                .add(recipe.getInfusingItem());

        // item to infuse (bottom, like amethyst/iron)
        builder.addSlot(RecipeIngredientRole.INPUT, 3, 39)
                .add(recipe.getInfusedItem());

        // Output
        builder.addSlot(RecipeIngredientRole.OUTPUT, 63, 21)
                .add(recipe.getResult().create());
    }

    @Override
    public void draw(RecipeHolder<ManaInfuserRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView,
                     GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        this.background.draw(guiGraphics);

        // Horizontal progress arrow (fills left→right), same asset/motion as the crafting GUI
        this.progressArrow.draw(guiGraphics, PROGRESS_X, PROGRESS_Y);

        ManaInfuserRecipe recipe = recipeHolder.value();
        var font = Minecraft.getInstance().font;
        int textColor = 0xFF8B8B8B;
        int textPad = 3;

        Component mana = Component.translatable("tooltip.spellgems.mana_infuser.mana_level", recipe.getManaCost());
        guiGraphics.text(font, mana, getWidth() - font.width(mana) - textPad, 3, 0xFF4824DA, false);

        int seconds = recipe.getProcessingTime() / 20;
        Component duration = Component.translatable("tooltip.spellgems.mana_infuser.duration", seconds);
        guiGraphics.text(font, duration, getWidth() - font.width(duration) - textPad, 48, textColor, false);
    }
}