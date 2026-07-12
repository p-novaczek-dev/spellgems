package net.pnovaczek.spellgems.client.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.pnovaczek.spellgems.ModBlocks;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.recipe.SpellEnchantingRecipe;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * JEI category for Spell Enchanting recipes.
 * These are not standard item->item recipes; they describe effects applied to a target
 * using a catalyst. Inputs are shown on the left; the recipe's description text is
 * displayed as the primary output/result (the input is modified in-place).
 */
public class SpellEnchantingRecipeCategory implements IRecipeCategory<RecipeHolder<SpellEnchantingRecipe>> {

    public static final IRecipeType<RecipeHolder<SpellEnchantingRecipe>> TYPE =
            IRecipeType.create(Spellgems.MOD_ID, "spell_enchanting", (Class<RecipeHolder<SpellEnchantingRecipe>>)(Class<?>) RecipeHolder.class);

    private static final Identifier BACKGROUND_LOCATION =
            Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "textures/gui/jei/spell_enchanting_table.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final List<ItemStack> potionCatalysts;

    public SpellEnchantingRecipeCategory(IGuiHelper guiHelper, List<ItemStack> potionCatalysts) {
        this.background = guiHelper.drawableBuilder(BACKGROUND_LOCATION, 0, 0, 160, 60)
                .setTextureSize(160, 60)
                .build();
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.SPELL_ENCHANTING_TABLE));
        this.potionCatalysts = potionCatalysts != null ? potionCatalysts : List.of();
    }

    @Override
    public IRecipeType<RecipeHolder<SpellEnchantingRecipe>> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.spellgems.spell_enchanting_table");
    }

    @Override
    public int getWidth() {
        return 160;
    }

    @Override
    public int getHeight() {
        return 60;
    }

    @Override
    public boolean needsRecipeBorder() {
        return true;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<SpellEnchantingRecipe> recipeHolder, IFocusGroup focuses) {
        SpellEnchantingRecipe recipe = recipeHolder.value();

        // Target (input gem/book or generic via ingredient) - left side
        var targetIngredient = recipe.getInput().getIngredient();
        if (targetIngredient != null) {
            builder.addSlot(RecipeIngredientRole.INPUT, 5, 20)
                    .add(targetIngredient);
        } else {
            // Fallback representative for "any combat/utility"
            builder.addSlot(RecipeIngredientRole.INPUT, 5, 20)
                    .add(new ItemStack(net.minecraft.world.item.Items.BOOK));
        }

        // Catalyst
        var catalystDef = recipe.getCatalystDef();
        if (catalystDef.anyPotion()) {
            // Cycle through all valid potions (drinkable, splash, lingering with effects)
            var potionSlot = builder.addSlot(RecipeIngredientRole.INPUT, 25, 20);
            if (!potionCatalysts.isEmpty()) {
                for (ItemStack potion : potionCatalysts) {
                    potionSlot.add(potion);
                }
            } else {
                // Fallback: provide a few potions with actual effects instead of the empty "uncraftable" potion
                ItemStack drink = new ItemStack(Items.POTION);
                drink.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.REGENERATION));
                potionSlot.add(drink);

                ItemStack splash = new ItemStack(Items.SPLASH_POTION);
                splash.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.HEALING));
                potionSlot.add(splash);

                ItemStack linger = new ItemStack(Items.LINGERING_POTION);
                linger.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.SLOWNESS));
                potionSlot.add(linger);
            }
        } else {
            builder.addSlot(RecipeIngredientRole.INPUT, 25, 20)
                    .add(catalystDef.asIngredient());
        }
    }

    @Override
    public void draw(RecipeHolder<SpellEnchantingRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView,
                     GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        // Draw custom background first
        this.background.draw(guiGraphics);

        SpellEnchantingRecipe recipe = recipeHolder.value();

        // Description text from the recipe (instead of an item result)
        String key = recipe.getDescriptionKey();
        if (key != null && !key.isEmpty()) {
            Component desc = Component.translatable(key);
            guiGraphics.text(Minecraft.getInstance().font, desc, 5, 5, 0xFFFFFFFF, true);
        }

        Component levelReq = Component.translatable("container.spellgems.spell_enchanting.level_requirement", recipe.getLevelRequirement());
        guiGraphics.text(Minecraft.getInstance().font, levelReq, 5, 40, 0xFFAAAAAA, false);

        Component xpCost = Component.translatable("container.spellgems.spell_enchanting.xp_cost", recipe.getXpCost());
        guiGraphics.text(Minecraft.getInstance().font, xpCost, 5, 50, 0xFFAAAAAA, false);
    }
}
