package net.pnovaczek.spellgems.platform.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.pnovaczek.spellgems.platform.PlatformPaths;

import java.nio.file.Path;

public final class FabricPaths implements PlatformPaths {
    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
