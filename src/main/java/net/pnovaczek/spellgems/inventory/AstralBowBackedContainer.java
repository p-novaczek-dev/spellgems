package net.pnovaczek.spellgems.inventory;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.pnovaczek.spellgems.ModItems;

public class AstralBowBackedContainer extends SimpleContainer {

    private final Player player;
    private final InteractionHand hand;

    public AstralBowBackedContainer(Player player, InteractionHand hand) {
        super(AstralBowContainer.SIZE);
        this.player = player;
        this.hand = hand;
        AstralBowContainer.loadInto(this, player.getItemInHand(hand));
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.player.level().isClientSide()) {
            return;
        }
        ItemStack bow = this.player.getItemInHand(this.hand);
        if (bow.is(ModItems.ASTRAL_BOW)) {
            AstralBowContainer.saveFrom(this, bow);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getItemInHand(this.hand).is(ModItems.ASTRAL_BOW);
    }
}