package net.pnovaczek.spellgems;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.pnovaczek.spellgems.spell.SpellContext;
import net.pnovaczek.spellgems.spell.Spells;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class SpellgemsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public final Map<String, SpellConfig> spells = new HashMap<>();
    public int strikeEffectDuration = 100;
    public float spellEnchantmentDurabilityCostMultiplier = 2.0F;

    public static class SpellConfig {
        public int wandBaseDurabilityCost = 1;
    }

    public static class SpellCombatConfig extends SpellConfig {
        public float damage = 1.0F;
    }

    public static SpellgemsConfig load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("spellgems.json");
        SpellgemsConfig config = null;

        if (path.toFile().exists()) {
            try (Reader reader = new FileReader(path.toFile())) {
                config = GSON.fromJson(reader, SpellgemsConfig.class);
            } catch (IOException e) {
                Spellgems.LOGGER.error("Failed to load config", e);
            }
        }

        if (config == null) {
            config = new SpellgemsConfig();

            config.strikeEffectDuration = 100;
            config.spellEnchantmentDurabilityCostMultiplier = 2.0F;
            config.spells.put(Spells.PROJECTILE.getPath(), new SpellCombatConfig());
            config.spells.put(Spells.NOVA.getPath(), new SpellCombatConfig());
            config.spells.put(Spells.VORTEX.getPath(), new SpellCombatConfig());

            config.save();
        }
        return config;
    }

    public void save() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("spellgems.json");
        try (Writer writer = new FileWriter(path.toFile())) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            Spellgems.LOGGER.error("Failed to save config", e);
        }
    }
}