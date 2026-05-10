package net.pnovaczek.spellgems.item.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record TomeData(
        Identifier enchantmentId,
        String category
) {

    public static final Codec<TomeData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("enchantment_id").forGetter(TomeData::enchantmentId),
                    Codec.STRING.fieldOf("category").forGetter(TomeData::category)
            ).apply(instance, TomeData::new)
    );

    public static TomeData create(String category) {
        return new TomeData(null, category);
    }
}
