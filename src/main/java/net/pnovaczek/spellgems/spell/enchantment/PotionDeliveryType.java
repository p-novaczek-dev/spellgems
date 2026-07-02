package net.pnovaczek.spellgems.spell.enchantment;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum PotionDeliveryType implements StringRepresentable {
    DRINK("drink"),
    SPLASH("splash"),
    LINGERING("lingering");

    public static final Codec<PotionDeliveryType> CODEC = StringRepresentable.fromEnum(PotionDeliveryType::values);

    private final String name;

    PotionDeliveryType(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public Item baseItem() {
        return switch (this) {
            case DRINK -> Items.POTION;
            case SPLASH -> Items.SPLASH_POTION;
            case LINGERING -> Items.LINGERING_POTION;
        };
    }
}