package net.pnovaczek.spellgems.spell.enchantment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

/**
 * One affix entry on a combat gem or affix tome. {@code level} is 1 for affixes that do not scale.
 * A {@code kind} is not stored per entry; {@link SpellAffixes} classifies ids so later kinds can be added.
 */
public record SpellAffix(Identifier id, int level) {

    public static final Codec<SpellAffix> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(SpellAffix::id),
            Codec.INT.optionalFieldOf("level", 1).forGetter(SpellAffix::level)
    ).apply(instance, SpellAffix::new));

    public SpellAffix {
        if (level < 1) {
            level = 1;
        }
    }

    public boolean is(Identifier affixId) {
        return id.equals(affixId);
    }
}
