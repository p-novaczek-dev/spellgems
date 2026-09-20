package net.pnovaczek.spellgems.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
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
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.item.GreaterSpellGems;
import net.pnovaczek.spellgems.item.SpellTomeApplier;
import net.pnovaczek.spellgems.item.WeaponSocketing;

import java.util.Optional;

public class GemForgeRecipe implements Recipe<GemForgeRecipeInput> {

    public enum Operation {
        STATIC,
        APPLY_TOME,
        SOCKET_WEAPON,
        UNSOCKET_WEAPON,
        COMBINE_GREATER;

        public static final Codec<Operation> CODEC = Codec.STRING.xmap(Operation::fromId, Operation::id);

        public static Operation fromId(String id) {
            return switch (id) {
                case "apply_tome" -> APPLY_TOME;
                case "socket_weapon" -> SOCKET_WEAPON;
                case "unsocket_weapon" -> UNSOCKET_WEAPON;
                case "combine_greater" -> COMBINE_GREATER;
                default -> STATIC;
            };
        }

        public String id() {
            return switch (this) {
                case APPLY_TOME -> "apply_tome";
                case SOCKET_WEAPON -> "socket_weapon";
                case UNSOCKET_WEAPON -> "unsocket_weapon";
                case COMBINE_GREATER -> "combine_greater";
                case STATIC -> "static";
            };
        }
    }

    public static final MapCodec<GemForgeRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient_1").forGetter(GemForgeRecipe::getIngredient1),
            Ingredient.CODEC.optionalFieldOf("ingredient_2").forGetter(GemForgeRecipe::getIngredient2),
            ItemStackTemplate.CODEC.optionalFieldOf("result").forGetter(GemForgeRecipe::getStaticResult),
            Operation.CODEC.optionalFieldOf("operation", Operation.STATIC).forGetter(GemForgeRecipe::getOperation),
            Codec.INT.fieldOf("xp_cost").forGetter(GemForgeRecipe::getXpCost)
    ).apply(instance, GemForgeRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, GemForgeRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, GemForgeRecipe::getIngredient1,
            ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC), GemForgeRecipe::getIngredient2,
            ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC), GemForgeRecipe::getStaticResult,
            ByteBufCodecs.STRING_UTF8, recipe -> recipe.getOperation().id(),
            ByteBufCodecs.INT, GemForgeRecipe::getXpCost,
            (ingredient1, ingredient2, staticResult, operationId, xpCost) ->
                    new GemForgeRecipe(ingredient1, ingredient2, staticResult, Operation.fromId(operationId), xpCost));

    public static final RecipeType<GemForgeRecipe> TYPE = new RecipeType<>() {
        @Override
        public String toString() {
            return Spellgems.MOD_ID + ":gem_forging";
        }
    };
    public static final RecipeSerializer<GemForgeRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final Ingredient ingredient1;
    private final Optional<Ingredient> ingredient2;
    private final Optional<ItemStackTemplate> staticResult;
    private final Operation operation;
    private final int xpCost;

    public GemForgeRecipe(
            Ingredient ingredient1,
            Optional<Ingredient> ingredient2,
            Optional<ItemStackTemplate> staticResult,
            Operation operation,
            int xpCost
    ) {
        this.ingredient1 = ingredient1;
        this.ingredient2 = ingredient2;
        this.staticResult = staticResult;
        this.operation = operation == null ? Operation.STATIC : operation;
        this.xpCost = Math.max(xpCost, 0);
    }

    public static GemForgeRecipe staticResult(
            Ingredient ingredient1, Ingredient ingredient2, ItemStackTemplate result, int xpCost
    ) {
        return new GemForgeRecipe(ingredient1, Optional.of(ingredient2), Optional.of(result), Operation.STATIC, xpCost);
    }

    public static GemForgeRecipe applyTome(Ingredient ingredient1, Ingredient ingredient2, int xpCost) {
        return new GemForgeRecipe(ingredient1, Optional.of(ingredient2), Optional.empty(), Operation.APPLY_TOME, xpCost);
    }

    public static GemForgeRecipe socketWeapon(Ingredient ingredient1, Ingredient ingredient2, int xpCost) {
        return new GemForgeRecipe(ingredient1, Optional.of(ingredient2), Optional.empty(), Operation.SOCKET_WEAPON, xpCost);
    }

    public static GemForgeRecipe unsocketWeapon(Ingredient weapons, int xpCost) {
        return new GemForgeRecipe(weapons, Optional.empty(), Optional.empty(), Operation.UNSOCKET_WEAPON, xpCost);
    }

    public static GemForgeRecipe combineGreater(Ingredient ingredient1, Ingredient ingredient2, int xpCost) {
        return new GemForgeRecipe(ingredient1, Optional.of(ingredient2), Optional.empty(), Operation.COMBINE_GREATER, xpCost);
    }

    @Override
    public boolean matches(GemForgeRecipeInput input, Level level) {
        ItemStack first = input.getItem(0);
        ItemStack second = input.getItem(1);
        return switch (operation) {
            case UNSOCKET_WEAPON -> matchesUnsocket(first, second);
            case APPLY_TOME -> twoFilled(first, second) && ingredientsMatch(first, second)
                    && SpellTomeApplier.tryApply(first, second).isPresent();
            case SOCKET_WEAPON -> twoFilled(first, second) && ingredientsMatch(first, second)
                    && WeaponSocketing.trySocket(first, second).isPresent();
            case COMBINE_GREATER -> twoFilled(first, second) && ingredientsMatch(first, second)
                    && GreaterSpellGems.tryCombine(first, second).isPresent();
            case STATIC -> twoFilled(first, second) && ingredientsMatch(first, second);
        };
    }

    private boolean matchesUnsocket(ItemStack first, ItemStack second) {
        ItemStack weapon;
        if (first.isEmpty() == second.isEmpty()) {
            return false;
        }
        weapon = first.isEmpty() ? second : first;
        return ingredient1.test(weapon) && WeaponSocketing.tryUnsocket(weapon).isPresent();
    }

    private static boolean twoFilled(ItemStack first, ItemStack second) {
        return !first.isEmpty() && !second.isEmpty();
    }

    private boolean ingredientsMatch(ItemStack first, ItemStack second) {
        Ingredient secondIngredient = ingredient2.orElse(null);
        if (secondIngredient == null) {
            return false;
        }
        return (ingredient1.test(first) && secondIngredient.test(second))
                || (ingredient1.test(second) && secondIngredient.test(first));
    }

    @Override
    public ItemStack assemble(GemForgeRecipeInput input) {
        ItemStack first = input.getItem(0);
        ItemStack second = input.getItem(1);
        return switch (operation) {
            case APPLY_TOME -> SpellTomeApplier.tryApply(first, second).orElse(ItemStack.EMPTY);
            case SOCKET_WEAPON -> WeaponSocketing.trySocket(first, second).orElse(ItemStack.EMPTY);
            case UNSOCKET_WEAPON -> WeaponSocketing.tryUnsocket(first.isEmpty() ? second : first).orElse(ItemStack.EMPTY);
            case COMBINE_GREATER -> GreaterSpellGems.tryCombine(first, second).orElse(ItemStack.EMPTY);
            case STATIC -> staticResult.map(ItemStackTemplate::create).orElse(ItemStack.EMPTY);
        };
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
    public RecipeSerializer<GemForgeRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<GemForgeRecipe> getType() {
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

    public Ingredient getIngredient1() {
        return ingredient1;
    }

    public Optional<Ingredient> getIngredient2() {
        return ingredient2;
    }

    public Optional<ItemStackTemplate> getStaticResult() {
        return staticResult;
    }

    public Operation getOperation() {
        return operation;
    }

    public int getXpCost() {
        return xpCost;
    }
}
