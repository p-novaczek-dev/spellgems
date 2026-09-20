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

public class GemForgeRecipeBuilder implements RecipeBuilder {
    private final Ingredient ingredient1;
    private final Ingredient ingredient2;
    private final ItemStackTemplate result;
    private final GemForgeRecipe.Operation operation;
    private final int xpCost;

    private GemForgeRecipeBuilder(
            Ingredient ingredient1,
            Ingredient ingredient2,
            @Nullable ItemStackTemplate result,
            GemForgeRecipe.Operation operation,
            int xpCost
    ) {
        this.ingredient1 = ingredient1;
        this.ingredient2 = ingredient2;
        this.result = result;
        this.operation = operation;
        this.xpCost = xpCost;
    }

    public static GemForgeRecipeBuilder create(Ingredient ingredient1, Ingredient ingredient2, ItemLike result, int xpCost) {
        return new GemForgeRecipeBuilder(
                ingredient1, ingredient2, new ItemStackTemplate(result.asItem()), GemForgeRecipe.Operation.STATIC, xpCost);
    }

    public static GemForgeRecipeBuilder applyTome(Ingredient gems, Ingredient tomes, int xpCost) {
        return new GemForgeRecipeBuilder(gems, tomes, null, GemForgeRecipe.Operation.APPLY_TOME, xpCost);
    }

    public static GemForgeRecipeBuilder socketWeapon(Ingredient weapons, Ingredient gem, int xpCost) {
        return new GemForgeRecipeBuilder(weapons, gem, null, GemForgeRecipe.Operation.SOCKET_WEAPON, xpCost);
    }

    public static GemForgeRecipeBuilder unsocketWeapon(Ingredient weapons, int xpCost) {
        return new GemForgeRecipeBuilder(weapons, null, null, GemForgeRecipe.Operation.UNSOCKET_WEAPON, xpCost);
    }

    public static GemForgeRecipeBuilder combineGreater(Ingredient first, Ingredient second, int xpCost) {
        return new GemForgeRecipeBuilder(first, second, null, GemForgeRecipe.Operation.COMBINE_GREATER, xpCost);
    }

    @Override
    public GemForgeRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        return this;
    }

    @Override
    public GemForgeRecipeBuilder group(@Nullable String group) {
        return this;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return RecipeBuilder.getDefaultRecipeId(this.result);
    }

    @Override
    public void save(RecipeOutput output, ResourceKey<Recipe<?>> location) {
        GemForgeRecipe recipe = switch (operation) {
            case APPLY_TOME -> GemForgeRecipe.applyTome(ingredient1, ingredient2, xpCost);
            case SOCKET_WEAPON -> GemForgeRecipe.socketWeapon(ingredient1, ingredient2, xpCost);
            case UNSOCKET_WEAPON -> GemForgeRecipe.unsocketWeapon(ingredient1, xpCost);
            case COMBINE_GREATER -> GemForgeRecipe.combineGreater(ingredient1, ingredient2, xpCost);
            case STATIC -> GemForgeRecipe.staticResult(ingredient1, ingredient2, result, xpCost);
        };
        output.accept(location, recipe, null);
    }

    public void save(RecipeOutput output, String name) {
        ResourceKey<Recipe<?>> location = ResourceKey.create(
                Registries.RECIPE,
                Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, name + "_from_gem_forging")
        );
        save(output, location);
    }
}
