package net.pnovaczek.spellgems.spell.enchantment;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * Thin id-holder for strike enchantments stored on spell gems.
 * Behavior lives in {@link StrikeEffects} strategies.
 */
public record StrikeEnchantment(Identifier id) {

    public static final Codec<StrikeEnchantment> CODEC = Identifier.CODEC.xmap(
            StrikeEnchantment::new,
            StrikeEnchantment::id
    );

    public void applyTo(LivingEntity living, LivingEntity caster) {
        effect().apply(living, caster);
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
        return effect().tintColor();
    }

    public void addParticle(Level level, double x, double y, double z, RandomSource random) {
        addParticle(level, x, y, z, random, 0.0, 0.0, 0.0);
    }

    public void addParticle(Level level, double x, double y, double z, RandomSource random, double dx, double dy, double dz) {
        effect().addParticle(level, x, y, z, random, dx, dy, dz);
    }

    private StrikeEffect effect() {
        return StrikeEffects.get(id);
    }
}
