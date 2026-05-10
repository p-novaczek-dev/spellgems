package net.pnovaczek.spellgems.client;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.pnovaczek.spellgems.datagen.ModBlockLootTableProvider;
import net.pnovaczek.spellgems.datagen.ModBlockTagProvider;
import net.pnovaczek.spellgems.datagen.ModItemTagProvider;
import net.pnovaczek.spellgems.datagen.ModRecipeProvider;

public class SpellgemsDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(ModRecipeProvider::new);
		pack.addProvider(ModBlockLootTableProvider::new);
		pack.addProvider(ModBlockTagProvider::new);
		pack.addProvider(ModItemTagProvider::new);
	}
}
