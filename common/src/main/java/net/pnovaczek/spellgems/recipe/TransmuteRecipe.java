package net.pnovaczek.spellgems.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.pnovaczek.spellgems.Spellgems;

public class TransmuteRecipe implements Recipe<TransmuteRecipeInput> {

    public static final MapCodec<TransmuteRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(TransmuteRecipe::getIngredient),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(TransmuteRecipe::getResult)
    ).apply(instance, TransmuteRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TransmuteRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, TransmuteRecipe::getIngredient,
            ItemStackTemplate.STREAM_CODEC, TransmuteRecipe::getResult,
            TransmuteRecipe::new);

    public static final RecipeType<TransmuteRecipe> TYPE = new RecipeType<>() {
        @Override
        public String toString() {
            return Spellgems.MOD_ID + ":transmuting";
        }
    };
    public static final RecipeSerializer<TransmuteRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final Ingredient ingredient;
    private final ItemStackTemplate result;

    public TransmuteRecipe(Ingredient ingredient, ItemStackTemplate result) {
        this.ingredient = ingredient;
        this.result = result;
    }

    @Override
    public boolean matches(TransmuteRecipeInput input, Level level) {
        ItemStack asItem = input.getItem(0);
        return !asItem.isEmpty() && ingredient.test(asItem);
    }

    @Override
    public ItemStack assemble(TransmuteRecipeInput input) {
        return this.result.create();
    }

    public BlockState resultState(BlockState input) {
        Block block = Block.byItem(this.result.create().getItem());
        if (block == Blocks.AIR) {
            return Blocks.AIR.defaultBlockState();
        }
        return block.withPropertiesOf(input);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeSerializer<TransmuteRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<TransmuteRecipe> getType() {
        return TYPE;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public ItemStackTemplate getResult() {
        return result;
    }
}
