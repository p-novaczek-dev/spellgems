package net.pnovaczek.spellgems.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Loader helpers for registry construction and content registration lifecycle.
 */
public interface PlatformRegistries {
    <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(
            BlockEntityFactory<? extends T> factory,
            Block... blocks
    );

    CreativeModeTab.Builder creativeTabBuilder();

    void registerEntityDataSerializer(Identifier id, EntityDataSerializer<?> serializer);

    /**
     * Fabric: runs {@code registerAll} immediately (BuiltInRegistries open at mod init).
     * NeoForge: no-ops — content is registered from {@code RegisterEvent} via
     * {@link net.pnovaczek.spellgems.registry.ModRegistries#registerFor}.
     */
    void registerModContent(Runnable registerAll);

    /**
     * NeoForge entity-data-serializer registry key, or {@code null} when serializers
     * are registered outside vanilla {@link RegisterEvent} (Fabric).
     */
    @Nullable
    ResourceKey<? extends Registry<?>> entityDataSerializerRegistryKey();

    @FunctionalInterface
    interface BlockEntityFactory<T extends BlockEntity> {
        T create(BlockPos pos, BlockState state);
    }
}
