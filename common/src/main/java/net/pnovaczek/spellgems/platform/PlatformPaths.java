package net.pnovaczek.spellgems.platform;

import java.nio.file.Path;

/**
 * Loader-agnostic filesystem locations (config dir, etc.).
 */
public interface PlatformPaths {
    Path getConfigDir();
}
