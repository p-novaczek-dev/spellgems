package net.pnovaczek.spellgems;

import net.minecraft.core.registries.BuiltInRegistries;
import net.pnovaczek.spellgems.recipe.ManaInfuserRecipe;
import net.pnovaczek.spellgems.recipe.SpellEnchantingRecipe;
import net.pnovaczek.spellgems.registry.ModRegistry;

public class ModRecipeTypes {
    private ModRecipeTypes() {
    }

    public static void register() {
        ModRegistry.register(
                BuiltInRegistries.RECIPE_TYPE,
                "mana_infusing",
                ManaInfuserRecipe.TYPE
        );
        ModRegistry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                "mana_infusing",
                ManaInfuserRecipe.SERIALIZER
        );
        ModRegistry.register(
                BuiltInRegistries.RECIPE_TYPE,
                "spell_enchanting",
                SpellEnchantingRecipe.TYPE
        );
        ModRegistry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                "spell_enchanting",
                SpellEnchantingRecipe.SERIALIZER
        );
    }
}
