package net.pnovaczek.spellgems;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModTags {

    public static final TagKey<Item> COMBAT_SPELL_GEMS = TagKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "combat_spell_gems")
    );

    public static final TagKey<Item> UTILITY_SPELL_GEMS = TagKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "utility_spell_gems")
    );

    public static final TagKey<Item> SMELT_SPELL_GEMS = TagKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "smelt_spell_gems")
    );

    public static final TagKey<Item> EXTEND_SPELL_GEMS = TagKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "extend_spell_gems")
    );

    public static final TagKey<Item> CATALYST_BOOKS = TagKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "catalyst_books")
    );

    public static final TagKey<Item> WAND_ENCHANTABLE = TagKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "wand_enchantable")
    );

    public static void register() {
        // empty - just forces class loading if needed
    }
}
