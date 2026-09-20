package net.pnovaczek.spellgems.client.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.pnovaczek.spellgems.ModItems;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.recipe.TransmuteRecipe;
import org.jspecify.annotations.Nullable;

public class TransmuteRecipeCategory implements IRecipeCategory<RecipeHolder<TransmuteRecipe>> {

    @SuppressWarnings("unchecked")
    public static final IRecipeType<RecipeHolder<TransmuteRecipe>> TYPE =
            IRecipeType.create(Spellgems.MOD_ID, "transmuting", (Class<RecipeHolder<TransmuteRecipe>>) (Class<?>) RecipeHolder.class);

    private static final Identifier BACKGROUND_LOCATION =
            Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "textures/gui/jei/transmute.png");

    private final IDrawable background;
    private final IDrawable icon;

    public TransmuteRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(BACKGROUND_LOCATION, 0, 0, 140, 32)
                .setTextureSize(140,
                        32)
                .build();
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModItems.SPELL_GEM_TRANSMUTE));
    }

    @Override
    public IRecipeType<RecipeHolder<TransmuteRecipe>> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("tooltip.spellgems.spell.transmute.name");
    }

    @Override
    public int getWidth() {
        return 140;
    }

    @Override
    public int getHeight() {
        return 32;
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
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<TransmuteRecipe> recipeHolder, IFocusGroup focuses) {
        TransmuteRecipe recipe = recipeHolder.value();
        builder.addSlot(RecipeIngredientRole.INPUT, 3, 8)
                .add(new ItemStack(ModItems.SPELL_GEM_TRANSMUTE));
        builder.addSlot(RecipeIngredientRole.INPUT, 67, 8)
                .add(recipe.getIngredient());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 121, 8)
                .add(recipe.getResult().create());
    }

    @Override
    public void draw(RecipeHolder<TransmuteRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView,
                     GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        this.background.draw(guiGraphics);
    }
}
