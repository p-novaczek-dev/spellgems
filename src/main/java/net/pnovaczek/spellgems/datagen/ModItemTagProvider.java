package net.pnovaczek.spellgems.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.pnovaczek.spellgems.ModItems;
import net.pnovaczek.spellgems.ModTags;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.item.SpellGemItem;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagsProvider.ItemTagsProvider {

    public ModItemTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        builder(ModTags.COMBAT_SPELL_GEMS)
                .add(ModItems.keyOfItem("spell_gem_projectile"))
                .add(ModItems.keyOfItem("spell_gem_nova"))
                .add(ModItems.keyOfItem("spell_gem_vortex"));

        builder(ModTags.UTILITY_SPELL_GEMS)
                .add(ModItems.keyOfItem("spell_gem_blink"))
                .add(ModItems.keyOfItem("spell_gem_wind_charge"))
                .add(ModItems.keyOfItem("spell_gem_magnet"))
                .add(ModItems.keyOfItem("spell_gem_place_block"))
                .add(ModItems.keyOfItem("spell_gem_break_block"))
                .add(ModItems.keyOfItem("spell_gem_plant"))
                .add(ModItems.keyOfItem("spell_gem_harvest"))
                .add(ModItems.keyOfItem("spell_gem_feed"))
                .add(ModItems.keyOfItem("spell_gem_grow"))
                .add(ModItems.keyOfItem("spell_gem_potion"));

        builder(ModTags.SMELT_SPELL_GEMS)
                .add(ModItems.keyOfItem("spell_gem_break_block"))
                .add(ModItems.keyOfItem("spell_gem_harvest"));

        builder(ModTags.WAND_ENCHANTABLE)
                .add(ModItems.keyOfItem("wand"));

        builder(ModTags.CATALYST_BOOKS)
                .add(ModItems.keyOfItem("spell_tome"));

        // Make mana root (and thus the plant spell) recognize it as a plantable seed,
        // and allow vanilla systems (villagers etc.) to treat it as one.
        builder(ItemTags.VILLAGER_PLANTABLE_SEEDS)
                .add(ModItems.keyOfItem("mana_root"));
    }
}
