package net.pnovaczek.spellgems.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.pnovaczek.spellgems.ModComponents;
import net.pnovaczek.spellgems.item.data.TomeData;
import org.jspecify.annotations.NonNull;

public class SpellTomeItem extends Item {

    public SpellTomeItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull Component getName(@NonNull ItemStack stack) {
        MutableComponent name = super.getName(stack).copy();
        TomeData data = getTomeData(stack);
        if (data != null && data.isEnchanted()) {
            name.withStyle(ChatFormatting.AQUA);
        }
        return name;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        TomeData data = getTomeData(stack);
        return data != null && data.isEnchanted();
    }

    public static TomeData getTomeData(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        return stack.get(ModComponents.TOME_DATA);
    }
}