package net.pnovaczek.spellgems.platform;

import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Loader helpers for registry types that still need platform-specific construction APIs.
 */
public interface PlatformRegistries {
    <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(
            BlockEntityFactory<? extends T> factory,
            Block... blocks
    );

    CreativeModeTab.Builder creativeTabBuilder();

    void registerEntityDataSerializer(Identifier id, EntityDataSerializer<?> serializer);

    @FunctionalInterface
    interface BlockEntityFactory<T extends BlockEntity> {
        T create(BlockPos pos, BlockState state);
    }
}
