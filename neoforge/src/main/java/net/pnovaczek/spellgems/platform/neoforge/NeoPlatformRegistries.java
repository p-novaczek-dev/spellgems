package net.pnovaczek.spellgems.platform.neoforge;

import net.minecraft.core.Registry;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.pnovaczek.spellgems.platform.PlatformRegistries;
import net.pnovaczek.spellgems.registry.ModRegistry;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * NeoForge registry helpers. Named to avoid clashing with
 * {@link NeoForgeRegistries}.
 * <p>
 * Content objects are registered from {@code RegisterEvent} (see {@link NeoForgePlatform}),
 * not via immediate {@link #registerModContent}.
 */
public final class NeoPlatformRegistries implements PlatformRegistries {
    @Override
    public <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(
            BlockEntityFactory<? extends T> factory,
            Block... blocks
    ) {
        return new BlockEntityType<>(factory::create, Set.of(blocks));
    }

    @Override
    public CreativeModeTab.Builder creativeTabBuilder() {
        return CreativeModeTab.builder();
    }

    @Override
    public void registerEntityDataSerializer(Identifier id, EntityDataSerializer<?> serializer) {
        // Called while RegisterEvent is active for ENTITY_DATA_SERIALIZERS.
        ModRegistry.register(
                NeoForgeRegistries.ENTITY_DATA_SERIALIZERS,
                id.getPath(),
                serializer
        );
    }

    @Override
    public void registerModContent(Runnable registerAll) {
        // NeoForge: ModRegistries.registerFor is invoked from RegisterEvent in NeoForgePlatform.
    }

    @Override
    public @Nullable ResourceKey<? extends Registry<?>> entityDataSerializerRegistryKey() {
        return NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS;
    }
}
