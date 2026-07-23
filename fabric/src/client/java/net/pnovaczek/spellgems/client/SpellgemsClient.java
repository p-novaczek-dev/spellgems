package net.pnovaczek.spellgems.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.ModMenuTypes;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.client.renderer.item.properties.numeric.AstralBowPull;
import net.pnovaczek.spellgems.client.screen.AstralBowScreen;
import net.pnovaczek.spellgems.client.screen.ManaInfuserScreen;
import net.pnovaczek.spellgems.client.screen.SpellDispenserScreen;
import net.pnovaczek.spellgems.client.screen.SpellEnchantingScreen;
import net.pnovaczek.spellgems.client.screen.WandScreen;
import net.pnovaczek.spellgems.platform.client.fabric.FabricClientPlatform;

/**
 * Fabric client entrypoint. Declared in {@code fabric.mod.json}.
 * Main init has already registered content, so entity types are available.
 */
public class SpellgemsClient implements ClientModInitializer {
	static {
		FabricClientPlatform.bootstrap();
	}

	@Override
	public void onInitializeClient() {
		// Fabric access-widens these vanilla registration helpers.
		RangeSelectItemModelProperties.ID_MAPPER.put(
				Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "astral_bow/pull"),
				AstralBowPull.MAP_CODEC
		);
		MenuScreens.register(ModMenuTypes.MANA_INFUSER, ManaInfuserScreen::new);
		MenuScreens.register(ModMenuTypes.SPELL_ENCHANTING_TABLE, SpellEnchantingScreen::new);
		MenuScreens.register(ModMenuTypes.SPELL_DISPENSER, SpellDispenserScreen::new);
		MenuScreens.register(ModMenuTypes.WAND, WandScreen::new);
		MenuScreens.register(ModMenuTypes.ASTRAL_BOW, AstralBowScreen::new);

		SpellgemsClientBootstrap.registerEntityRenderers();
		SpellgemsClientBootstrap.initializeClient();
	}
}
