package net.pnovaczek.spellgems.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.pnovaczek.spellgems.client.renderer.item.properties.numeric.AstralBowPull;
import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.ModEntities;
import net.pnovaczek.spellgems.ModMenuTypes;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.client.network.ModClientNetworking;
import net.pnovaczek.spellgems.spell.SpellBurstScheduler;
import net.pnovaczek.spellgems.client.render.AstralArrowRenderer;
import net.pnovaczek.spellgems.client.render.SpellProjectileRenderer;
import net.pnovaczek.spellgems.client.screen.AstralBowScreen;
import net.pnovaczek.spellgems.client.screen.ManaInfuserScreen;
import net.pnovaczek.spellgems.client.screen.SpellDispenserScreen;
import net.pnovaczek.spellgems.client.screen.SpellEnchantingScreen;
import net.pnovaczek.spellgems.client.screen.WandScreen;

public class SpellgemsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		RangeSelectItemModelProperties.ID_MAPPER.put(
				Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "astral_bow/pull"),
				AstralBowPull.MAP_CODEC
		);

		EntityRenderers.register(ModEntities.ASTRAL_ARROW, AstralArrowRenderer::new);
		EntityRenderers.register(ModEntities.SPELL_PROJECTILE, SpellProjectileRenderer::new);
		EntityRenderers.register(ModEntities.INFERNO_CLOUD, NoopRenderer::new);
		EntityRenderers.register(ModEntities.FROSTBITE_CLOUD, NoopRenderer::new);
		EntityRenderers.register(ModEntities.PLAGUE_CLOUD, NoopRenderer::new);
		MenuScreens.register(ModMenuTypes.MANA_INFUSER, ManaInfuserScreen::new);
		MenuScreens.register(ModMenuTypes.SPELL_ENCHANTING_TABLE, SpellEnchantingScreen::new);
		MenuScreens.register(ModMenuTypes.SPELL_DISPENSER, SpellDispenserScreen::new);
		MenuScreens.register(ModMenuTypes.WAND, WandScreen::new);
		MenuScreens.register(ModMenuTypes.ASTRAL_BOW, AstralBowScreen::new);
		SpellgemsKeyMappings.register();
		SpellgemsTooltips.register();
		ModClientNetworking.register();
		WandClientInput.register();
		WandSpellHighlight.register();

		// Drop predicted burst pulses when leaving a world / disconnecting.
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> SpellBurstScheduler.clearClient());

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.level == null) {
				SpellBurstScheduler.clearClient();
				return;
			}
			SpellBurstScheduler.tickClient(client.level.getGameTime());
		});
	}
}