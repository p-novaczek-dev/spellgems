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

    private static final int COOLDOWN_TICKS = 10;

    @Override
    public Identifier id() {
        return Spells.PLACE_BLOCK;
    }

    @Override
    public boolean canCast(SpellContext context) {
        return context.caster() instanceof Player player && HotbarBlocks.hasPlaceableBlock(player);
    }

    @Override
    public void cast(SpellContext context) {
        if (!(context.caster() instanceof Player player) || context.level().isClientSide()) {
            return;
        }

        BlockHitResult hit = SpellTargeting.resolveBlockHit(player, player.blockInteractionRange());
        if (hit == null || !player.isWithinBlockInteractionRange(hit.getBlockPos(), 0.0)) {
            return;
        }

        ItemStack stack = HotbarBlocks.pickWeightedPlaceableBlock(player, context.level().getRandom());
        if (stack == null || !(stack.getItem() instanceof BlockItem blockItem)) {
            return;
        }

        UseOnContext useContext = new UseOnContext(context.level(), player, InteractionHand.MAIN_HAND, stack, hit);
        InteractionResult result = blockItem.place(new BlockPlaceContext(useContext));
        if (!result.consumesAction()) {
            return;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.inventoryMenu.sendAllDataToRemote();
        }

        applyCastCooldown(context, COOLDOWN_TICKS);
    }
}