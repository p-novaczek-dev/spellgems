package net.pnovaczek.spellgems;

import net.fabricmc.api.ModInitializer;
import net.pnovaczek.spellgems.platform.fabric.FabricPlatform;

/**
 * Fabric main entrypoint. Declared in {@code fabric.mod.json}.
 */
public class SpellgemsFabric implements ModInitializer {
	static {
		FabricPlatform.bootstrapCommon();
		Spellgems.initConfig();
	}

	@Override
	public void onInitialize() {
		Spellgems.initializeCommon();
	}
}
