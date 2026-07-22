package net.pnovaczek.spellgems;

import net.fabricmc.api.ModInitializer;
import net.pnovaczek.spellgems.loot.VillageManaRootLoot;
import net.pnovaczek.spellgems.network.ModNetworking;
import net.pnovaczek.spellgems.platform.Platform;
import net.pnovaczek.spellgems.platform.fabric.FabricPlatform;
import net.pnovaczek.spellgems.registry.ModRegistries;
import net.pnovaczek.spellgems.spell.SpellBurstScheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Spellgems implements ModInitializer {
	static {
		// Before CONFIG static init (and any Platform.* use).
		FabricPlatform.bootstrapCommon();
	}

	public static final String MOD_ID = "spellgems";
	public static SpellgemsConfig CONFIG = SpellgemsConfig.load();
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Content registries (idempotent; order documented on ModRegistries).
		ModRegistries.registerAll();

		ModSpells.initialize();
		// Wand durability costs now live solely in spells.*.wandDurabilityCost (no default methods on Spell impls).

		ModModifierEnchantments.initialize();
		ModStrikeEnchantments.initialize();
		ModUtilityEnchantments.initialize();

		SpellBurstScheduler.initialize();

		ModNetworking.registerPayloadTypes();
		ModNetworking.registerServerReceivers();

		Platform.lifecycle().onModifyLootTable((key, tableBuilder, builtin, registries) ->
				VillageManaRootLoot.tryInject(key, tableBuilder, builtin));
	}
}
