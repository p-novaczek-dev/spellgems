package net.pnovaczek.spellgems.spell;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class PlaceBlock extends AbstractSpell {

    private static final double DEFAULT_REACH = 5.0;

    @Override
    public Identifier id() {
        return SpellIds.PLACE_BLOCK;
    }

    @Override
    public boolean canCast(SpellContext context) {
        return HotbarUtils.hasItem(context, HotbarUtils::isPlaceableBlock);
    }

    @Override
    public boolean repeatWhileHeld() {
        return false;
    }

    @Override
    protected boolean performCast(SpellContext context) {
        if (context.level().isClientSide()) {
            return false;
        }

        double reach = context.caster() instanceof Player player
                ? player.blockInteractionRange()
                : DEFAULT_REACH;
        BlockHitResult hit = SpellTargeting.resolveBlockHit(context, reach);
        if (hit == null) {
            return false;
        }

        if (context.caster() instanceof Player player
                && !player.isWithinBlockInteractionRange(hit.getBlockPos(), 0.0)) {
            return false;
        }

        ItemStack stack = HotbarUtils.pickWeighted(context, context.level().getRandom(), HotbarUtils::isPlaceableBlock);
        if (stack == null || !(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }

        boolean placed;
        if (context.caster() instanceof Player player) {
            UseOnContext useContext = new UseOnContext(
                    context.level(), player, InteractionHand.MAIN_HAND, stack, hit);
            InteractionResult result = blockItem.place(new BlockPlaceContext(useContext));
            placed = result.consumesAction();
        } else {
            placed = placeWithoutPlayer(context.level(), blockItem, stack, hit);
        }

        if (!placed) {
            return false;
        }

        if (context.caster() instanceof ServerPlayer serverPlayer) {
            serverPlayer.inventoryMenu.sendAllDataToRemote();
        }

        return true;
    }

    /**
     * Direct placement for non-player casters (spell dispenser). Places against the hit face.
     */
    private static boolean placeWithoutPlayer(Level level, BlockItem blockItem, ItemStack stack, BlockHitResult hit) {
        Direction face = hit.getDirection();
        BlockPos placePos = hit.getBlockPos().relative(face);
        if (!level.getBlockState(placePos).canBeReplaced()) {
            return false;
        }

        Block block = blockItem.getBlock();
        BlockState state = block.defaultBlockState();
        if (!state.canSurvive(level, placePos)) {
            return false;
        }
        if (!level.setBlock(placePos, state, Block.UPDATE_ALL)) {
            return false;
        }

        level.gameEvent(GameEvent.BLOCK_PLACE, placePos, GameEvent.Context.of(state));
        stack.shrink(1);
        return true;
    }
}
