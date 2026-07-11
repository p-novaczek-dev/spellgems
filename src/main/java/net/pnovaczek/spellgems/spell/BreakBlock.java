package net.pnovaczek.spellgems.spell;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BreakBlock extends AbstractSpell {

    @Override
    public Identifier id() {
        return SpellIds.BREAK_BLOCK;
    }

    @Override
    protected int getCooldownTicks() {
        return 10;
    }

    @Override
    protected boolean performCast(SpellContext context) {
        if (context.level().isClientSide()) {
            return false;
        }

        var caster = context.caster();
        double reach = caster instanceof Player player ? player.blockInteractionRange() : 5.0;
        BlockHitResult hit = SpellTargeting.resolveBlockHit(caster, reach);
        if (hit == null) {
            return false;
        }

        var pos = hit.getBlockPos();
        if (caster instanceof Player player && !player.isWithinBlockInteractionRange(pos, 0.0)) {
            return false;
        }

        BlockState state = context.level().getBlockState(pos);
        if (state.isAir()) {
            return false;
        }

        if (caster instanceof ServerPlayer serverPlayer) {
            serverPlayer.gameMode.destroyBlock(pos);
        } else {
            context.level().destroyBlock(pos, true, caster);
        }

        return true;
    }
}