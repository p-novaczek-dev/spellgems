package net.pnovaczek.spellgems.item.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record WandData(int selectedSlot) {

    public static final WandData DEFAULT = new WandData(0);

    public static final Codec<WandData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("selected_slot").orElse(0).forGetter(WandData::selectedSlot)
    ).apply(instance, WandData::new));
}