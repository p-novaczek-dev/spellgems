package net.pnovaczek.spellgems.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.*;
import net.pnovaczek.spellgems.ModEntities;
import net.pnovaczek.spellgems.ModMenuTypes;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.client.render.AstralArrowRenderer;
import net.pnovaczek.spellgems.client.render.SpellProjectileRenderer;
import net.pnovaczek.spellgems.client.screen.ManaInfuserScreen;
import net.pnovaczek.spellgems.client.screen.SpellEnchantingScreen;

public class SpellgemsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRenderers.register(ModEntities.ASTRAL_ARROW, AstralArrowRenderer::new);
		EntityRenderers.register(ModEntities.SPELL_PROJECTILE, SpellProjectileRenderer::new);
		MenuScreens.register(ModMenuTypes.MANA_INFUSER, ManaInfuserScreen::new);
		MenuScreens.register(ModMenuTypes.SPELL_ENCHANTING_TABLE, SpellEnchantingScreen::new);
		SpellgemsTooltips.register();
	}
}