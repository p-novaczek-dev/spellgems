package net.pnovaczek.spellgems;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.pnovaczek.spellgems.platform.neoforge.NeoForgePlatform;

/**
 * NeoForge common entrypoint.
 */
@Mod(Spellgems.MOD_ID)
public class SpellgemsNeoForge {
    public SpellgemsNeoForge(IEventBus modBus, ModContainer container) {
        NeoForgePlatform.bootstrapCommon(modBus);
        Spellgems.initConfig();
        Spellgems.initializeCommon();
        Spellgems.LOGGER.info("Spellgems NeoForge common initialized");
    }
}
