package net.pnovaczek.spellgems.client.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.pnovaczek.spellgems.ModBlocks;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.recipe.ManaInfuserRecipe;

import java.util.List;

@JeiPlugin
public class SpellgemsJeiPlugin implements IModPlugin {

    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "jei");
    }
//
//    @Override
//    public void registerCategories(IRecipeCategoryRegistration registration) {
//        registration.addRecipeCategories(
//                new ManaInfuserRecipeCategory(registration.getJeiHelpers().getGuiHelper())
//        );
//    }
//
//    @Override
//    public void registerRecipes(IRecipeRegistration registration) {
//        RecipeManager recipeManager = Minecraft.getInstance().getSingleplayerServer().getRecipeManager();
//        List<RecipeHolder<ManaInfuserRecipe>> recipes =
//                recipeManager.getAllOfType(ManaInfuserRecipe.TYPE).stream().toList();
//
//        registration.addRecipes(ManaInfuserRecipeCategory.TYPE, recipes);
//    }
//
//    @Override
//    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
//        registration.addCraftingStation(
//                ManaInfuserRecipeCategory.TYPE,
//                new ItemStack(ModBlocks.MANA_INFUSER)
//        );
//    }
}