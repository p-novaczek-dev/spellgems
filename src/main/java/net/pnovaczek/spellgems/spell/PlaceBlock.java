package net.pnovaczek.spellgems.spell;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;

public class PlaceBlock extends AbstractSpell {

    @Override
    public Identifier id() {
        return SpellIds.PLACE_BLOCK;
    }

    @Override
    protected int getCooldownTicks() {
        return 10;
    }

    @Override
    public boolean canCast(SpellContext context) {
        return context.caster() instanceof Player player && HotbarUtils.hasItem(player, HotbarUtils::isPlaceableBlock);
    }

    @Override
    protected boolean performCast(SpellContext context) {
        if (!(context.caster() instanceof Player player) || context.level().isClientSide()) {
            return false;
        }

        BlockHitResult hit = SpellTargeting.resolveBlockHit(player, player.blockInteractionRange());
        if (hit == null || !player.isWithinBlockInteractionRange(hit.getBlockPos(), 0.0)) {
            return false;
        }

        ItemStack stack = HotbarUtils.pickWeighted(player, context.level().getRandom(), HotbarUtils::isPlaceableBlock);
        if (stack == null || !(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }

        UseOnContext useContext = new UseOnContext(context.level(), player, InteractionHand.MAIN_HAND, stack, hit);
        InteractionResult result = blockItem.place(new BlockPlaceContext(useContext));
        if (!result.consumesAction()) {
            return false;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.inventoryMenu.sendAllDataToRemote();
        }

        return true;
    }
}