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

    /** Mapped from container (79, 34) via the same slot offset as JEI inputs/output. */
    private static final int PROGRESS_X = 41;
    private static final int PROGRESS_Y = 22;
    private static final int PROGRESS_WIDTH = 24;
    private static final int PROGRESS_HEIGHT = 16;
    /** Default craft length (matches most recipes / block-entity default). */
    private static final int PROGRESS_ANIMATION_TICKS = 200;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated progressArrow;

    public ManaInfuserRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(BACKGROUND_LOCATION, 0, 0, 116, 76)
                .setTextureSize(116, 76)
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
        return 116;
    }

    @Override
    public int getHeight() {
        return 76;
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
        builder.addSlot(RecipeIngredientRole.INPUT, 18, 5)
                .add(recipe.getInfusingItem());

        // item to infuse (bottom, like amethyst/iron)
        builder.addSlot(RecipeIngredientRole.INPUT, 18, 41)
                .add(recipe.getInfusedItem());

        // Output
        builder.addSlot(RecipeIngredientRole.OUTPUT, 78, 23)
                .add(recipe.getResult().create());
    }

    @Override
    public void draw(RecipeHolder<ManaInfuserRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView,
                     GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        // Draw custom background first (JEI draws its default border before this, we disabled border)
        this.background.draw(guiGraphics);

        // Horizontal progress arrow (fills left→right), same asset/motion as the crafting GUI
        this.progressArrow.draw(guiGraphics, PROGRESS_X, PROGRESS_Y);

        ManaInfuserRecipe recipe = recipeHolder.value();

        // Position the cost text near the bottom of the background area (slots end ~y=63)
        Component mana = Component.translatable("tooltip.spellgems.mana_infuser.mana_level", recipe.getManaCost());
        guiGraphics.text(Minecraft.getInstance().font, mana, 4, 66, 0xFF2424DA, false);
    }
}