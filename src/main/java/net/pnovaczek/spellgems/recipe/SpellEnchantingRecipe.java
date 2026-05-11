package net.pnovaczek.spellgems.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.pnovaczek.spellgems.Spellgems;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class SpellEnchantingRecipe implements Recipe<SpellEnchantingRecipeInput> {

    public static final MapCodec<SpellEnchantingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("category").forGetter(SpellEnchantingRecipe::getCategory),
            SpellEnchantInput.CODEC.fieldOf("input").forGetter(SpellEnchantingRecipe::getInput),
            CatalystDefinition.CODEC.fieldOf("catalyst").forGetter(SpellEnchantingRecipe::getCatalystDef),
            Codec.INT.fieldOf("level_requirement").forGetter(SpellEnchantingRecipe::getLevelRequirement),
            Codec.INT.fieldOf("xp_cost").forGetter(SpellEnchantingRecipe::getXpCost),
            SpellEnchantResult.CODEC.fieldOf("result").forGetter(SpellEnchantingRecipe::getResult)
    ).apply(instance, SpellEnchantingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpellEnchantingRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SpellEnchantingRecipe::getCategory,
            SpellEnchantInput.STREAM_CODEC, SpellEnchantingRecipe::getInput,
            CatalystDefinition.STREAM_CODEC, SpellEnchantingRecipe::getCatalystDef,
            ByteBufCodecs.INT, SpellEnchantingRecipe::getLevelRequirement,
            ByteBufCodecs.INT, SpellEnchantingRecipe::getXpCost,
            SpellEnchantResult.STREAM_CODEC, SpellEnchantingRecipe::getResult,
            SpellEnchantingRecipe::new);

    public static final RecipeType<SpellEnchantingRecipe> TYPE = new RecipeType<>() {
        @Override
        public String toString() {
            return Spellgems.MOD_ID + ":spell_enchanting";
        }
    };
    public static final RecipeSerializer<SpellEnchantingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final String category;
    private final SpellEnchantInput input;
    private final CatalystDefinition catalystDef;
    private final int levelRequirement;
    private final int xpCost;
    private final SpellEnchantResult result;

    public SpellEnchantingRecipe(String category, SpellEnchantInput input, CatalystDefinition catalystDef,
                                 int levelRequirement, int xpCost, SpellEnchantResult result) {
        this.category = category;
        this.input = input;
        this.catalystDef = catalystDef;
        this.levelRequirement = levelRequirement;
        this.xpCost = xpCost;
        this.result = result;
    }

    @Override
    public boolean matches(SpellEnchantingRecipeInput recipeInput, Level level) {
        ItemStack target = recipeInput.target();
        ItemStack catalystStack = recipeInput.catalyst();
        boolean baseMatch = input.getIngredient().test(target);
        // spell match (placeholder – implement via your spell Component when ready)
        boolean spellMatch = input.spell().map(s -> true /* check target component */).orElse(true);
        boolean catalystMatch = catalystDef.asIngredient().test(catalystStack) && catalystStack.getCount() >= catalystDef.count();
        return baseMatch && spellMatch && catalystMatch;
    }

    @Override
    public ItemStack assemble(SpellEnchantingRecipeInput input)  {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean showNotification() { return false; }
    @Override
    public String group() { return ""; }
    @Override
    public RecipeSerializer<SpellEnchantingRecipe> getSerializer() { return SERIALIZER; }
    @Override
    public RecipeType<SpellEnchantingRecipe> getType() { return TYPE; }
    @Override
    public PlacementInfo placementInfo() { return PlacementInfo.NOT_PLACEABLE; }
    @Override
    public RecipeBookCategory recipeBookCategory() { return RecipeBookCategories.CRAFTING_MISC; }

    public String getCategory() { return category; }
    public SpellEnchantInput getInput() { return input; }
    public CatalystDefinition getCatalystDef() { return catalystDef; }
    public int getLevelRequirement() { return levelRequirement; }
    public int getXpCost() { return xpCost; }
    public SpellEnchantResult getResult() { return result; }

    // Sub-records (exact JSON mapping)
    public record SpellEnchantInput(Optional<Identifier> item, Optional<Identifier> tag, Optional<Identifier> spell) {
        public Ingredient getIngredient() {
            if (item.isPresent()) return BuiltInRegistries.ITEM.get(item.get())
                    .map(holder -> Ingredient.of(holder.value()))
                    .orElse(null);
            if (tag.isPresent()) {
                TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tag.get());
                Iterable<Holder<Item>> iterableTags = BuiltInRegistries.ITEM.getTagOrEmpty(tagKey);
                return StreamSupport.stream(iterableTags.spliterator(), false)
                        .map(HolderSet::direct)
                        .map(Ingredient::of)
                        .findFirst()
                        .orElse(null);
            }
            return null;
        }
        public static final Codec<SpellEnchantInput> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.optionalFieldOf("item").forGetter(SpellEnchantInput::item),
                Identifier.CODEC.optionalFieldOf("tag").forGetter(SpellEnchantInput::tag),
                Identifier.CODEC.optionalFieldOf("spell").forGetter(SpellEnchantInput::spell)
        ).apply(instance, SpellEnchantInput::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, SpellEnchantInput> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.optional(Identifier.STREAM_CODEC), SpellEnchantInput::item,
                ByteBufCodecs.optional(Identifier.STREAM_CODEC), SpellEnchantInput::tag,
                ByteBufCodecs.optional(Identifier.STREAM_CODEC), SpellEnchantInput::spell,
                SpellEnchantInput::new);
    }

    public record CatalystDefinition(Identifier item, int count) {
        public Ingredient asIngredient() {
            return Ingredient.of(BuiltInRegistries.ITEM.get(item).get().value());
        }
        public static final Codec<CatalystDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("item").forGetter(CatalystDefinition::item),
                Codec.INT.optionalFieldOf("count", 1).forGetter(CatalystDefinition::count)
        ).apply(instance, CatalystDefinition::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, CatalystDefinition> STREAM_CODEC = StreamCodec.composite(
                Identifier.STREAM_CODEC, CatalystDefinition::item,
                ByteBufCodecs.INT, CatalystDefinition::count,
                CatalystDefinition::new);
    }

    public record SpellEnchantResult(Optional<Integer> modifiers, Optional<Integer> strikes,
                                     Optional<Identifier> utility, boolean potion) {
        public static final Codec<SpellEnchantResult> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.optionalFieldOf("modifiers").forGetter(SpellEnchantResult::modifiers),
                Codec.INT.optionalFieldOf("strikes").forGetter(SpellEnchantResult::strikes),
                Identifier.CODEC.optionalFieldOf("utility").forGetter(SpellEnchantResult::utility),
                Codec.BOOL.optionalFieldOf("potion", false).forGetter(SpellEnchantResult::potion)
        ).apply(instance, SpellEnchantResult::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, SpellEnchantResult> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.optional(ByteBufCodecs.INT), SpellEnchantResult::modifiers,
                ByteBufCodecs.optional(ByteBufCodecs.INT), SpellEnchantResult::strikes,
                ByteBufCodecs.optional(Identifier.STREAM_CODEC), SpellEnchantResult::utility,
                ByteBufCodecs.BOOL, SpellEnchantResult::potion,
                SpellEnchantResult::new);
    }
}