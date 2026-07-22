package net.pnovaczek.spellgems.recipe;

import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.spell.SpellIds;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class SpellEnchantingRecipeBuilder implements RecipeBuilder {
    private final String category;
    private final SpellEnchantingRecipe.SpellEnchantInput input;
    private final SpellEnchantingRecipe.CatalystDefinition catalyst;
    private final int levelRequirement;
    private final int xpCost;
    private final String descriptionKey;
    private final SpellEnchantingRecipe.SpellEnchantResult result;

    private SpellEnchantingRecipeBuilder(String category, SpellEnchantingRecipe.SpellEnchantInput input,
                                         SpellEnchantingRecipe.CatalystDefinition catalyst,
                                         int levelRequirement, int xpCost, String descriptionKey,
                                         SpellEnchantingRecipe.SpellEnchantResult result) {
        this.category = category;
        this.input = input;
        this.catalyst = catalyst;
        this.levelRequirement = levelRequirement;
        this.xpCost = xpCost;
        this.descriptionKey = descriptionKey;
        this.result = result;
    }

    public static SpellEnchantingRecipeBuilder combat(SpellEnchantingRecipe.SpellEnchantInput input,
                                                      SpellEnchantingRecipe.CatalystDefinition catalyst,
                                                      int levelRequirement, int xpCost,
                                                      String descriptionKey,
                                                      int modifiers, int strikes) {
        return new SpellEnchantingRecipeBuilder("combat", input, catalyst, levelRequirement, xpCost, descriptionKey,
                new SpellEnchantingRecipe.SpellEnchantResult(
                        modifiers > 0 ? Optional.of(modifiers) : Optional.empty(),
                        strikes > 0 ? Optional.of(strikes) : Optional.empty(),
                        Optional.empty(), false));
    }

    public static SpellEnchantingRecipeBuilder utility(SpellEnchantingRecipe.SpellEnchantInput input,
                                                       SpellEnchantingRecipe.CatalystDefinition catalyst,
                                                       int levelRequirement, int xpCost,
                                                       String descriptionKey,
                                                       String utilityEnchant) {
        return new SpellEnchantingRecipeBuilder("utility", input, catalyst, levelRequirement, xpCost, descriptionKey,
                new SpellEnchantingRecipe.SpellEnchantResult(Optional.empty(), Optional.empty(),
                        Optional.of(Identifier.parse(utilityEnchant)), false));
    }

    public static SpellEnchantingRecipeBuilder potionEnchant(int levelRequirement, int xpCost, String descriptionKey) {
        return new SpellEnchantingRecipeBuilder(
                "utility",
                new SpellEnchantingRecipe.SpellEnchantInput(
                        // Use the specific potion gem item (not the broad utility tag) so that
                        // JEI and getIngredient() correctly show only the valid input.
                        Optional.of(Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "spell_gem_potion")),
                        Optional.empty(),
                        Optional.of(SpellIds.POTION)
                ),
                SpellEnchantingRecipe.CatalystDefinition.anyPotion(1),
                levelRequirement,
                xpCost,
                descriptionKey,
                new SpellEnchantingRecipe.SpellEnchantResult(Optional.empty(), Optional.empty(), Optional.empty(), true)
        );
    }

    @Override
    public SpellEnchantingRecipeBuilder unlockedBy(String name, Criterion<?> criterion) { return this; }
    @Override
    public SpellEnchantingRecipeBuilder group(@Nullable String group) { return this; }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return RecipeBuilder.getDefaultRecipeId(null);
    }

    @Override
    public void save(RecipeOutput output, ResourceKey<Recipe<?>> location) {
        SpellEnchantingRecipe recipe = new SpellEnchantingRecipe(
                category, input, catalyst, levelRequirement, xpCost, descriptionKey, result);
        output.accept(location, recipe, null);
    }

    public void save(RecipeOutput output, String name) {
        ResourceKey<Recipe<?>> location = ResourceKey.create(
                Registries.RECIPE,
                Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, name + "_from_spell_enchanting"));
        save(output, location);
    }
}