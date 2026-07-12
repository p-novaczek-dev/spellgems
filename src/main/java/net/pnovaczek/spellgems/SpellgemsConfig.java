package net.pnovaczek.spellgems;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.spell.SpellIds;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class SpellgemsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** Schema version for migrations. Increment when breaking changes are made to the config structure. */
    public int version = 1;

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

        /** Returns a spell-specific config by ID (for more uniform access). */
        public SpellConfig get(Identifier spellId) {
            if (spellId.equals(SpellIds.PROJECTILE)) return projectile;
            if (spellId.equals(SpellIds.NOVA)) return nova;
            if (spellId.equals(SpellIds.VORTEX)) return vortex;
            if (spellId.equals(SpellIds.BLINK)) return blink;
            if (spellId.equals(SpellIds.MAGNET)) return magnet;
            if (spellId.equals(SpellIds.FEED)) return feed;
            if (spellId.equals(SpellIds.GROW)) return grow;
            if (spellId.equals(SpellIds.POTION)) return potion;

            // Other spells currently have no dedicated tunable config
            return new SpellConfig();
        }

        public void validate() {
            if (projectile != null) projectile.validate();
            if (nova != null) nova.validate();
            if (vortex != null) vortex.validate();
            if (blink != null) blink.validate();
            if (magnet != null) magnet.validate();
            if (feed != null) feed.validate();
            if (grow != null) grow.validate();
            // potion is empty
        }
    }

    public static class AstralBowConfig {
        public int normalShotDurabilityCost = 1;
        public int potionShotDurabilityCost = 4;

        public void validate() {
            normalShotDurabilityCost = Math.max(0, normalShotDurabilityCost);
            potionShotDurabilityCost = Math.max(0, potionShotDurabilityCost);
        }
    }

    public static class SpellConfig {
        public void validate() {
            // base does nothing
        }
    }

    public static class SpellCombatConfig extends SpellConfig {
        public float damage = 1.0F;

        @Override
        public void validate() {
            damage = Math.max(0f, damage);
        }
    }

    public static class NovaSpellConfig extends SpellCombatConfig {
        public float radius = 4.0F;
        public float centerYOffset = 0.75F;
        public float knockbackStrength = 0.3F;
        public float powerDamageMultiplier = 2.0F;
        public float expandRadiusMultiplier = 1.5F;
        public int particleCount = 90;
        public float particleSpeed = 0.1F;

        @Override
        public void validate() {
            super.validate();
            radius = Math.max(0.1f, radius);
            centerYOffset = Math.max(-2f, Math.min(5f, centerYOffset));
            knockbackStrength = Math.max(0f, knockbackStrength);
            powerDamageMultiplier = Math.max(1f, powerDamageMultiplier);
            expandRadiusMultiplier = Math.max(1f, expandRadiusMultiplier);
            particleCount = Math.max(1, particleCount);
            particleSpeed = Math.max(0f, particleSpeed);
        }
    }

    public static class BlinkSpellConfig extends SpellConfig {
        public double maxDistance = 16.0;

        @Override
        public void validate() {
            maxDistance = Math.max(1.0, maxDistance);
        }
    }

    public static class MagnetSpellConfig extends SpellConfig {
        public float range = 5.0F;

        @Override
        public void validate() {
            range = Math.max(0.5f, range);
        }
    }

    public static class FeedSpellConfig extends SpellConfig {
        public boolean requireFeedItems = true;

        @Override
        public void validate() {
            // boolean is fine
        }
    }

    public static class GrowSpellConfig extends SpellConfig {
        public boolean requireBoneMeal = true;

        @Override
        public void validate() {
            // boolean is fine
        }
    }

    public static class VortexSpellConfig extends SpellCombatConfig {
        public float radius = 4.0F;
        public float maxDistance = 16.0F;
        public float pullDistance = 0.5F;
        public float pullStrength = 0.5F;
        public float expandRadiusMultiplier = 1.5F;
        public int particleCount = 30;
        public float particleSpeed = 0.3F;

        public VortexSpellConfig() {
            damage = 0.0F;
        }

        @Override
        public void validate() {
            super.validate();
            radius = Math.max(0.1f, radius);
            maxDistance = Math.max(1.0f, maxDistance);
            pullDistance = Math.max(0f, pullDistance);
            pullStrength = Math.max(0f, pullStrength);
            expandRadiusMultiplier = Math.max(1f, expandRadiusMultiplier);
            particleCount = Math.max(1, particleCount);
            particleSpeed = Math.max(0f, particleSpeed);
        }
    }

    public static SpellgemsConfig load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("spellgems.json");
        SpellgemsConfig config = null;

        if (path.toFile().exists()) {
            try (Reader reader = new FileReader(path.toFile())) {
                config = GSON.fromJson(reader, SpellgemsConfig.class);
            } catch (Exception e) {
                Spellgems.LOGGER.error("Failed to load config (using defaults)", e);
            }
        }

        if (config == null) {
            config = new SpellgemsConfig();
            // Seed and validate even for brand new configs (seeding may be partial until ModSpells is ready)
            config.wand.seedDefaultsFromRegisteredSpells();
            config.validate();
            config.save();
        } else {
            config.migrate();
            config.validate();
            // Make sure any spells added after the user's last save have entries
            // (will use the spell's declared defaultDurabilityCost).
            config.wand.seedDefaultsFromRegisteredSpells();
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

    /** Performs any necessary migrations from older config versions. */
    private void migrate() {
        if (version < 1) {
            version = 1;
            // Example: if future versions change field names or structure,
            // copy values from legacy locations here.
        }
        // Ensure sub-configs are not null after deserialization from very old files
        if (spells == null) {
            // This shouldn't normally happen, but defensive
        }
    }

    /** Clamps values to safe ranges and ensures sub-configs are valid. */
    public void validate() {
        strikeEffectDuration = Math.max(1, strikeEffectDuration);
        strikeCloudDamage = Math.max(0f, strikeCloudDamage);
        drainHealPerTarget = Math.max(0f, drainHealPerTarget);

        if (wand != null) wand.validate();
        if (astralBow != null) astralBow.validate();
        if (spells != null) spells.validate();
    }

    /**
     * Returns the configuration object associated with a spell, if any.
     * Falls back to a default empty config.
     */
    public SpellConfig getSpellConfig(Identifier spellId) {
        return spells != null ? spells.get(spellId) : new SpellConfig();
    }
}