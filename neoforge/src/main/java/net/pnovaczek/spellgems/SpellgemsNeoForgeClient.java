package net.pnovaczek.spellgems;

import java.util.function.Function;

import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;
import net.pnovaczek.spellgems.client.SpellgemsClientBootstrap;
import net.pnovaczek.spellgems.client.particle.SpellMoteParticleProviders;
import net.pnovaczek.spellgems.client.renderer.item.properties.numeric.AstralBowPull;
import net.pnovaczek.spellgems.client.screen.AstralBowScreen;
import net.pnovaczek.spellgems.client.screen.GemForgeScreen;
import net.pnovaczek.spellgems.client.screen.ManaInfuserScreen;
import net.pnovaczek.spellgems.client.screen.SpellDispenserScreen;
import net.pnovaczek.spellgems.client.screen.SpellEnchantingScreen;
import net.pnovaczek.spellgems.client.screen.WandScreen;
import net.pnovaczek.spellgems.platform.client.neoforge.NeoForgeClientPlatform;

/**
 * NeoForge client-only entrypoint.
 * <p>
 * Entity/menu types are registered later via {@code RegisterEvent}; client object
 * registration that needs those types must use the matching Neo client events.
 */
@Mod(value = Spellgems.MOD_ID, dist = Dist.CLIENT)
public class SpellgemsNeoForgeClient {
    public SpellgemsNeoForgeClient(IEventBus modBus, ModContainer container) {
        NeoForgeClientPlatform.bootstrap(modBus);

        modBus.addListener(this::onRegisterMenus);
        modBus.addListener(this::onRegisterRangeSelect);
        modBus.addListener(this::onRegisterEntityRenderers);
        modBus.addListener(this::onRegisterParticles);

        // Keys/tooltips/ticks only — not entity renderers (ModEntities still null here).
        SpellgemsClientBootstrap.initializeClient();
        Spellgems.LOGGER.info("Spellgems NeoForge client initialized");
    }

    private void onRegisterMenus(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.MANA_INFUSER, ManaInfuserScreen::new);
        event.register(ModMenuTypes.SPELL_ENCHANTING_TABLE, SpellEnchantingScreen::new);
        event.register(ModMenuTypes.SPELL_DISPENSER, SpellDispenserScreen::new);
        event.register(ModMenuTypes.WAND, WandScreen::new);
        event.register(ModMenuTypes.ASTRAL_BOW, AstralBowScreen::new);
        event.register(ModMenuTypes.GEM_FORGE, GemForgeScreen::new);
    }

    private void onRegisterRangeSelect(RegisterRangeSelectItemModelPropertyEvent event) {
        event.register(
                Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "astral_bow/pull"),
                AstralBowPull.MAP_CODEC
        );
    }

    private void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Entity types are registered by the time this event fires.
        SpellgemsClientBootstrap.registerEntityRenderers();
    }

    private void onRegisterParticles(RegisterParticleProvidersEvent event) {
        SpellMoteParticleProviders.register(new SpellMoteParticleProviders.Registrar() {
            @Override
            public <T extends ParticleOptions> void register(
                    ParticleType<T> type,
                    Function<SpriteSet, ParticleProvider<T>> factory
            ) {
                event.registerSpriteSet(type, factory::apply);
            }
        });
    }
}
