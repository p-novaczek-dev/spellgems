package net.pnovaczek.spellgems.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.pnovaczek.spellgems.Spellgems;

public class ManaInfuserRecipe implements Recipe<RecipeInput> {

    public static final MapCodec<ManaInfuserRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("infused_item").forGetter(ManaInfuserRecipe::getInfusedItem),
            Ingredient.CODEC.fieldOf("infusing_item").forGetter(ManaInfuserRecipe::getInfusingItem),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(ManaInfuserRecipe::getResult),
            com.mojang.serialization.Codec.INT.fieldOf("mana_cost").forGetter(ManaInfuserRecipe::getManaCost),
            com.mojang.serialization.Codec.INT.fieldOf("processing_time")
                    .forGetter(ManaInfuserRecipe::getProcessingTime)
    ).apply(instance, ManaInfuserRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ManaInfuserRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, ManaInfuserRecipe::getInfusedItem,
            Ingredient.CONTENTS_STREAM_CODEC, ManaInfuserRecipe::getInfusingItem,
            ItemStackTemplate.STREAM_CODEC, ManaInfuserRecipe::getResult,
            ByteBufCodecs.INT, ManaInfuserRecipe::getManaCost,
            ByteBufCodecs.INT, ManaInfuserRecipe::getProcessingTime,
            ManaInfuserRecipe::new);

    public static final RecipeType<ManaInfuserRecipe> TYPE = new RecipeType<>() {
        @Override
        public String toString() {
            return Spellgems.MOD_ID + ":mana_infusing";
        }
    };
    public static final RecipeSerializer<ManaInfuserRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final Ingredient infusedItem;
    private final Ingredient infusingItem;
    private final ItemStackTemplate result;
    private final int manaCost;
    private final int processingTime;

    public ManaInfuserRecipe(Ingredient infusedItem, Ingredient infusingItem, ItemStackTemplate result,
                             int manaCost, int processingTime) {
        this.infusedItem = infusedItem;
        this.infusingItem = infusingItem;
        this.result = result;
        this.manaCost = Math.max(manaCost, 0);
        this.processingTime = Math.max(processingTime, 1);
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        return infusingItem.test(input.getItem(1)) && infusedItem.test(input.getItem(2));
    }

    @Override
    public ItemStack assemble(RecipeInput input) {
        return this.result.create();
    }

    /**
     * Machine recipe — not craftable in the 2x2/3x3 grid. Special recipes skip
     * {@link RecipeManager} placement validation that warns on empty placement info.
     */
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
    public RecipeSerializer<ManaInfuserRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<ManaInfuserRecipe> getType() {
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

    public Ingredient getInfusedItem() { return infusedItem; }
    public Ingredient getInfusingItem() { return infusingItem; }
    public ItemStackTemplate getResult() { return result; }
    public int getManaCost() { return manaCost; }
    public int getProcessingTime() { return processingTime; }
}