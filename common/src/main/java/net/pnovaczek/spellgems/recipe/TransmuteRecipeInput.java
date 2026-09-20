package net.pnovaczek.spellgems.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.block.state.BlockState;

public record TransmuteRecipeInput(BlockState blockState) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return new ItemStack(blockState.getBlock());
    }

    @Override
    public int size() {
        return 1;
    }
}
