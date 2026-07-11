package net.pnovaczek.spellgems.spell;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

public class Harvest extends AbstractSpell {

    private static final int COOLDOWN_TICKS = 10;
    private static final int AREA_RADIUS = 4;

    @Override
    public Identifier id() {
        return Spells.HARVEST;
    }

    @Override
    public void cast(SpellContext context) {
        if (!(context.caster() instanceof Player player) || context.level().isClientSide()) {
            return;
        }

        var level = context.level();
        BlockPos center = player.blockPosition();
        boolean harvestedAny = false;

        for (int dx = -AREA_RADIUS; dx <= AREA_RADIUS; dx++) {
            for (int dz = -AREA_RADIUS; dz <= AREA_RADIUS; dz++) {
                BlockPos base = center.offset(dx, 0, dz);
                if (tryHarvestCrop(level, base, player) || tryHarvestCrop(level, base.above(), player)) {
                    harvestedAny = true;
                }
            }
        }

        if (harvestedAny) {
            applyCastCooldown(context, COOLDOWN_TICKS);
        }
    }

    private static boolean tryHarvestCrop(Level level, BlockPos pos, Player player) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof CropBlock crop) || !crop.isMaxAge(state)) {
            return false;
        }

        return level.destroyBlock(pos, true, player);
    }
}