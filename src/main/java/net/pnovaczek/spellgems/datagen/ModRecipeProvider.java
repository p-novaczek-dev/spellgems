package net.pnovaczek.spellgems.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.pnovaczek.spellgems.ModBlocks;
import net.pnovaczek.spellgems.ModItems;
import net.pnovaczek.spellgems.ModTags;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.recipe.ManaInfuserRecipeBuilder;
import net.pnovaczek.spellgems.recipe.SpellEnchantingRecipe;
import net.pnovaczek.spellgems.recipe.SpellEnchantingRecipeBuilder;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected @NonNull RecipeProvider createRecipeProvider(HolderLookup.@NonNull Provider registryLookup, @NonNull RecipeOutput exporter) {
        return new RecipeProvider(registryLookup, exporter) {
            @Override
            public void buildRecipes() {
                // 8 Cobblestone + 1 Mana Essence → 1 Mana Infuser
                shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.MANA_INFUSER)
                        .pattern("###")
                        .pattern("#M#")
                        .pattern("###")
                        .define('#', Blocks.COBBLESTONE)
                        .define('M', ModItems.MANA_ESSENCE)
                        .unlockedBy("has_mana_essence", has(ModItems.MANA_ESSENCE))
                        .save(exporter);

                // Mana Root → Mana Essence (furnace smelting)
                oreSmelting(
                        List.of(ModItems.MANA_ROOT),
                        RecipeCategory.MISC,
                        CookingBookCategory.MISC,
                        ModItems.MANA_ESSENCE,
                        0.7f,
                        200,
                        "mana_essence"
                );

                ManaInfuserRecipeBuilder.create(
                        Ingredient.of(Items.IRON_INGOT),
                        Ingredient.of(Items.COAL),
                        ModItems.SHIMMERSTEEL_INGOT,
                        1,
                        2,
                        100)
                    .unlockedBy("has_iron_ingot", has(net.minecraft.world.item.Items.IRON_INGOT))
                    .save(exporter, "schimmersteel_ingot");

                // Raw Spellgem: amethyst_shard (infused) + lapis (infusing) + 8 mana essence
                ManaInfuserRecipeBuilder.create(
                        Ingredient.of(Items.AMETHYST_SHARD),
                        Ingredient.of(Items.LAPIS_LAZULI),
                        ModItems.RAW_SPELL_GEM,
                        8)
                    .unlockedBy("has_amethyst_shard", has(Items.AMETHYST_SHARD))
                    .save(exporter, "raw_spell_gem");

                // Spell gems from raw + specific item (shapeless, per blueprint)
                shapeless(RecipeCategory.MISC, ModItems.SPELL_GEM_PROJECTILE)
                        .requires(ModItems.RAW_SPELL_GEM)
                        .requires(Items.ARROW)
                        .unlockedBy("has_raw_spell_gem", has(ModItems.RAW_SPELL_GEM))
                        .save(exporter);

                shapeless(RecipeCategory.MISC, ModItems.SPELL_GEM_NOVA)
                        .requires(ModItems.RAW_SPELL_GEM)
                        .requires(Items.GUNPOWDER)
                        .unlockedBy("has_raw_spell_gem", has(ModItems.RAW_SPELL_GEM))
                        .save(exporter);

                shapeless(RecipeCategory.MISC, ModItems.SPELL_GEM_VORTEX)
                        .requires(ModItems.RAW_SPELL_GEM)
                        .requires(Items.SLIME_BALL)
                        .unlockedBy("has_raw_spell_gem", has(ModItems.RAW_SPELL_GEM))
                        .save(exporter);

                SpellEnchantingRecipeBuilder.combat(
                         new SpellEnchantingRecipe.SpellEnchantInput(
                                 Optional.ofNullable(null),
                                 Optional.of(Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "combat_spell_gems")),
                                 Optional.ofNullable(null)),
                        new SpellEnchantingRecipe.CatalystDefinition(
                                BuiltInRegistries.ITEM.getKey(Items.LAPIS_LAZULI),
                                1),
                        20,
                        100,
                        "recipe.spellgems.combat_spell_random.description",
                        1,
                        1)
                        .save(exporter, "combat_spell_random");;
            }
        };
    }

    @Override
    public String getName() {
        return "ModRecipeProvider";
    }
}