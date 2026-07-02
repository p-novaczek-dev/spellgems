package net.pnovaczek.spellgems.item.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record AstralBowData(int selectedSlot) {

    public static final AstralBowData DEFAULT = new AstralBowData(0);

    public static final Codec<AstralBowData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("selected_slot").orElse(0).forGetter(AstralBowData::selectedSlot)
    ).apply(instance, AstralBowData::new));
}