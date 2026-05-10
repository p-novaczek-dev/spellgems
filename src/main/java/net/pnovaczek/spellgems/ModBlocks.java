package net.pnovaczek.spellgems;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.pnovaczek.spellgems.block.ManaInfuserBlock;
import net.pnovaczek.spellgems.block.ManaRootCropBlock;
import net.pnovaczek.spellgems.block.SpellEnchantingTableBlock;

import java.util.function.Function;

public class ModBlocks {

    public static final CropBlock MANA_ROOT = register(
            "mana_root",
            ManaRootCropBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.CARROTS)
    );

    public static final Block MANA_INFUSER = registerBlockAndItem(
            "mana_infuser",
            ManaInfuserBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.FURNACE)
    );

    public static final Block SPELL_ENCHANTING_TABLE = registerBlockAndItem(
            "spell_enchanting_table",
            SpellEnchantingTableBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.ENCHANTING_TABLE)
    );

    private static Block registerBlockAndItem(String name, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties settings) {
        ResourceKey<Block> blockKey = keyOfBlock(name);
        Block block = blockFactory.apply(settings.setId(blockKey));

        // register item for the block
        ResourceKey<Item> itemKey = ModItems.keyOfItem(name);
        BlockItem blockItem = new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix());
        Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);

        return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
    }

    private static <T extends Block> T register(String name, java.util.function.Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties properties) {
        ResourceKey<Block> blockKey = keyOfBlock(name);
        T block = factory.apply(properties.setId(blockKey));
        return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
    }

    public static ResourceKey<Block> keyOfBlock(String name) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, name));
    }

    public static void initialize() {
        // forces static initialization
    }
}