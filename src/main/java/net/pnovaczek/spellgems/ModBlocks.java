package net.pnovaczek.spellgems;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.pnovaczek.spellgems.block.ManaInfuserBlock;
import net.pnovaczek.spellgems.block.ManaRootCropBlock;
import net.pnovaczek.spellgems.block.SpellDispenserBlock;
import net.pnovaczek.spellgems.block.SpellEnchantingTableBlock;
import net.pnovaczek.spellgems.registry.ModRegistry;

import java.util.function.Function;

/**
 * Blocks only. Block items are registered in {@link ModItems} after blocks exist.
 * Fields are assigned in {@link #register()} (not class-init).
 */
public class ModBlocks {

    public static CropBlock MANA_ROOT;
    public static Block MANA_INFUSER;
    public static Block SPELL_ENCHANTING_TABLE;
    public static Block SPELL_DISPENSER;

    private ModBlocks() {
    }

    public static void register() {
        MANA_ROOT = register(
                "mana_root",
                ManaRootCropBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.CARROTS)
        );
        MANA_INFUSER = register(
                "mana_infuser",
                ManaInfuserBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.FURNACE)
        );
        SPELL_ENCHANTING_TABLE = register(
                "spell_enchanting_table",
                SpellEnchantingTableBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.ENCHANTING_TABLE)
        );
        SPELL_DISPENSER = register(
                "spell_dispenser",
                SpellDispenserBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.DISPENSER)
        );
    }

    private static <T extends Block> T register(
            String name,
            Function<BlockBehaviour.Properties, T> factory,
            BlockBehaviour.Properties properties
    ) {
        ResourceKey<Block> blockKey = keyOfBlock(name);
        T block = factory.apply(properties.setId(blockKey));
        return ModRegistry.register(BuiltInRegistries.BLOCK, blockKey, block);
    }

    public static ResourceKey<Block> keyOfBlock(String name) {
        return ResourceKey.create(Registries.BLOCK, ModRegistry.id(name));
    }
}
