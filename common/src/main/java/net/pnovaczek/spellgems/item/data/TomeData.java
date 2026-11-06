package net.pnovaczek.spellgems.item.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.spell.enchantment.SpellAffix;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public record TomeData(
        @Nullable Identifier enchantmentId,
        List<SpellAffix> affixes
) {

    public static final Codec<TomeData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Identifier.CODEC.optionalFieldOf("enchantment_id")
                            .forGetter(data -> Optional.ofNullable(data.enchantmentId())),
                    SpellAffix.CODEC.listOf().optionalFieldOf("affixes", List.of()).forGetter(TomeData::affixes)
            ).apply(instance, (id, affixes) -> new TomeData(id.orElse(null), affixes))
    );

    public TomeData {
        affixes = affixes == null ? List.of() : List.copyOf(affixes);
    }

    public TomeData(@Nullable Identifier enchantmentId) {
        this(enchantmentId, List.of());
    }

    public static TomeData create() {
        return new TomeData(null, List.of());
    }

    public static TomeData affix(SpellAffix affix) {
        return new TomeData(null, List.of(affix));
    }

    public static TomeData affixes(List<SpellAffix> affixes) {
        return new TomeData(null, affixes);
    }

    public boolean isEnchanted() {
        return enchantmentId != null || !affixes.isEmpty();
    }

    public String tooltipNameKey() {
        if (enchantmentId == null) {
            return "item.spellgems.spell_tome";
        }
        return "tooltip.spellgems.spell_enchantment." + enchantmentId.getPath() + ".name";
    }

    public String tooltipDescriptionKey() {
        if (enchantmentId == null) {
            return "tooltip.spellgems.spell_tome.description";
        }
        return "tooltip.spellgems.spell_enchantment." + enchantmentId.getPath() + ".description";
    }
}
