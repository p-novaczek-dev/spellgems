package net.pnovaczek.spellgems.item.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Block inventory an item-transport gem sends items to.
 */
public record InventoryBinding(ResourceKey<Level> dimension, BlockPos pos) {
    public static final Codec<InventoryBinding> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(InventoryBinding::dimension),
            BlockPos.CODEC.fieldOf("pos").forGetter(InventoryBinding::pos)
    ).apply(instance, InventoryBinding::new));
}
