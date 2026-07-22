package net.pnovaczek.spellgems.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record ManaInfuserRecipeInput(ItemStack infusing, ItemStack toInfuse) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return switch (index) {
            case 1 -> infusing;
            case 2 -> toInfuse;
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public int size() {
        return 2;
    }
}