package net.pnovaczek.spellgems.client;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.pnovaczek.spellgems.datagen.ModBlockLootTableProvider;
import net.pnovaczek.spellgems.datagen.ModBlockTagProvider;
import net.pnovaczek.spellgems.datagen.ModItemTagProvider;
import net.pnovaczek.spellgems.datagen.ModRecipeProvider;
import net.pnovaczek.spellgems.platform.fabric.FabricPlatform;
import net.pnovaczek.spellgems.registry.ModRegistries;

public class SpellgemsDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		// Datagen does not always run the main ModInitializer; ensure content exists.
		FabricPlatform.bootstrapCommon();
		ModRegistries.registerAll();

		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(ModRecipeProvider::new);
		pack.addProvider(ModBlockLootTableProvider::new);
		pack.addProvider(ModBlockTagProvider::new);
		pack.addProvider(ModItemTagProvider::new);
	}
}
