package net.pnovaczek.spellgems.platform.neoforge;

import net.neoforged.fml.loading.FMLPaths;
import net.pnovaczek.spellgems.platform.PlatformPaths;

import java.nio.file.Path;

public final class NeoForgePaths implements PlatformPaths {
    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }
}
