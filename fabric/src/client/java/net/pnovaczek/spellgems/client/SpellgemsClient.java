package net.pnovaczek.spellgems.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.ModMenuTypes;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.client.particle.SpellMoteParticleProviders;
import net.pnovaczek.spellgems.client.renderer.item.properties.numeric.AstralBowPull;
import net.pnovaczek.spellgems.client.screen.AstralBowScreen;
import net.pnovaczek.spellgems.client.screen.GemForgeScreen;
import net.pnovaczek.spellgems.client.screen.ManaInfuserScreen;
import net.pnovaczek.spellgems.client.screen.SpellDispenserScreen;
import net.pnovaczek.spellgems.client.screen.SpellEnchantingScreen;
import net.pnovaczek.spellgems.client.screen.WandScreen;
import net.pnovaczek.spellgems.platform.client.fabric.FabricClientPlatform;

import java.util.function.Function;

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
		MenuScreens.register(ModMenuTypes.GEM_FORGE, GemForgeScreen::new);

		SpellgemsClientBootstrap.registerEntityRenderers();
		SpellgemsClientBootstrap.initializeClient();
		SpellMoteParticleProviders.register(SpellgemsClient::registerParticle);
	}

	private static <T extends ParticleOptions> void registerParticle(
			ParticleType<T> type,
			Function<SpriteSet, ParticleProvider<T>> factory
	) {
		ParticleProviderRegistry.getInstance().register(type, sprites -> factory.apply(sprites));
	}
}
