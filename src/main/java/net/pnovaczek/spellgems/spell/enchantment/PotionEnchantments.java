package net.pnovaczek.spellgems.spell.enchantment;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import org.jspecify.annotations.Nullable;

public final class PotionEnchantments {

    private PotionEnchantments() {
    }

    public static boolean isValidCatalyst(ItemStack stack) {
        return fromCatalyst(stack) != null;
    }

    public static @Nullable PotionEnchantment fromCatalyst(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        PotionDeliveryType delivery = deliveryTypeFor(stack.getItem());
        if (delivery == null) {
            return null;
        }

        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        if (!contents.hasEffects()) {
            return null;
        }

        float durationScale = stack.getOrDefault(DataComponents.POTION_DURATION_SCALE, 1.0F);
        return new PotionEnchantment(delivery, contents, durationScale);
    }

    private static @Nullable PotionDeliveryType deliveryTypeFor(Item item) {
        if (item == Items.POTION) {
            return PotionDeliveryType.DRINK;
        }
        if (item == Items.SPLASH_POTION) {
            return PotionDeliveryType.SPLASH;
        }
        if (item == Items.LINGERING_POTION) {
            return PotionDeliveryType.LINGERING;
        }
        return null;
    }
}