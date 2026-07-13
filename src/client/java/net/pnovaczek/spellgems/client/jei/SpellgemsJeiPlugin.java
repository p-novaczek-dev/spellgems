package net.pnovaczek.spellgems.client.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.pnovaczek.spellgems.ModBlocks;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.recipe.ManaInfuserRecipe;
import net.pnovaczek.spellgems.recipe.SpellEnchantingRecipe;
import net.pnovaczek.spellgems.spell.enchantment.PotionEnchantments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@JeiPlugin
public class SpellgemsJeiPlugin implements IModPlugin {

    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var jeiHelpers = registration.getJeiHelpers();
        var guiHelper = jeiHelpers.getGuiHelper();

        // Collect all valid potion catalysts (any potion/splash/lingering that has effects)
        // so the JEI slot for "any potion" recipes can cycle through real potions instead of
        // showing the empty "uncraftable potion".
        List<ItemStack> potionCatalysts = new ArrayList<>();
        try {
            Collection<ItemStack> allItems = jeiHelpers.getIngredientManager()
                    .getAllIngredients(VanillaTypes.ITEM_STACK);
            for (ItemStack stack : allItems) {
                if (PotionEnchantments.isValidCatalyst(stack)) {
                    potionCatalysts.add(stack.copy());
                }
            }
        } catch (Exception ignored) {
            // If ingredient manager not ready or no potions, fallbacks in category will be used.
        }

        registration.addRecipeCategories(
                new ManaInfuserRecipeCategory(guiHelper),
                new SpellEnchantingRecipeCategory(guiHelper, potionCatalysts)
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public void registerRecipes(IRecipeRegistration registration) {
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null) {
            return;  // Only register in singleplayer for now; full recipes not available on dedicated client
        }
        RecipeManager recipeManager = server.getRecipeManager();

        // Mana infuser recipes (use same iteration as SpellEnchantingRecipeLookup since no getAllRecipesFor in this MC version)
        @SuppressWarnings("unchecked")
        List<RecipeHolder<ManaInfuserRecipe>> manaRecipes = new ArrayList<>();
        for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
            if (holder.value().getType() == ManaInfuserRecipe.TYPE) {
                manaRecipes.add((RecipeHolder<ManaInfuserRecipe>) holder);
            }
        }
        registration.addRecipes(ManaInfuserRecipeCategory.TYPE, manaRecipes);

        // Spell enchanting recipes (combat, utility, potion variants)
        @SuppressWarnings("unchecked")
        List<RecipeHolder<SpellEnchantingRecipe>> enchantingRecipes = new ArrayList<>();
        for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
            if (holder.value().getType() == SpellEnchantingRecipe.TYPE) {
                enchantingRecipes.add((RecipeHolder<SpellEnchantingRecipe>) holder);
            }
        }
        registration.addRecipes(SpellEnchantingRecipeCategory.TYPE, enchantingRecipes);
    }

    @Override
    @SuppressWarnings("removal")
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // Mana infuser workstation
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.MANA_INFUSER),
                ManaInfuserRecipeCategory.TYPE
        );

        // Spell enchanting table workstation
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.SPELL_ENCHANTING_TABLE),
                SpellEnchantingRecipeCategory.TYPE
        );
    }
}