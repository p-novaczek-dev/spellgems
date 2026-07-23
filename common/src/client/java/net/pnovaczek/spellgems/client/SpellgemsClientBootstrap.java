package net.pnovaczek.spellgems.client;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.pnovaczek.spellgems.ModEntities;
import net.pnovaczek.spellgems.client.network.ModClientNetworking;
import net.pnovaczek.spellgems.client.render.AstralArrowRenderer;
import net.pnovaczek.spellgems.client.render.SpellProjectileRenderer;
import net.pnovaczek.spellgems.platform.client.ClientPlatform;
import net.pnovaczek.spellgems.spell.SpellBurstScheduler;

/**
 * Shared client setup used by Fabric and NeoForge client entrypoints.
 * Requires {@link ClientPlatform} to be bootstrapped first.
 * <p>
 * Menu screens and range-select item model properties are registered by each loader
 * (Fabric can call vanilla helpers; NeoForge uses mod-bus events).
 * <p>
 * Entity renderers must run only after entity types exist:
 * <ul>
 *   <li>Fabric: after main {@code ModRegistries.registerAll()} (client init is later)</li>
 *   <li>NeoForge: from {@code EntityRenderersEvent.RegisterRenderers} after {@code RegisterEvent}</li>
 * </ul>
 */
public final class SpellgemsClientBootstrap {
	private SpellgemsClientBootstrap() {
	}

	/**
	 * Non-entity client hooks (keys, tooltips, networking, ticks).
	 * Safe to call before entity types are registered.
	 */
	public static void initializeClient() {
		SpellgemsKeyMappings.register();
		SpellgemsTooltips.register();
		ModClientNetworking.register();
		WandClientInput.register();
		WandSpellHighlight.register();

		ClientPlatform.client().onClientDisconnect(client -> SpellBurstScheduler.clearClient());

		ClientPlatform.client().onEndClientTick(client -> {
			if (client.level == null) {
				SpellBurstScheduler.clearClient();
				return;
			}
			SpellBurstScheduler.tickClient(client.level.getGameTime());
		});
	}

	/**
	 * Registers entity renderers. Call only when {@link ModEntities} fields are non-null.
	 */
	public static void registerEntityRenderers() {
		EntityRenderers.register(ModEntities.ASTRAL_ARROW, AstralArrowRenderer::new);
		EntityRenderers.register(ModEntities.SPELL_PROJECTILE, SpellProjectileRenderer::new);
		EntityRenderers.register(ModEntities.INFERNO_CLOUD, NoopRenderer::new);
		EntityRenderers.register(ModEntities.FROSTBITE_CLOUD, NoopRenderer::new);
		EntityRenderers.register(ModEntities.PLAGUE_CLOUD, NoopRenderer::new);
	}
}
