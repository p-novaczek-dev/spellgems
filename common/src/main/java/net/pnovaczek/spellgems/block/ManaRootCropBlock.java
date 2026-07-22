package net.pnovaczek.spellgems.block;

import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.pnovaczek.spellgems.ModItems;
import org.jspecify.annotations.NonNull;

public class ManaRootCropBlock extends CropBlock {

    public ManaRootCropBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected @NonNull ItemLike getBaseSeedId() {
        return ModItems.MANA_ROOT;
    }
}