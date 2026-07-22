package net.pnovaczek.spellgems.recipe;

import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.pnovaczek.spellgems.Spellgems;
import org.jetbrains.annotations.Nullable;

public class ManaInfuserRecipeBuilder implements RecipeBuilder {
    private final Ingredient infusedItem;
    private final Ingredient infusingItem;
    private final ItemStackTemplate resultItem;
    private final int resultCount;
    private final int manaCost;
    private final int processingTime;

    private ManaInfuserRecipeBuilder(Ingredient infusedItem, Ingredient infusingItem,
                                     ItemLike resultItem, int resultCount,
                                     int manaCost, int processingTime) {
        this.infusedItem = infusedItem;
        this.infusingItem = infusingItem;
        this.resultItem = new ItemStackTemplate(resultItem.asItem());
        this.resultCount = resultCount;
        this.manaCost = manaCost;
        this.processingTime = processingTime;
    }

    public static ManaInfuserRecipeBuilder create(Ingredient infusedItem, Ingredient infusingItem,
                                                  ItemLike result, int manaCost) {
        return create(infusedItem, infusingItem, result, 1, manaCost, 200);
    }

    public static ManaInfuserRecipeBuilder create(Ingredient infusedItem, Ingredient infusingItem,
                                                  ItemLike result, int count, int manaCost, int processingTime) {
        return new ManaInfuserRecipeBuilder(infusedItem, infusingItem, result, count, manaCost, processingTime);
    }

    @Override
    public ManaInfuserRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        return this;
    }

    @Override
    public ManaInfuserRecipeBuilder group(@Nullable String group) {
        return this;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return RecipeBuilder.getDefaultRecipeId(this.resultItem);
    }

    @Override
    public void save(RecipeOutput output, ResourceKey<Recipe<?>> location) {
        ManaInfuserRecipe recipe = new ManaInfuserRecipe(
                infusedItem, infusingItem, resultItem, manaCost, processingTime);

        output.accept(location, recipe, null);
    }

    public void save(RecipeOutput output, String name) {
        String craftingPath = name + "_from_mana_infusing";
        ResourceKey<Recipe<?>> location = ResourceKey.create(
                Registries.RECIPE,
                Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, craftingPath)
        );
        save(output, location);
    }
}