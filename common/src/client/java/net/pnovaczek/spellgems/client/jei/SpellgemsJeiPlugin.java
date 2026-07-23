package net.pnovaczek.spellgems.client.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.pnovaczek.spellgems.ModBlocks;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.platform.client.ClientPlatform;
import net.pnovaczek.spellgems.recipe.ManaInfuserRecipe;
import net.pnovaczek.spellgems.recipe.SpellEnchantingRecipe;
import net.pnovaczek.spellgems.spell.enchantment.PotionEnchantments;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Shared JEI plugin (Fabric + NeoForge). Discovered via {@link JeiPlugin}; Fabric also lists it in
 * {@code fabric.mod.json} under {@code jei_mod_plugin}.
 * <p>
 * Recipes are resolved from:
 * <ol>
 *   <li>Integrated server {@link RecipeManager} (singleplayer / LAN host)</li>
 *   <li>Loader multiplayer recipe sync ({@link ClientPlatform} → Fabric SynchronizedRecipes / Neo RecipesReceivedEvent)</li>
 * </ol>
 */
@JeiPlugin
public class SpellgemsJeiPlugin implements IModPlugin {

    private static @Nullable IJeiRuntime jeiRuntime;
    private static boolean manaRecipesInJei;
    private static boolean enchantingRecipesInJei;
    private static boolean syncListenerRegistered;

    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        ensureSyncListener();

        var jeiHelpers = registration.getJeiHelpers();
        var guiHelper = jeiHelpers.getGuiHelper();

        List<ItemStack> potionCatalysts = new ArrayList<>();
        try {
            Collection<ItemStack> allItems = jeiHelpers.getIngredientManager()
                    .getAllIngredients(VanillaTypes.ITEM_STACK);
            for (ItemStack stack : allItems) {
                if (PotionEnchantments.isValidCatalyst(stack)) {
                    potionCatalysts.add(stack.copy());
                }
            }
        } catch (Exception ignored) {
            // Ingredient manager not ready; category falls back to placeholder potions.
        }

        registration.addRecipeCategories(
                new ManaInfuserRecipeCategory(guiHelper),
                new SpellEnchantingRecipeCategory(guiHelper, potionCatalysts)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ensureSyncListener();

        List<RecipeHolder<ManaInfuserRecipe>> manaRecipes = collectRecipes(ManaInfuserRecipe.TYPE);
        if (!manaRecipes.isEmpty()) {
            registration.addRecipes(ManaInfuserRecipeCategory.TYPE, manaRecipes);
            manaRecipesInJei = true;
        }

        List<RecipeHolder<SpellEnchantingRecipe>> enchantingRecipes = collectRecipes(SpellEnchantingRecipe.TYPE);
        if (!enchantingRecipes.isEmpty()) {
            registration.addRecipes(SpellEnchantingRecipeCategory.TYPE, enchantingRecipes);
            enchantingRecipesInJei = true;
        }
    }

    @Override
    @SuppressWarnings("removal")
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.MANA_INFUSER),
                ManaInfuserRecipeCategory.TYPE
        );
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.SPELL_ENCHANTING_TABLE),
                SpellEnchantingRecipeCategory.TYPE
        );
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        jeiRuntime = runtime;
        ensureSyncListener();
        pushMissingRecipesToRuntime();
    }

    @Override
    public void onRuntimeUnavailable() {
        jeiRuntime = null;
        manaRecipesInJei = false;
        enchantingRecipesInJei = false;
    }

    private static void ensureSyncListener() {
        if (syncListenerRegistered) {
            return;
        }
        if (!ClientPlatform.isInitialized()) {
            // Client entrypoint has not bootstrapped yet; retry from onRuntimeAvailable.
            return;
        }
        syncListenerRegistered = true;
        ClientPlatform.client().onClientRecipesSynchronized(SpellgemsJeiPlugin::pushMissingRecipesToRuntime);
    }

    private static void pushMissingRecipesToRuntime() {
        IJeiRuntime runtime = jeiRuntime;
        if (runtime == null) {
            return;
        }
        IRecipeManager recipeManager = runtime.getRecipeManager();

        if (!manaRecipesInJei) {
            List<RecipeHolder<ManaInfuserRecipe>> manaRecipes = collectRecipes(ManaInfuserRecipe.TYPE);
            if (!manaRecipes.isEmpty()) {
                recipeManager.addRecipes(ManaInfuserRecipeCategory.TYPE, manaRecipes);
                manaRecipesInJei = true;
            }
        }

        if (!enchantingRecipesInJei) {
            List<RecipeHolder<SpellEnchantingRecipe>> enchantingRecipes = collectRecipes(SpellEnchantingRecipe.TYPE);
            if (!enchantingRecipes.isEmpty()) {
                recipeManager.addRecipes(SpellEnchantingRecipeCategory.TYPE, enchantingRecipes);
                enchantingRecipesInJei = true;
            }
        }
    }

    private static <I extends net.minecraft.world.item.crafting.RecipeInput, T extends net.minecraft.world.item.crafting.Recipe<I>>
    List<RecipeHolder<T>> collectRecipes(RecipeType<T> type) {
        Minecraft client = Minecraft.getInstance();

        // Singleplayer / LAN host: full RecipeManager
        IntegratedServer integrated = client.getSingleplayerServer();
        if (integrated != null) {
            RecipeManager manager = integrated.getRecipeManager();
            List<RecipeHolder<T>> out = new ArrayList<>();
            for (RecipeHolder<?> holder : manager.getRecipes()) {
                if (holder.value().getType() == type) {
                    @SuppressWarnings("unchecked")
                    RecipeHolder<T> cast = (RecipeHolder<T>) holder;
                    out.add(cast);
                }
            }
            return out;
        }

        // Multiplayer: loader-specific synced recipe data
        if (ClientPlatform.isInitialized()) {
            List<RecipeHolder<T>> synced = ClientPlatform.client().getSyncedCustomRecipes(type);
            if (!synced.isEmpty()) {
                return synced;
            }
        }

        return List.of();
    }
}
