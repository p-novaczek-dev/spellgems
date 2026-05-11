package net.pnovaczek.spellgems;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Spellgems implements ModInitializer {
	public static final String MOD_ID = "spellgems";
	public static SpellgemsConfig CONFIG = SpellgemsConfig.load();
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModBlocks.initialize();
		ModBlockEntities.initialize();
		ModItems.initialize();
		ModRecipeTypes.register();
		ModEntities.initialize();
		ModMenuTypes.initialize();
		ModComponents.initialize();

		ModSpells.initialize();
		ModModifierEnchantments.initialize();
		ModStrikeEnchantments.initialize();
		ModUtilityEnchantments.initialize();
	}
}