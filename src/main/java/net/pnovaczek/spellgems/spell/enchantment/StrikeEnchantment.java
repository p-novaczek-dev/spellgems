package net.pnovaczek.spellgems.spell.enchantment;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.entity.InfernoCloud;

public record StrikeEnchantment(Identifier id) {

    public static final Codec<StrikeEnchantment> CODEC = Identifier.CODEC.xmap(
            StrikeEnchantment::new,
            StrikeEnchantment::id
    );

    public void applyTo(LivingEntity living, LivingEntity caster) {
        var duration = Spellgems.CONFIG.strikeEffectDuration;
        Level level = living.level();
        Vec3 pos = living.position();

        if (is(StrikeEnchantments.POISON)) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, duration, 0));
        } else if (is(StrikeEnchantments.FLAME)) {
            living.setRemainingFireTicks(duration);
        } else if (is(StrikeEnchantments.FROST)) {
            living.setTicksFrozen(duration);
        } else if (is(StrikeEnchantments.SLOW)) {
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, 0));
        } else if (is(StrikeEnchantments.LEVITATE)) {
            living.addEffect(new MobEffectInstance(MobEffects.LEVITATION, duration, 0));
        } else if (is(StrikeEnchantments.INFERNO)) {
            if (living.getRemainingFireTicks() > 0 && !level.isClientSide()) {
                InfernoCloud cloud = new InfernoCloud(level, pos.x(), pos.y(), pos.z(), caster);
                level.addFreshEntity(cloud);
            }
        } else if (is(StrikeEnchantments.FROSTBITE)) {
            // TODO: if target freezing, apply wither + immobilize (high slowness) (~8 lines)
        } else if (is(StrikeEnchantments.PLAGUE)) {
            // TODO: if target poisoned, apply wither + slowness (~5 lines)
        } else if (is(StrikeEnchantments.LIGHTNING)) {
            // TODO: if target slowed, spawn lightning bolt (~6 lines)
        } else if (is(StrikeEnchantments.EXPLOSION)) {
            // TODO: if target levitating, create explosion at target pos (~5 lines)
        } else if (is(StrikeEnchantments.DRAIN)) {
            // TODO: heal caster per target + extra dmg if withered (requires caster param; ~15 lines)
        } else if (is(StrikeEnchantments.THERMAL_INVERSION)) {
            // TODO: if target burning, apply freezing + levitation (~6 lines)
        } else if (is(StrikeEnchantments.PURIFY)) {
            // TODO: extra dmg (elsewhere?) + if poisoned set on fire (~4 lines)
        } else if (is(StrikeEnchantments.VOLLEY)) {
            // TODO: if slowed/levitating, spawn 8 astral arrows raining (~20+ lines; new entity logic)
        } else if (is(StrikeEnchantments.VENGEANCE)) {
            // TODO: extra dmg scaled by caster missing health (needs caster; ~10 lines)
        }
    }

    public boolean is(Identifier strikeId) {
        return id.equals(strikeId);
    }

    public String tooltipNameKey() {
        return "tooltip.spellgems.spell_enchantment." + id.getPath() + ".name";
    }

    public String tooltipDescriptionKey() {
        return "tooltip.spellgems.spell_enchantment." + id.getPath() + ".description";
    }

    public Integer getTintColor() {
        if (is(StrikeEnchantments.FLAME)) return 0xFF5500;
        if (is(StrikeEnchantments.INFERNO)) return 0xCC3300;
        if (is(StrikeEnchantments.FROST)) return 0x88DDFF;
        if (is(StrikeEnchantments.FROSTBITE)) return 0x66BBDD;
        if (is(StrikeEnchantments.POISON)) return 0x339933;
        if (is(StrikeEnchantments.PLAGUE)) return 0x227722;
        if (is(StrikeEnchantments.SLOW)) return 0x5555FF;
        if (is(StrikeEnchantments.LEVITATE)) return 0xAA88FF;
        if (is(StrikeEnchantments.LIGHTNING)) return 0xFFEE77;
        if (is(StrikeEnchantments.EXPLOSION)) return 0xFFAA00;
        if (is(StrikeEnchantments.DRAIN)) return 0xCC2222;
        if (is(StrikeEnchantments.THERMAL_INVERSION)) return 0x55CCFF;
        if (is(StrikeEnchantments.PURIFY)) return 0xEEFFEE;
        if (is(StrikeEnchantments.VOLLEY)) return 0x77AAFF;
        if (is(StrikeEnchantments.VENGEANCE)) return 0xFF4444;
        return 0xCCCCCC;
    }

    public void addParticle(Level level, double x, double y, double z, RandomSource random) {
        addParticle(level, x, y, z, random, 0.0, 0.0, 0.0);
    }

    public void addParticle(Level level, double x, double y, double z, RandomSource random, double dx, double dy, double dz) {
        var randomSpread = 0.1D;
        SimpleParticleType particleType = ParticleTypes.CRIT;

        if (is(StrikeEnchantments.FROST)) {
            particleType = ParticleTypes.SNOWFLAKE;
            randomSpread = 0.4D;
        }
        else if (is(StrikeEnchantments.FROSTBITE)) {
            particleType = ParticleTypes.CRIT;
            randomSpread = 0.4D;
        }
        else if (is(StrikeEnchantments.FLAME)) {
            particleType = ParticleTypes.FLAME;
            randomSpread = 0.6D;
        }
        else if (is(StrikeEnchantments.INFERNO)) {
            particleType = ParticleTypes.LARGE_SMOKE;
            randomSpread = 0.6D;
        }
        else if (is(StrikeEnchantments.POISON)) {
            particleType = ParticleTypes.WITCH;
            randomSpread = 0.35D;
        }
        else if (is(StrikeEnchantments.PLAGUE)) {
            particleType = ParticleTypes.WITCH;
            randomSpread = 0.35D;
        }
        else if (is(StrikeEnchantments.LEVITATE)) {
            particleType = ParticleTypes.END_ROD;
            randomSpread = 0.25D;
        }
        else if (is(StrikeEnchantments.LIGHTNING)) {
            particleType = ParticleTypes.ELECTRIC_SPARK;
            randomSpread = 0.2D;
        }
        else if (is(StrikeEnchantments.EXPLOSION)) {
            particleType = ParticleTypes.EXPLOSION;
            randomSpread = 0.5D;
        }
        else if (is(StrikeEnchantments.VENGEANCE)) {
            particleType = ParticleTypes.DAMAGE_INDICATOR;
        }
        else if (is(StrikeEnchantments.PURIFY)) {
            particleType = ParticleTypes.HAPPY_VILLAGER;
            randomSpread = 0.3D;
        }
        else if (is(StrikeEnchantments.VOLLEY)) {
            particleType = ParticleTypes.ENCHANTED_HIT;
            randomSpread = 0.3D;
        }
        else if (is(StrikeEnchantments.DRAIN)) {
            particleType = ParticleTypes.ANGRY_VILLAGER;
            randomSpread = 0.3D;
        }
        else if (is(StrikeEnchantments.SLOW)) {
            particleType = ParticleTypes.BUBBLE;
            randomSpread = 0.3D;
        }
        else if (is(StrikeEnchantments.THERMAL_INVERSION)) {
            particleType = ParticleTypes.POOF;
            randomSpread = 0.3D;
        }

        level.addParticle(
                particleType,
                x + (random.nextDouble() - 0.5) * randomSpread,
                y + (random.nextDouble() - 0.5) * randomSpread,
                z + (random.nextDouble() - 0.5) * randomSpread,
                dx, dy, dz
        );
    }
}