package net.pnovaczek.spellgems.item.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public record TomeData(
        @Nullable Identifier enchantmentId
) {

    public static final Codec<TomeData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Identifier.CODEC.optionalFieldOf("enchantment_id")
                            .forGetter(data -> Optional.ofNullable(data.enchantmentId()))
            ).apply(instance, opt -> new TomeData(opt.orElse(null)))
    );

    public static TomeData create() {
        return new TomeData(null);
    }

    public boolean isEnchanted() {
        return enchantmentId != null;
    }

    public String tooltipNameKey() {
        return "tooltip.spellgems.spell_enchantment." + enchantmentId.getPath() + ".name";
    }

    public String tooltipDescriptionKey() {
        return "tooltip.spellgems.spell_enchantment." + enchantmentId.getPath() + ".description";
    }
}
