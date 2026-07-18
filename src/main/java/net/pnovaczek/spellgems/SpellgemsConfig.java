package net.pnovaczek.spellgems;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.spell.SpellIds;

import java.io.*;
import java.nio.file.Path;

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
    public int chainingCount = 5;
    public int multishotCount = 5;

    public static class WandConfig {
        /** Multiplier applied to base durability cost for each spell enchantment on a gem. */
        public float spellEnchantmentDurabilityCostMultiplier = 3.0F;

        public WandConfig() {
        }

        public void validate() {
            spellEnchantmentDurabilityCostMultiplier = Math.max(1f, spellEnchantmentDurabilityCostMultiplier);
        }
    }

    public static class SpellConfigs {
        public SpellCombatConfig projectile = new SpellCombatConfig();
        public NovaSpellConfig nova = new NovaSpellConfig();
        public VortexSpellConfig vortex = new VortexSpellConfig();
        public BlinkSpellConfig blink = new BlinkSpellConfig();
        public MagnetSpellConfig magnet = new MagnetSpellConfig();
        public FeedSpellConfig feed = new FeedSpellConfig();
        public GrowSpellConfig grow = new GrowSpellConfig();
        public SpellConfig potion = new SpellConfig();

        // Additional spell configs (for uniform access and to host wandDurabilityCost etc.)
        public SpellConfig windCharge = new SpellConfig();
        public SpellConfig placeBlock = new SpellConfig();
        public SpellConfig breakBlock = new SpellConfig();
        public SpellConfig harvest = new SpellConfig();
        public SpellConfig plant = new SpellConfig();

        public SpellConfigs() {
            // Provide the canonical default wand durability costs here.
            // These are used for new configs and when keys are absent from spellgems.json.
            // (Previously these lived in each Spell subclass via defaultDurabilityCost().)
            projectile.wandDurabilityCost = 8;
            nova.wandDurabilityCost = 16;
            vortex.wandDurabilityCost = 12;
            blink.wandDurabilityCost = 48;
            windCharge.wandDurabilityCost = 2;
            placeBlock.wandDurabilityCost = 0;
            magnet.wandDurabilityCost = 0;
            // All others default to 1.
        }

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
            if (spellId.equals(SpellIds.WIND_CHARGE)) return windCharge;
            if (spellId.equals(SpellIds.PLACE_BLOCK)) return placeBlock;
            if (spellId.equals(SpellIds.BREAK_BLOCK)) return breakBlock;
            if (spellId.equals(SpellIds.HARVEST)) return harvest;
            if (spellId.equals(SpellIds.PLANT)) return plant;

            // Unknown spell: return a fresh default (cost=1)
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
            if (potion != null) potion.validate();
            if (windCharge != null) windCharge.validate();
            if (placeBlock != null) placeBlock.validate();
            if (breakBlock != null) breakBlock.validate();
            if (harvest != null) harvest.validate();
            if (plant != null) plant.validate();
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
        /** Durability cost when casting this spell from a wand. Always >= 1. */
        public int wandDurabilityCost = 1;

        public void validate() {
            wandDurabilityCost = Math.max(1, wandDurabilityCost);
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
        public double maxDistance = 12.0;
        public double extendMultiplier = 2.0;

        @Override
        public void validate() {
            maxDistance = Math.max(1.0, maxDistance);
            extendMultiplier = Math.max(1.0, extendMultiplier);
        }
    }

    public static class MagnetSpellConfig extends SpellConfig {
        public float range = 5.0F;
        public double extendMultiplier = 2.0;

        @Override
        public void validate() {
            range = Math.max(0.5f, range);
            extendMultiplier = Math.max(1.0, extendMultiplier);
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
            config.validate();
            config.save();
        } else {
            config.migrate();
            config.validate();
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
        chainingCount = Math.max(1, chainingCount);
        multishotCount = Math.max(1, multishotCount);

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

    /** Returns the wand durability cost for the given spell (from its spell config). */
    public int getWandDurabilityCost(Identifier spellId) {
        return getSpellConfig(spellId).wandDurabilityCost;
    }
}