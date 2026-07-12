package net.pnovaczek.spellgems;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pnovaczek.spellgems.network.ModNetworking;

public class Spellgems implements ModInitializer {
	public static final String MOD_ID = "spellgems";
	public static SpellgemsConfig CONFIG = SpellgemsConfig.load();
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModBlocks.initialize();
		ModBlockEntities.initialize();
		ModItems.initialize();
		ModCreativeModeTabs.initialize();
		ModRecipeTypes.register();
		ModEntities.initialize();
		ModMenuTypes.initialize();
		ModComponents.initialize();
		ModEntityDataSerializers.register();

		ModSpells.initialize();
		// Seed wand spell costs from the spells' own defaultDurabilityCost() declarations
		// so the config contains useful entries for all spells.
		CONFIG.wand.seedDefaultsFromRegisteredSpells();

		ModModifierEnchantments.initialize();
		ModStrikeEnchantments.initialize();
		ModUtilityEnchantments.initialize();

		ModEnchantmentEffects.register();
		ModNetworking.registerPayloadTypes();
		ModNetworking.registerServerReceivers();

		// Add mana root to village house chests via a custom loot table reference.
		// This lets players find mana root (as seeds/crop) in village chests.
		ResourceKey<LootTable> manaRootLoot =
				ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(MOD_ID, "chests/village_mana_root"));

		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			if (!source.isBuiltin()) {
				return;
			}
			String path = key.identifier().getPath();
			if (path.startsWith("chests/village/village_") && path.endsWith("_house")) {
				// Inject our mana root loot table as an additional pool entry.
				// The referenced table controls the actual chance/quantity.
				tableBuilder.withPool(
						LootPool.lootPool()
								.add(NestedLootTable.lootTableReference(manaRootLoot))
				);
			}
		});
	}
}