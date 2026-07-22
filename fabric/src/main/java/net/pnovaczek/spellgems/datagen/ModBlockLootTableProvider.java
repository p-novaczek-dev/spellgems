package net.pnovaczek.spellgems.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.pnovaczek.spellgems.ModBlocks;
import net.pnovaczek.spellgems.ModItems;

import java.util.concurrent.CompletableFuture;

public class ModBlockLootTableProvider extends FabricBlockLootSubProvider {
    public ModBlockLootTableProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate() {
        add(ModBlocks.MANA_ROOT, createCropDrops(
                ModBlocks.MANA_ROOT,
                ModItems.MANA_ROOT,
                ModItems.MANA_ROOT,
                LootItemBlockStatePropertyCondition
                        .hasBlockStateProperties(ModBlocks.MANA_ROOT)
                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                .hasProperty(CropBlock.AGE, ModBlocks.MANA_ROOT.getMaxAge()))
        ));

        dropSelf(ModBlocks.MANA_INFUSER);
        dropSelf(ModBlocks.SPELL_DISPENSER);
    }
}