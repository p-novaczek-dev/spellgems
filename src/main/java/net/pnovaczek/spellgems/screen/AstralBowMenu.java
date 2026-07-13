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
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.inventory.AstralBowBackedContainer;
import net.pnovaczek.spellgems.inventory.AstralBowContainer;
import org.jspecify.annotations.Nullable;

public class AstralBowMenu extends AbstractContainerMenu {

    public static final int GEM_SLOT_START = 0;
    public static final int GEM_SLOT_COUNT = AstralBowContainer.SIZE;
    public static final int PLAYER_INVENTORY_START = GEM_SLOT_COUNT;
    public static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    public static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_END;
    public static final int PLAYER_HOTBAR_END = PLAYER_HOTBAR_START + 9;

    private static final int GEM_SLOT_X = 8;
    private static final int GEM_SLOT_Y = 36;
    private static final int GEM_SLOT_STEP = 18;

    private static final Identifier POTION_GEM_SLOT_SPRITE =
            Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "container/slot/spell_gem");

    private final Container bowSlots;
    private final @Nullable InteractionHand bowHand;

    @SuppressWarnings("this-escape")
    public AstralBowMenu(int containerId, Inventory playerInventory, InteractionHand hand) {
        super(ModMenuTypes.ASTRAL_BOW, containerId);
        this.bowHand = hand;
        this.bowSlots = new AstralBowBackedContainer(playerInventory.player, hand);
        addBowSlots();
        addPlayerInventory(playerInventory);
    }

    @SuppressWarnings("this-escape")
    public AstralBowMenu(int containerId, Inventory playerInventory) {
        super(ModMenuTypes.ASTRAL_BOW, containerId);
        this.bowHand = null;
        this.bowSlots = new SimpleContainer(AstralBowContainer.SIZE);
        addBowSlots();
        addPlayerInventory(playerInventory);
    }

    @SuppressWarnings("this-escape")
    private void addBowSlots() {
        for (int i = 0; i < GEM_SLOT_COUNT; i++) {
            int x = GEM_SLOT_X + i * GEM_SLOT_STEP;
            this.addSlot(new PotionGemSlot(this.bowSlots, i, x, GEM_SLOT_Y));
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
        if (this.bowHand == null) {
            return true;
        }
        return player.getItemInHand(this.bowHand).is(ModItems.ASTRAL_BOW);
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
        } else if (isPotionGem(stack)) {
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
        if (this.bowHand != null && this.bowSlots instanceof AstralBowBackedContainer) {
            ItemStack bow = player.getItemInHand(this.bowHand);
            if (bow.is(ModItems.ASTRAL_BOW)) {
                AstralBowContainer.saveFrom(this.bowSlots, bow);
            }
        }
    }

    private static boolean isPotionGem(ItemStack stack) {
        return stack.is(ModItems.SPELL_GEM_POTION);
    }

    private static class PotionGemSlot extends Slot {

        PotionGemSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return isPotionGem(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public Identifier getNoItemIcon() {
            return POTION_GEM_SLOT_SPRITE;
        }
    }
}