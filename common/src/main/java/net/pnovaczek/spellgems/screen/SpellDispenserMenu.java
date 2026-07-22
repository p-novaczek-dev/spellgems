package net.pnovaczek.spellgems.screen;

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
import net.pnovaczek.spellgems.ModMenuTypes;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.block.entity.SpellDispenserBlockEntity;

public class SpellDispenserMenu extends AbstractContainerMenu {

    private static final Identifier EMPTY_SLOT_SPELL_GEM =
            Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "container/slot/spell_gem");

    private final Container container;
    private final ContainerData data;

    @SuppressWarnings("this-escape")
    public SpellDispenserMenu(int syncId, Inventory playerInventory, Container container, ContainerData data) {
        super(ModMenuTypes.SPELL_DISPENSER, syncId);
        this.container = container;
        this.data = data;

        // Spell gem slot
        this.addSlot(new SpellGemSlot(container, SpellDispenserBlockEntity.SLOT_SPELL_GEM, 26, 35));

        // 3x3 material inventory: start left 62, top 17, padding 2 → step 18
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int index = SpellDispenserBlockEntity.FIRST_MATERIAL_SLOT + row * 3 + col;
                this.addSlot(new Slot(container, index, 62 + col * 18, 17 + row * 18));
            }
        }

        // Player inventory + hotbar
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }

        this.addDataSlots(data);
    }

    public SpellDispenserMenu(int syncId, Inventory playerInventory) {
        this(
                syncId,
                playerInventory,
                new SimpleContainer(SpellDispenserBlockEntity.CONTAINER_SIZE),
                new SimpleContainerData(4)
        );
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack moved = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        moved = stack.copy();

        final int machineEnd = SpellDispenserBlockEntity.CONTAINER_SIZE; // 10
        final int playerStart = machineEnd;
        final int playerEnd = playerStart + 36;

        if (slotIndex < machineEnd) {
            // Machine → player
            if (!this.moveItemStackTo(stack, playerStart, playerEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // Player → machine
            if (SpellDispenserBlockEntity.isSpellGem(stack)) {
                if (!this.moveItemStackTo(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 1, machineEnd, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == moved.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return moved;
    }

    public int getCooldownRemaining() {
        return data.get(0);
    }

    public int getCooldownMax() {
        return data.get(1);
    }

    public int getBurnoutRemaining() {
        return data.get(2);
    }

    public int getBurnoutMax() {
        return data.get(3);
    }

    public boolean isBurnedOut() {
        return getBurnoutRemaining() > 0;
    }

    private static class SpellGemSlot extends Slot {
        SpellGemSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return SpellDispenserBlockEntity.isSpellGem(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public Identifier getNoItemIcon() {
            return EMPTY_SLOT_SPELL_GEM;
        }
    }
}
