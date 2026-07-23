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

                // 7 Cobblestone + 1 Redstone + 1 Shimmersteel → Spell Dispenser
                shaped(RecipeCategory.REDSTONE, ModBlocks.SPELL_DISPENSER)
                        .pattern("###")
                        .pattern("#R#")
                        .pattern("#S#")
                        .define('#', Blocks.COBBLESTONE)
                        .define('R', Items.REDSTONE)
                        .define('S', ModItems.SHIMMERSTEEL_INGOT)
                        .unlockedBy("has_shimmersteel_ingot", has(ModItems.SHIMMERSTEEL_INGOT))
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
                        Ingredient.of(Items.DIAMOND),
                        ModItems.SHIMMERSTEEL_INGOT,
                        1,
                        2,
                        100)
                    .unlockedBy("has_iron_ingot", has(net.minecraft.world.item.Items.IRON_INGOT))
                    .save(exporter, "shimmersteel_ingot");

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

                shapeless(RecipeCategory.MISC, ModItems.SPELL_GEM_BLINK)
                        .requires(ModItems.RAW_SPELL_GEM)
                        .requires(Items.ENDER_PEARL)
                        .unlockedBy("has_raw_spell_gem", has(ModItems.RAW_SPELL_GEM))
                        .save(exporter);

                shapeless(RecipeCategory.MISC, ModItems.SPELL_GEM_MAGNET)
                        .requires(ModItems.RAW_SPELL_GEM)
                        .requires(Items.REDSTONE)
                        .unlockedBy("has_raw_spell_gem", has(ModItems.RAW_SPELL_GEM))
                        .save(exporter);

                shapeless(RecipeCategory.MISC, ModItems.SPELL_GEM_PLACE_BLOCK)
                        .requires(ModItems.RAW_SPELL_GEM)
                        .requires(Items.COBBLESTONE)
                        .unlockedBy("has_raw_spell_gem", has(ModItems.RAW_SPELL_GEM))
                        .save(exporter);

                shapeless(RecipeCategory.MISC, ModItems.SPELL_GEM_BREAK_BLOCK)
                        .requires(ModItems.RAW_SPELL_GEM)
                        .requires(Items.OBSIDIAN)
                        .unlockedBy("has_raw_spell_gem", has(ModItems.RAW_SPELL_GEM))
                        .save(exporter);

                shapeless(RecipeCategory.MISC, ModItems.SPELL_GEM_PLANT)
                        .requires(ModItems.RAW_SPELL_GEM)
                        .requires(Items.WHEAT_SEEDS)
                        .unlockedBy("has_raw_spell_gem", has(ModItems.RAW_SPELL_GEM))
                        .save(exporter);

                shapeless(RecipeCategory.MISC, ModItems.SPELL_GEM_HARVEST)
                        .requires(ModItems.RAW_SPELL_GEM)
                        .requires(Items.WHEAT)
                        .unlockedBy("has_raw_spell_gem", has(ModItems.RAW_SPELL_GEM))
                        .save(exporter);

                shapeless(RecipeCategory.MISC, ModItems.SPELL_GEM_FEED)
                        .requires(ModItems.RAW_SPELL_GEM)
                        .requires(Items.HAY_BLOCK)
                        .unlockedBy("has_raw_spell_gem", has(ModItems.RAW_SPELL_GEM))
                        .save(exporter);

                shapeless(RecipeCategory.MISC, ModItems.SPELL_GEM_GROW)
                        .requires(ModItems.RAW_SPELL_GEM)
                        .requires(Items.BONE_MEAL)
                        .unlockedBy("has_raw_spell_gem", has(ModItems.RAW_SPELL_GEM))
                        .save(exporter);

                shapeless(RecipeCategory.MISC, ModItems.SPELL_GEM_POTION)
                        .requires(ModItems.RAW_SPELL_GEM)
                        .requires(Items.GLASS_BOTTLE)
                        .unlockedBy("has_raw_spell_gem", has(ModItems.RAW_SPELL_GEM))
                        .save(exporter, "spell_gem_potion");

                // Wand: 1 stick + 1 shimmersteel (shimmersteel above stick)
                shaped(RecipeCategory.TOOLS, ModItems.WAND)
                        .pattern("S")
                        .pattern("|")
                        .define('S', ModItems.SHIMMERSTEEL_INGOT)
                        .define('|', Items.STICK)
                        .unlockedBy("has_shimmersteel_ingot", has(ModItems.SHIMMERSTEEL_INGOT))
                        .save(exporter);

                // Astral Bow: like vanilla bow but with shimmersteel instead of the middle stick
                // Pattern: 2 sticks + 1 shimmersteel + 3 string
                shaped(RecipeCategory.COMBAT, ModItems.ASTRAL_BOW)
                        .pattern(" #X")
                        .pattern("S X")
                        .pattern(" #X")
                        .define('#', Items.STICK)
                        .define('S', ModItems.SHIMMERSTEEL_INGOT)
                        .define('X', Items.STRING)
                        .unlockedBy("has_shimmersteel_ingot", has(ModItems.SHIMMERSTEEL_INGOT))
                        .save(exporter);

                // Spell Enchanting Table: 1 book + 2 shimmersteel + 4 obsidian
                shaped(RecipeCategory.DECORATIONS, ModBlocks.SPELL_ENCHANTING_TABLE)
                        .pattern(" B ")
                        .pattern("SSS")
                        .pattern("OOO")
                        .define('B', Items.BOOK)
                        .define('S', ModItems.SHIMMERSTEEL_INGOT)
                        .define('O', Blocks.OBSIDIAN)
                        .unlockedBy("has_shimmersteel_ingot", has(ModItems.SHIMMERSTEEL_INGOT))
                        .save(exporter);

                var lapisCatalyst = new SpellEnchantingRecipe.CatalystDefinition(
                        BuiltInRegistries.ITEM.getKey(Items.LAPIS_LAZULI),
                        1);

                // Combat spell gem: 1 random modifier + 1 random strike
                SpellEnchantingRecipeBuilder.combat(
                        new SpellEnchantingRecipe.SpellEnchantInput(
                                Optional.empty(),
                                Optional.of(Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "combat_spell_gems")),
                                Optional.empty()),
                        lapisCatalyst,
                        20,
                        62,
                        "recipe.spellgems.combat_spell_random.description",
                        1,
                        1)
                        .save(exporter, "combat_spell_random");

                // Spell tome: 1 random modifier (player chooses this option)
                SpellEnchantingRecipeBuilder.combat(
                        new SpellEnchantingRecipe.SpellEnchantInput(
                                Optional.of(BuiltInRegistries.ITEM.getKey(Items.BOOK)),
                                Optional.empty(),
                                Optional.empty()),
                        lapisCatalyst,
                        20,
                        62,
                        "recipe.spellgems.combat_tome_modifier.description",
                        1,
                        0)
                        .save(exporter, "combat_tome_modifier");

                // Spell tome: 1 random strike (player chooses this option)
                SpellEnchantingRecipeBuilder.combat(
                        new SpellEnchantingRecipe.SpellEnchantInput(
                                Optional.of(BuiltInRegistries.ITEM.getKey(Items.BOOK)),
                                Optional.empty(),
                                Optional.empty()),
                        lapisCatalyst,
                        20,
                        62,
                        "recipe.spellgems.combat_tome_strike.description",
                        0,
                        1)
                        .save(exporter, "combat_tome_strike");

                SpellEnchantingRecipeBuilder.potionEnchant(
                        10,
                        27,
                        "recipe.spellgems.potion_spell_enchant.description"
                ).save(exporter, "potion_spell");

                // Utility enchantments via spell enchanting table (lapis catalyst)
                // Break block + lapis -> silk touch
                SpellEnchantingRecipeBuilder.utility(
                        new SpellEnchantingRecipe.SpellEnchantInput(
                                Optional.of(BuiltInRegistries.ITEM.getKey(ModItems.SPELL_GEM_BREAK_BLOCK)),
                                Optional.empty(),
                                Optional.empty()),
                        lapisCatalyst,
                        10,
                        27,
                        "recipe.spellgems.break_block_silk_touch.description",
                        "spellgems:silk_touch")
                        .save(exporter, "break_block_silk_touch");

                // Smelt (for break block and harvest gems, via tag)
                SpellEnchantingRecipeBuilder.utility(
                        new SpellEnchantingRecipe.SpellEnchantInput(
                                Optional.empty(),
                                Optional.of(Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "smelt_spell_gems")),
                                Optional.empty()),
                        lapisCatalyst,
                        10,
                        27,
                        "recipe.spellgems.smelt.description",
                        "spellgems:smelt")
                        .save(exporter, "smelt");

                // Extend (for blink and magnet gems, via tag)
                SpellEnchantingRecipeBuilder.utility(
                        new SpellEnchantingRecipe.SpellEnchantInput(
                                Optional.empty(),
                                Optional.of(Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "extend_spell_gems")),
                                Optional.empty()),
                        lapisCatalyst,
                        10,
                        27,
                        "recipe.spellgems.extend.description",
                        "spellgems:extend")
                        .save(exporter, "extend");
            }
        };
    }

    @Override
    public String getName() {
        return "ModRecipeProvider";
    }
}