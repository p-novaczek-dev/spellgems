package net.pnovaczek.spellgems.client.jei;

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
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.pnovaczek.spellgems.ModBlocks;
import net.pnovaczek.spellgems.ModComponents;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.item.GreaterSpellGems;
import net.pnovaczek.spellgems.item.data.SpellGemData;
import net.pnovaczek.spellgems.recipe.GemForgeRecipe;
import net.pnovaczek.spellgems.spell.SpellIds;
import org.jspecify.annotations.Nullable;

public class GemForgeRecipeCategory implements IRecipeCategory<RecipeHolder<GemForgeRecipe>> {

    @SuppressWarnings("unchecked")
    public static final IRecipeType<RecipeHolder<GemForgeRecipe>> TYPE =
            IRecipeType.create(Spellgems.MOD_ID, "gem_forging", (Class<RecipeHolder<GemForgeRecipe>>) (Class<?>) RecipeHolder.class);

    private static final Identifier BACKGROUND_LOCATION =
            Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "textures/gui/jei/gem_forge.png");

    private final IDrawable background;
    private final IDrawable icon;

    public GemForgeRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(BACKGROUND_LOCATION, 0, 0, 112, 32)
                .setTextureSize(112, 32)
                .build();
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.GEM_FORGE));
    }

    @Override
    public IRecipeType<RecipeHolder<GemForgeRecipe>> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.spellgems.gem_forge");
    }

    @Override
    public int getWidth() {
        return 112;
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
    @SuppressWarnings("deprecation") // Ingredient.items() is the only item listing both loaders share. NeoForge's getValues() is not on Fabric.
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<GemForgeRecipe> recipeHolder, IFocusGroup focuses) {
        GemForgeRecipe recipe = recipeHolder.value();
        var input1 = builder.addSlot(RecipeIngredientRole.INPUT, 3, 3);
        if (recipe.getOperation() == GemForgeRecipe.Operation.UNSOCKET_WEAPON) {
            recipe.getIngredient1().items().forEach(holder -> {
                ItemStack socketed = new ItemStack(holder);
                socketed.set(ModComponents.SPELL_GEM_DATA, SpellGemData.create(SpellIds.NOVA));
                input1.add(socketed);
            });
        } else {
            input1.add(recipe.getIngredient1());
        }
        recipe.getIngredient2().ifPresent(ingredient ->
                builder.addSlot(RecipeIngredientRole.INPUT, 39, 3).add(ingredient));
        var output = builder.addSlot(RecipeIngredientRole.OUTPUT, 93, 3);
        if (recipe.getOperation() == GemForgeRecipe.Operation.COMBINE_GREATER) {
            GreaterSpellGems.allCombinationStacks().forEach(output::add);
        } else if (recipe.getStaticResult().isPresent()) {
            output.add(recipe.getStaticResult().get().create());
        } else {
            output.add(recipe.getIngredient1());
        }
    }

    @Override
    public void draw(RecipeHolder<GemForgeRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView,
                     GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        this.background.draw(guiGraphics);

        int xp = recipeHolder.value().getXpCost();
        if (xp <= 0) {
            return;
        }
        Component xpCost = Component.translatable("container.spellgems.spell_enchanting.xp_cost", xp);
        guiGraphics.text(Minecraft.getInstance().font, xpCost, 3, 23, 0xFF8B8B8B, false);
    }
}
