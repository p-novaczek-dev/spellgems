package net.pnovaczek.spellgems;

import net.pnovaczek.spellgems.loot.VillageManaRootLoot;
import net.pnovaczek.spellgems.network.ModNetworking;
import net.pnovaczek.spellgems.platform.Platform;
import net.pnovaczek.spellgems.registry.ModRegistries;
import net.pnovaczek.spellgems.spell.SpellBurstScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared mod constants and common initialization (loader-agnostic).
 * Loader entrypoints bootstrap {@link Platform} then call {@link #initConfig()} and {@link #initializeCommon()}.
 */
public final class Spellgems {
	public static final String MOD_ID = "spellgems";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/**
	 * Loaded after platform bootstrap via {@link #initConfig()}.
	 * Null until then — do not access during class static init of other types.
	 */
	public static SpellgemsConfig CONFIG;

	private Spellgems() {
	}

	/** Call after {@code Platform} is bootstrapped. */
	public static void initConfig() {
		if (CONFIG == null) {
			CONFIG = SpellgemsConfig.load();
		}
	}

	/**
	 * Content registration (loader-dependent lifecycle) plus spells, networking, loot hooks.
	 * <p>
	 * Content: Fabric runs {@link ModRegistries#registerAll()} immediately;
	 * NeoForge registers via {@code RegisterEvent} (see platform {@code registerModContent}).
	 */
	public static void initializeCommon() {
		initConfig();

		Platform.registries().registerModContent(ModRegistries::registerAll);

		ModSpells.initialize();
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
