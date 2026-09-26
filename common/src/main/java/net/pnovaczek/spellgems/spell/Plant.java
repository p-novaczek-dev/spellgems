package net.pnovaczek.spellgems.spell;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.pnovaczek.spellgems.Spellgems;

public class Plant extends AbstractSpell {

    @Override
    public Identifier id() {
        return SpellIds.PLANT;
    }

    @Override
    public boolean canCast(SpellContext context) {
        return HotbarUtils.hasItem(context, HotbarUtils::isPlantableSeed);
    }

    @Override
    protected boolean performCast(SpellContext context) {
        if (context.level().isClientSide()) {
            return false;
        }

        var level = context.level();
        BlockPos center = context.originBlockPos();
        boolean plantedAny = false;
        int areaRadius = Spellgems.CONFIG.spells.plant.areaRadius;

        for (int dx = -areaRadius; dx <= areaRadius; dx++) {
            for (int dz = -areaRadius; dz <= areaRadius; dz++) {
                for (int dy = -2; dy <= 2; dy++) {
                    BlockPos farmlandPos = center.offset(dx, dy, dz);
                    if (!(level.getBlockState(farmlandPos).getBlock() instanceof FarmlandBlock)) {
                        continue;
                    }

                    BlockPos plantPos = farmlandPos.above();
                    if (!level.getBlockState(plantPos).isAir()) {
                        continue;
                    }

                    ItemStack seedStack = HotbarUtils.pickWeighted(context, level.getRandom(), HotbarUtils::isPlantableSeed);
                    if (seedStack == null || !(seedStack.getItem() instanceof BlockItem blockItem)) {
                        break;
                    }

                    BlockState cropState = blockItem.getBlock().defaultBlockState();
                    level.setBlockAndUpdate(plantPos, cropState);
                    level.gameEvent(GameEvent.BLOCK_PLACE, plantPos, GameEvent.Context.of(cropState));
                    level.playSound(
                            null,
                            plantPos.getX(),
                            plantPos.getY(),
                            plantPos.getZ(),
                            SoundEvents.CROP_PLANTED,
                            SoundSource.BLOCKS,
                            1.0F,
                            1.0F
                    );
                    seedStack.shrink(1);
                    plantedAny = true;
                }
            }
        }

        if (!plantedAny) {
            return false;
        }

        if (context.caster() instanceof ServerPlayer serverPlayer) {
            serverPlayer.inventoryMenu.sendAllDataToRemote();
        }

        return true;
    }
}
