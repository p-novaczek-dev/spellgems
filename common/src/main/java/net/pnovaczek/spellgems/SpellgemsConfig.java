package net.pnovaczek.spellgems;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.platform.Platform;
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
    /** Spell dispenser machine settings (cooldown is per-spell; burnout is global). */
    public final SpellDispenserConfig spellDispenser = new SpellDispenserConfig();
    public int strikeEffectDuration = 120;
    public float strikeCloudDamage = 2.0F;
    public float drainHealPerTarget = 2.0F;
    /** Damage dealt by Vampiric Mist to each enemy in the cloud, each interval. */
    public float vampiricMistDamage = 3.0F;
    /** Health restored to the caster per enemy damaged by Vampiric Mist. */
    public float vampiricMistHealPerTarget = 1.0F;
    /** Ticks between Vampiric Mist damage pulses. */
    public int vampiricMistIntervalTicks = 20;
    public float judgementDamage = 16.0F;
    /** Outward speed of Shatter/Combust/Judgement trigger burst particles. */
    public float strikeBurstParticleSpeed = 0.5F;
    public int chainingCount = 5;
    public int multishotCount = 5;
    /** Number of child projectiles spawned when Split hits a target. */
    public int splitCount = 3;
    /** How many times a projectile lineage may split (children can split again). */
    public int splitDepth = 2;
    /** Number of astral arrows spawned by the Volley strike enchantment. */
    public int volleyArrowCount = 8;
    /** Delay in ticks before a greater gem's second spell fires on a direct hit. */
    public int greaterGemFollowUpDelayTicks = 10;
    /** Max connected astral barrier blocks flipped by one redstone pulse. */
    public int astralBarrierMaxCompoundSize = 256;

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
        public SpellConfig placeBlock = new SpellConfig();
        public SpellConfig breakBlock = new SpellConfig();
        public AreaSpellConfig harvest = new AreaSpellConfig();
        public AreaSpellConfig plant = new AreaSpellConfig();
        public SpellConfig transmute = new SpellConfig();
        public GeodeResonanceSpellConfig geodeResonance = new GeodeResonanceSpellConfig();
        public ItemTransportSpellConfig itemTransport = new ItemTransportSpellConfig();

        public SpellConfigs() {
            // Provide the canonical default wand durability costs here.
            // These are used for new configs and when keys are absent from spellgems.json.
            projectile.wandDurabilityCost = 8;
            nova.wandDurabilityCost = 16;
            vortex.wandDurabilityCost = 12;
            blink.wandDurabilityCost = 48;
            placeBlock.wandDurabilityCost = 0;
            magnet.wandDurabilityCost = 0;
            potion.wandDurabilityCost = 192;
            transmute.wandDurabilityCost = 4;
            transmute.dispenserCooldownTicks = 1;
            geodeResonance.wandDurabilityCost = 128;
            geodeResonance.dispenserCooldownTicks = 40;
            itemTransport.wandDurabilityCost = 32;
            itemTransport.dispenserCooldownTicks = 1;
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
            if (spellId.equals(SpellIds.PLACE_BLOCK)) return placeBlock;
            if (spellId.equals(SpellIds.BREAK_BLOCK)) return breakBlock;
            if (spellId.equals(SpellIds.HARVEST)) return harvest;
            if (spellId.equals(SpellIds.PLANT)) return plant;
            if (spellId.equals(SpellIds.TRANSMUTE)) return transmute;
            if (spellId.equals(SpellIds.GEODE_RESONANCE)) return geodeResonance;
            if (spellId.equals(SpellIds.ITEM_TRANSPORT)) return itemTransport;

            // Unknown spell: return a fresh default (cost=1)
            return new SpellConfig();
        }

        public void validate() {
            if (projectile != null) projectile.validate();
            if (nova != null) nova.validate();
            if (vortex != null) vortex.validate();
            if (blink != null) blink.validate();
            if (magnet != null) magnet.validate();
            if (feed == null) feed = new FeedSpellConfig();
            feed.validate();
            if (grow == null) grow = new GrowSpellConfig();
            grow.validate();
            if (potion != null) potion.validate();
            if (placeBlock != null) placeBlock.validate();
            if (breakBlock != null) breakBlock.validate();
            if (harvest == null) harvest = new AreaSpellConfig();
            harvest.validate();
            if (plant == null) plant = new AreaSpellConfig();
            plant.validate();
            if (transmute != null) transmute.validate();
            if (geodeResonance == null) geodeResonance = new GeodeResonanceSpellConfig();
            geodeResonance.validate();
            if (itemTransport == null) itemTransport = new ItemTransportSpellConfig();
            itemTransport.validate();
        }
    }

    public static class ItemTransportSpellConfig extends SpellConfig {
        /** Maximum number of source stacks moved in one cast. */
        public int maxStacks = 27;

        public ItemTransportSpellConfig() {
            wandDurabilityCost = 32;
            dispenserCooldownTicks = 1;
        }

        @Override
        public void validate() {
            super.validate();
            maxStacks = Math.max(1, maxStacks);
        }
    }

    public static class GeodeResonanceSpellConfig extends SpellConfig {
        public int jumpCount = 4;
        public int jumpDelayTicks = 8;
        public float growthChance = 0.2F;
        public int searchRadius = 5;

        @Override
        public void validate() {
            super.validate();
            jumpCount = Math.max(1, jumpCount);
            jumpDelayTicks = Math.max(0, jumpDelayTicks);
            growthChance = Math.max(0f, Math.min(1f, growthChance));
            if (searchRadius < 1) searchRadius = 5;
        }
    }

    /** Square area around the caster. Side length shown in tooltips is {@code areaRadius * 2 + 1}. */
    public static class AreaSpellConfig extends SpellConfig {
        public int areaRadius = 4;

        @Override
        public void validate() {
            super.validate();
            if (areaRadius < 1) areaRadius = 4;
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
        /**
         * Cooldown in ticks after a successful cast from a spell dispenser.
         * Triggering again before this expires still fires the spell but causes burnout.
         */
        public int dispenserCooldownTicks = 20;

        public void validate() {
            wandDurabilityCost = Math.max(1, wandDurabilityCost);
            dispenserCooldownTicks = Math.max(0, dispenserCooldownTicks);
        }
    }

    public static class SpellDispenserConfig {
        /** Ticks the dispenser is disabled after firing during its spell cooldown (burnout). */
        public int burnoutTicks = 300;

        public void validate() {
            burnoutTicks = Math.max(0, burnoutTicks);
        }
    }

    public static class SpellCombatConfig extends SpellConfig {
        public float damage = 2.0F;
        public float powerDamageMultiplier = 4.0F;

        @Override
        public void validate() {
            super.validate();
            damage = Math.max(0f, damage);
            powerDamageMultiplier = Math.max(1f, powerDamageMultiplier);
        }
    }

    public static class NovaSpellConfig extends SpellCombatConfig {
        public float radius = 4.0F;
        public float centerYOffset = 0.75F;
        public float knockbackStrength = 0.6F;
        public float expandRadiusMultiplier = 1.5F;
        public int particleCount = 360;
        public float particleSpeed = 0.5F;

        @Override
        public void validate() {
            super.validate();
            radius = Math.max(0.1f, radius);
            centerYOffset = Math.max(-2f, Math.min(5f, centerYOffset));
            knockbackStrength = Math.max(0f, knockbackStrength);
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
            super.validate();
            maxDistance = Math.max(1.0, maxDistance);
            extendMultiplier = Math.max(1.0, extendMultiplier);
        }
    }

    public static class MagnetSpellConfig extends SpellConfig {
        public float range = 5.0F;
        public double extendMultiplier = 2.0;

        @Override
        public void validate() {
            super.validate();
            range = Math.max(0.5f, range);
            extendMultiplier = Math.max(1.0, extendMultiplier);
        }
    }

    public static class FeedSpellConfig extends SpellConfig {
        public boolean requireFeedItems = true;
        /** Horizontal reach from the caster, in blocks. */
        public float range = 8.0F;

        @Override
        public void validate() {
            super.validate();
            if (range < 0.5F) range = 8.0F;
        }
    }

    public static class GrowSpellConfig extends SpellConfig {
        public boolean requireBoneMeal = true;
        public int areaRadius = 4;

        @Override
        public void validate() {
            super.validate();
            if (areaRadius < 1) areaRadius = 4;
        }
    }

    public static class VortexSpellConfig extends SpellCombatConfig {
        public float radius = 4.0F;
        public float maxDistance = 16.0F;
        public float pullDistance = 1.0F;
        public float pullStrength = 1.0F;
        public float expandRadiusMultiplier = 1.5F;
        public int particleCount = 60;
        public float particleSpeed = 0.5F;

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
        Path path = Platform.paths().getConfigDir().resolve("spellgems.json");
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
        Path path = Platform.paths().getConfigDir().resolve("spellgems.json");
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
            // if future versions change field names or structure,
            // copy values from legacy locations here.
        }
    }

    /** Clamps values to safe ranges and ensures sub-configs are valid. */
    public void validate() {
        strikeEffectDuration = Math.max(1, strikeEffectDuration);
        strikeCloudDamage = Math.max(0f, strikeCloudDamage);
        drainHealPerTarget = Math.max(0f, drainHealPerTarget);
        vampiricMistDamage = Math.max(0f, vampiricMistDamage);
        vampiricMistHealPerTarget = Math.max(0f, vampiricMistHealPerTarget);
        vampiricMistIntervalTicks = Math.max(1, vampiricMistIntervalTicks);
        judgementDamage = Math.max(0f, judgementDamage);
        strikeBurstParticleSpeed = Math.max(0f, strikeBurstParticleSpeed);
        chainingCount = Math.max(1, chainingCount);
        multishotCount = Math.max(1, multishotCount);
        splitCount = Math.max(1, splitCount);
        splitDepth = Math.max(1, splitDepth);
        volleyArrowCount = Math.max(1, volleyArrowCount);
        greaterGemFollowUpDelayTicks = Math.max(0, greaterGemFollowUpDelayTicks);
        astralBarrierMaxCompoundSize = Math.max(1, astralBarrierMaxCompoundSize);

        if (wand != null) wand.validate();
        if (astralBow != null) astralBow.validate();
        if (spells != null) spells.validate();
        if (spellDispenser != null) spellDispenser.validate();
    }

    /** Per-spell dispenser cooldown in ticks (default 20). */
    public int getDispenserCooldownTicks(Identifier spellId) {
        return getSpellConfig(spellId).dispenserCooldownTicks;
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