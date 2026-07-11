package net.pnovaczek.spellgems.spell;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.pnovaczek.spellgems.Spellgems;
import org.jspecify.annotations.Nullable;

public class Grow extends AbstractSpell {

    private static final int AREA_RADIUS = 4;

    @Override
    public Identifier id() {
        return SpellIds.GROW;
    }

    @Override
    public boolean canCast(SpellContext context) {
        if (!(context.caster() instanceof Player player)) {
            return false;
        }

        if (!hasGrowableCrop(context, player)) {
            return false;
        }

        if (!Spellgems.CONFIG.spells.grow.requireBoneMeal) {
            return true;
        }

        return HotbarUtils.hasItem(player, HotbarUtils::isBoneMeal);
    }

    @Override
    protected int getCooldownTicks() {
        return 10;
    }

    @Override
    protected boolean performCast(SpellContext context) {
        if (!(context.caster() instanceof Player player) || context.level().isClientSide()) {
            return false;
        }

        var level = context.level();
        boolean requireBoneMeal = Spellgems.CONFIG.spells.grow.requireBoneMeal;
        BlockPos center = player.blockPosition();
        boolean grewAny = false;

        for (int dx = -AREA_RADIUS; dx <= AREA_RADIUS; dx++) {
            for (int dz = -AREA_RADIUS; dz <= AREA_RADIUS; dz++) {
                BlockPos base = center.offset(dx, 0, dz);
                if (tryGrowAt(level, base, player, requireBoneMeal)) {
                    grewAny = true;
                } else if (tryGrowAt(level, base.above(), player, requireBoneMeal)) {
                    grewAny = true;
                }
            }
        }

        if (!grewAny) {
            return false;
        }

        if (requireBoneMeal && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.inventoryMenu.sendAllDataToRemote();
        }

        return true;
    }

    private static boolean hasGrowableCrop(SpellContext context, Player player) {
        BlockPos center = player.blockPosition();
        var level = context.level();

        for (int dx = -AREA_RADIUS; dx <= AREA_RADIUS; dx++) {
            for (int dz = -AREA_RADIUS; dz <= AREA_RADIUS; dz++) {
                BlockPos base = center.offset(dx, 0, dz);
                if (isGrowableTarget(level, base) || isGrowableTarget(level, base.above())) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean tryGrowAt(Level level, BlockPos pos, Player player, boolean requireBoneMeal) {
        if (!isGrowableTarget(level, pos)) {
            return false;
        }

        ItemStack boneMeal = requireBoneMeal ? HotbarUtils.pickWeighted(player, level.getRandom(), HotbarUtils::isBoneMeal) : null;
        if (requireBoneMeal && boneMeal == null) {
            return false;
        }

        return applyBoneMeal(level, pos, boneMeal, requireBoneMeal);
    }

    private static boolean isGrowableTarget(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof BonemealableBlock block
                && block.isValidBonemealTarget(level, pos, state);
    }

    private static boolean applyBoneMeal(Level level, BlockPos pos, @Nullable ItemStack boneMeal, boolean consumeBoneMeal) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BonemealableBlock block) || !block.isValidBonemealTarget(level, pos, state)) {
            return false;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return true;
        }

        if (!block.isBonemealSuccess(level, level.getRandom(), pos, state)) {
            return false;
        }

        block.performBonemeal(serverLevel, level.getRandom(), pos, state);
        level.levelEvent(1505, pos, 15);

        if (consumeBoneMeal && boneMeal != null) {
            boneMeal.shrink(1);
        }

        return true;
    }
}