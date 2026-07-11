package net.pnovaczek.spellgems;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;

public class SpellgemsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public final SpellConfigs spells = new SpellConfigs();
    public final WandConfig wand = new WandConfig();
    public final AstralBowConfig astralBow = new AstralBowConfig();
    public int strikeEffectDuration = 100;
    public float strikeCloudDamage = 2.0F;
    public float drainHealPerTarget = 2.0F;

    public static class SpellConfigs {
        public SpellCombatConfig projectile = new SpellCombatConfig();
        public NovaSpellConfig nova = new NovaSpellConfig();
        public VortexSpellConfig vortex = new VortexSpellConfig();
        public BlinkSpellConfig blink = new BlinkSpellConfig();
        public MagnetSpellConfig magnet = new MagnetSpellConfig();
        public FeedSpellConfig feed = new FeedSpellConfig();
        public GrowSpellConfig grow = new GrowSpellConfig();
        public SpellConfig potion = new SpellConfig();
    }

    public static class AstralBowConfig {
        public int normalShotDurabilityCost = 1;
        public int potionShotDurabilityCost = 4;
    }

    public static class SpellConfig {
    }

    public static class SpellCombatConfig extends SpellConfig {
        public float damage = 1.0F;
    }

    public static class NovaSpellConfig extends SpellCombatConfig {
        public float radius = 4.0F;
        public float centerYOffset = 0.75F;
        public float knockbackStrength = 0.3F;
        public float powerDamageMultiplier = 2.0F;
        public float expandRadiusMultiplier = 1.5F;
    }

    public static class BlinkSpellConfig extends SpellConfig {
        public double maxDistance = 16.0;
    }

    public static class MagnetSpellConfig extends SpellConfig {
        public float range = 5.0F;
    }

    public static class FeedSpellConfig extends SpellConfig {
        public boolean requireFeedItems = true;
    }

    public static class GrowSpellConfig extends SpellConfig {
        public boolean requireBoneMeal = true;
    }

    public static class VortexSpellConfig extends SpellCombatConfig {
        public float radius = 3.0F;
        public float maxDistance = 16.0F;
        public float pullDistance = 2.5F;
        public float pullStrength = 1.5F;
        public float expandRadiusMultiplier = 1.5F;
        public int particleCount = 48;
        public float particleSpeed = 0.12F;

        public VortexSpellConfig() {
            damage = 0.0F;
        }
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