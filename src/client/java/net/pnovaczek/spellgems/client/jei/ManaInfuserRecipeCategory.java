package net.pnovaczek.spellgems.client.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.pnovaczek.spellgems.ModBlocks;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.recipe.ManaInfuserRecipe;
import org.jspecify.annotations.Nullable;

public class ManaInfuserRecipeCategory implements IRecipeCategory<RecipeHolder<ManaInfuserRecipe>> {
    @Override
    public IRecipeType<RecipeHolder<ManaInfuserRecipe>> getRecipeType() {
        return null;
    }

    @Override
    public Component getTitle() {
        return null;
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return null;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<ManaInfuserRecipe> recipe, IFocusGroup focuses) {

    }
//
//    public static final IRecipeType<RecipeHolder<ManaInfuserRecipe>> TYPE =
//            IRecipeType.create(Spellgems.MOD_ID, "mana_infuser", (Class<RecipeHolder<ManaInfuserRecipe>>)(Class<?>) RecipeHolder.class);
//
//    private final IDrawable background;
//    private final IDrawable icon;
//
//    public ManaInfuserRecipeCategory(IGuiHelper guiHelper) {
//        this.background = guiHelper.createBlankDrawable(110, 65);
//        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.MANA_INFUSER));
//    }
//
//    @Override
//    public IRecipeType<RecipeHolder<ManaInfuserRecipe>> getRecipeType() {
//        return TYPE;
//    }
//
//    @Override
//    public Component getTitle() {
//        return Component.translatable("block.spellgems.mana_infuser");
//    }
//
//    @Override
//    public int getWidth() {
//        return 200;
//    }
//
//    @Override
//    public int getHeight() {
//        return 200;
//    }
//
//    @Override
//    public IDrawable getIcon() {
//        return icon;
//    }
//
//    @Override
//    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<ManaInfuserRecipe> recipeHolder, IFocusGroup focuses) {
//        ManaInfuserRecipe recipe = recipeHolder.value();
//
//        // Top slot: infusing ingredient
//        builder.addSlot(RecipeIngredientRole.INPUT, 15, 8)
//                .add(recipe.getInfusingItem());
//
//        // Bottom slot: item to infuse
//        builder.addSlot(RecipeIngredientRole.INPUT, 15, 33)
//                .add(recipe.getInfusedItem());
//
//        // Output slot
//        builder.addSlot(RecipeIngredientRole.OUTPUT, 80, 20)
//                .add(recipe.getResult());
//    }
//
//    @Override
//    public void draw(RecipeHolder<ManaInfuserRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView,
//                     GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
//        ManaInfuserRecipe recipe = recipeHolder.value();
//
//        // Mana cost
//        Component mana = Component.literal(recipe.getManaCost() + " Mana");
//        guiGraphics.text(Minecraft.getInstance().font, mana, 5, 55, 0xAA00AA, false);
//
//        // Processing time
//        int seconds = recipe.getProcessingTime() / 20;
//        Component time = Component.literal(seconds + "s");
//        guiGraphics.text(Minecraft.getInstance().font, time, 85, 55, 0x555555, false);
//    }
}