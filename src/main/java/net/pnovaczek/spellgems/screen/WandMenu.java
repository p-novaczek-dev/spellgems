package net.pnovaczek.spellgems.screen;

import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.pnovaczek.spellgems.ModItems;
import net.pnovaczek.spellgems.ModMenuTypes;
import net.pnovaczek.spellgems.ModTags;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.inventory.WandBackedContainer;
import net.pnovaczek.spellgems.inventory.WandContainer;
import org.jspecify.annotations.Nullable;

public class WandMenu extends AbstractContainerMenu {

    public static final int GEM_SLOT_START = 0;
    public static final int GEM_SLOT_COUNT = WandContainer.SIZE;
    public static final int PLAYER_INVENTORY_START = GEM_SLOT_COUNT;
    public static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    public static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_END;
    public static final int PLAYER_HOTBAR_END = PLAYER_HOTBAR_START + 9;

    private static final int GEM_SLOT_X = 8;
    private static final int GEM_SLOT_Y = 36;
    private static final int GEM_SLOT_STEP = 18;

    private static final Identifier SPELL_GEM_SLOT_SPRITE =
            Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "container/slot/spell_gem");

    private final Container wandSlots;
    private final @Nullable InteractionHand wandHand;

    public WandMenu(int containerId, Inventory playerInventory, InteractionHand hand) {
        super(ModMenuTypes.WAND, containerId);
        this.wandHand = hand;
        this.wandSlots = new WandBackedContainer(playerInventory.player, hand);
        addWandSlots();
        addPlayerInventory(playerInventory);
    }

    public WandMenu(int containerId, Inventory playerInventory) {
        super(ModMenuTypes.WAND, containerId);
        this.wandHand = null;
        this.wandSlots = new SimpleContainer(WandContainer.SIZE);
        addWandSlots();
        addPlayerInventory(playerInventory);
    }

    private void addWandSlots() {
        for (int i = 0; i < GEM_SLOT_COUNT; i++) {
            int x = GEM_SLOT_X + i * GEM_SLOT_STEP;
            this.addSlot(new SpellGemSlot(this.wandSlots, i, x, GEM_SLOT_Y));
        }
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        8 + col * 18,
                        84 + row * 18
                ));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.wandHand == null) {
            return true;
        }
        return player.getItemInHand(this.wandHand).is(ModItems.WAND);
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

        if (slotIndex < GEM_SLOT_COUNT) {
            if (!this.moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (isSpellGem(stack)) {
            if (!this.moveItemStackTo(stack, GEM_SLOT_START, GEM_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
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

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (this.wandHand != null && this.wandSlots instanceof WandBackedContainer) {
            ItemStack wand = player.getItemInHand(this.wandHand);
            if (wand.is(ModItems.WAND)) {
                WandContainer.saveFrom(this.wandSlots, wand);
            }
        }
    }

    private static boolean isSpellGem(ItemStack stack) {
        return stack.is(ModTags.COMBAT_SPELL_GEMS) || stack.is(ModTags.UTILITY_SPELL_GEMS);
    }

    private static class SpellGemSlot extends Slot {

        SpellGemSlot(net.minecraft.world.Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return isSpellGem(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public Identifier getNoItemIcon() {
            return SPELL_GEM_SLOT_SPRITE;
        }
    }
}