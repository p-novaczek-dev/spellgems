package net.pnovaczek.spellgems.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public final class WandContainer {

    public static final int SIZE = 9;

    private WandContainer() {
    }

    public static void loadInto(SimpleContainer container, ItemStack wand) {
        ItemContainerContents contents = wand.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        contents.copyInto(items);
        for (int i = 0; i < SIZE; i++) {
            container.setItem(i, items.get(i));
        }
    }

    public static void loadInto(Container container, ItemStack wand) {
        ItemContainerContents contents = wand.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        contents.copyInto(items);
        for (int i = 0; i < SIZE; i++) {
            container.setItem(i, items.get(i));
        }
    }

    public static void saveFrom(Container container, ItemStack wand) {
        NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        for (int i = 0; i < SIZE; i++) {
            items.set(i, container.getItem(i));
        }
        wand.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
        wand.set(DataComponents.TOOLTIP_DISPLAY,
                TooltipDisplay.DEFAULT.withHidden(DataComponents.CONTAINER, true));
    }
}