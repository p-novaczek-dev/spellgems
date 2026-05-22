package net.pnovaczek.spellgems.screen;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.pnovaczek.spellgems.ModItems;
import net.pnovaczek.spellgems.ModMenuTypes;
import net.pnovaczek.spellgems.block.entity.ManaInfuserBlockEntity;

public class ManaInfuserMenu extends AbstractContainerMenu {

    private final Container container;
    private final ContainerData data;

    private static final Identifier EMPTY_SLOT_FUEL = Identifier.withDefaultNamespace("container/slot/brewing_fuel");

    // Server constructor
    public ManaInfuserMenu(int syncId, Inventory playerInventory, Container container, ContainerData manaInfuserData) {
        super(ModMenuTypes.MANA_INFUSER, syncId);
        this.container = container;
        this.data = manaInfuserData;

        this.addSlot(new FuelSlot(container, 0, 26, 35));      // fuel
        this.addSlot(new Slot(container, 1, 56, 17));          // infusing ingredient
        this.addSlot(new Slot(container, 2, 56, 53));          // item to infuse
        this.addSlot(new Slot(container, 3, 116, 35) {         // output
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });

        // Player inventory + hotbar
        for (int i = 0; i < 3; ++i)
            for (int j = 0; j < 9; ++j)
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
        for (int i = 0; i < 9; ++i)
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));

        this.addDataSlots(data);
    }

    // Client constructor
    public ManaInfuserMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(ManaInfuserBlockEntity.CONTAINER_SIZE), new SimpleContainerData(2));
    }

    private static ManaInfuserBlockEntity getBlockEntity(Inventory playerInventory, BlockPos pos) {
        Level level = playerInventory.player.level();
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ManaInfuserBlockEntity m) return m;
        throw new IllegalStateException("Block entity is not a ManaInfuserBlockEntity at " + pos);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack moved = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            moved = stack.copy();

            // Output slot → player inventory
            if (slotIndex == 3) {
                if (!this.moveItemStackTo(stack, 4, 40, true)) return ItemStack.EMPTY;
                slot.onQuickCraft(stack, moved);
            }
            // Player inventory → correct machine slot
            else if (slotIndex >= 4) {
                if (FuelSlot.mayPlaceItem(stack)) {
                    if (!this.moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
                } else if (!this.moveItemStackTo(stack, 1, 3, false)) return ItemStack.EMPTY;
            }
            // Machine slot → player inventory
            else if (!this.moveItemStackTo(stack, 4, 40, false)) return ItemStack.EMPTY;

            if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();

            if (stack.getCount() == moved.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, moved);
        }
        return moved;
    }

    public int getProgress() { return data.get(0); }
    public int getManaLevel() { return data.get(1); }

    private static class FuelSlot extends Slot {
        public FuelSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return mayPlaceItem(stack);
        }

        public static boolean mayPlaceItem(ItemStack stack) {
            return stack.is(ModItems.MANA_ESSENCE);
        }

        @Override
        public Identifier getNoItemIcon() {
            return EMPTY_SLOT_FUEL;
        }
    }
}