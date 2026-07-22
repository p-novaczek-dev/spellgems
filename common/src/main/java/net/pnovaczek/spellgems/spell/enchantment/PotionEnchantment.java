package net.pnovaczek.spellgems.spell.enchantment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;

public record PotionEnchantment(
        PotionDeliveryType delivery,
        PotionContents contents,
        float durationScale
) {

    public static final Codec<PotionEnchantment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PotionDeliveryType.CODEC.fieldOf("delivery").forGetter(PotionEnchantment::delivery),
            PotionContents.CODEC.fieldOf("contents").forGetter(PotionEnchantment::contents),
            Codec.FLOAT.optionalFieldOf("duration_scale", 1.0F).forGetter(PotionEnchantment::durationScale)
    ).apply(instance, PotionEnchantment::new));

    public ItemStack toItemStack() {
        ItemStack stack = new ItemStack(delivery.baseItem());
        stack.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS, contents);
        if (durationScale != 1.0F) {
            stack.set(net.minecraft.core.component.DataComponents.POTION_DURATION_SCALE, durationScale);
        }
        return stack;
    }

    public Component displayName() {
        return contents.getName(switch (delivery) {
            case DRINK -> "item.minecraft.potion.effect.";
            case SPLASH -> "item.minecraft.splash_potion.effect.";
            case LINGERING -> "item.minecraft.lingering_potion.effect.";
        });
    }
}