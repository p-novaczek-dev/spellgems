package net.pnovaczek.spellgems.platform.fabric;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityDataRegistry;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.pnovaczek.spellgems.platform.PlatformRegistries;

public final class FabricRegistries implements PlatformRegistries {
    @Override
    public <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(
            BlockEntityFactory<? extends T> factory,
            Block... blocks
    ) {
        return FabricBlockEntityTypeBuilder.<T>create(factory::create, blocks).build();
    }

    @Override
    public CreativeModeTab.Builder creativeTabBuilder() {
        return FabricCreativeModeTab.builder();
    }

    @Override
    public void registerEntityDataSerializer(Identifier id, EntityDataSerializer<?> serializer) {
        FabricEntityDataRegistry.register(id, serializer);
    }
}
