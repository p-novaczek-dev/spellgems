package net.pnovaczek.spellgems.inventory;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.pnovaczek.spellgems.ModItems;

public class WandBackedContainer extends SimpleContainer {

    private final Player player;
    private final InteractionHand hand;

    @SuppressWarnings("this-escape")
    public WandBackedContainer(Player player, InteractionHand hand) {
        super(WandContainer.SIZE);
        this.player = player;
        this.hand = hand;
        WandContainer.loadInto(this, player.getItemInHand(hand));
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.player.level().isClientSide()) {
            return;
        }
        ItemStack wand = this.player.getItemInHand(this.hand);
        if (wand.is(ModItems.WAND)) {
            WandContainer.saveFrom(this, wand);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getItemInHand(this.hand).is(ModItems.WAND);
    }
}