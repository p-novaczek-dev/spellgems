package net.pnovaczek.spellgems.recipe;

import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.pnovaczek.spellgems.Spellgems;
import org.jetbrains.annotations.Nullable;

public class TransmuteRecipeBuilder implements RecipeBuilder {
    private final Ingredient ingredient;
    private final ItemStackTemplate result;

    private TransmuteRecipeBuilder(Ingredient ingredient, ItemLike result) {
        this.ingredient = ingredient;
        this.result = new ItemStackTemplate(result.asItem());
    }

    public static TransmuteRecipeBuilder create(Ingredient ingredient, ItemLike result) {
        return new TransmuteRecipeBuilder(ingredient, result);
    }

    @Override
    public TransmuteRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        return this;
    }

    @Override
    public TransmuteRecipeBuilder group(@Nullable String group) {
        return this;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return RecipeBuilder.getDefaultRecipeId(this.result);
    }

    @Override
    public void save(RecipeOutput output, ResourceKey<Recipe<?>> location) {
        output.accept(location, new TransmuteRecipe(ingredient, result), null);
    }

    public void save(RecipeOutput output, String name) {
        ResourceKey<Recipe<?>> location = ResourceKey.create(
                Registries.RECIPE,
                Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, name + "_from_transmuting")
        );
        save(output, location);
    }
}
