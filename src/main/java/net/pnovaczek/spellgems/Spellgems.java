package net.pnovaczek.spellgems;

import net.fabricmc.api.ModInitializer;

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
	}
}