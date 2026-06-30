package net.pnovaczek.spellgems.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class SpellEnchantingRecipeLookup {

    public static final int MAX_RECIPES = 16;
    public static final int FIELDS_PER_RECIPE = 4;

    private SpellEnchantingRecipeLookup() {
    }

    public static List<RecipeHolder<SpellEnchantingRecipe>> findRecipesForTarget(
            RecipeManager recipeManager,
            ItemStack target
    ) {
        if (target.isEmpty()) {
            return List.of();
        }

        List<RecipeHolder<SpellEnchantingRecipe>> matches = new ArrayList<>();
        for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
            if (holder.value().getType() != SpellEnchantingRecipe.TYPE) {
                continue;
            }
            if (!(holder.value() instanceof SpellEnchantingRecipe recipe)) {
                continue;
            }
            if (recipe.matchesTarget(target)) {
                matches.add(cast(holder));
            }
        }

        matches.sort(Comparator.comparing(holder -> holder.id().identifier().toString()));
        return matches;
    }

    public static RecipeHolder<SpellEnchantingRecipe> getRecipeAt(
            RecipeManager recipeManager,
            ItemStack target,
            int index
    ) {
        List<RecipeHolder<SpellEnchantingRecipe>> recipes = findRecipesForTarget(recipeManager, target);
        if (index < 0 || index >= recipes.size()) {
            return null;
        }
        return recipes.get(index);
    }

    @SuppressWarnings("unchecked")
    private static RecipeHolder<SpellEnchantingRecipe> cast(RecipeHolder<?> holder) {
        return (RecipeHolder<SpellEnchantingRecipe>) holder;
    }
}