package net.pnovaczek.spellgems;

import net.minecraft.core.registries.BuiltInRegistries;
import net.pnovaczek.spellgems.recipe.ManaInfuserRecipe;
import net.pnovaczek.spellgems.recipe.SpellEnchantingRecipe;
import net.pnovaczek.spellgems.registry.ModRegistry;

public class ModRecipeTypes {
    private ModRecipeTypes() {
    }

    /** Fabric convenience: both registries. NeoForge calls the split methods from RegisterEvent. */
    public static void register() {
        registerTypes();
        registerSerializers();
    }

    public static void registerTypes() {
        ModRegistry.register(
                BuiltInRegistries.RECIPE_TYPE,
                "mana_infusing",
                ManaInfuserRecipe.TYPE
        );
        ModRegistry.register(
                BuiltInRegistries.RECIPE_TYPE,
                "spell_enchanting",
                SpellEnchantingRecipe.TYPE
        );
    }

    public static void registerSerializers() {
        ModRegistry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                "mana_infusing",
                ManaInfuserRecipe.SERIALIZER
        );
        ModRegistry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                "spell_enchanting",
                SpellEnchantingRecipe.SERIALIZER
        );
    }
}
