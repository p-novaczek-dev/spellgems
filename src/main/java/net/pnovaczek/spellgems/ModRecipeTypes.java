package net.pnovaczek.spellgems;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.recipe.ManaInfuserRecipe;
import net.pnovaczek.spellgems.recipe.SpellEnchantingRecipe;

public class ModRecipeTypes {
    public static void register() {
        Registry.register(
                BuiltInRegistries.RECIPE_TYPE,
                Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "mana_infusing"),
                ManaInfuserRecipe.TYPE
        );

        Registry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "mana_infusing"),
                ManaInfuserRecipe.SERIALIZER
        );

        Registry.register(
                BuiltInRegistries.RECIPE_TYPE,
                Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "spell_enchanting"),
                SpellEnchantingRecipe.TYPE
        );

        Registry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "spell_enchanting"),
                SpellEnchantingRecipe.SERIALIZER
        );
    }
}