package net.pnovaczek.spellgems;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.pnovaczek.spellgems.block.entity.ManaInfuserBlockEntity;
import net.pnovaczek.spellgems.block.entity.SpellDispenserBlockEntity;
import net.pnovaczek.spellgems.block.entity.SpellEnchantingTableBlockEntity;

public class ModBlockEntities {

    public static final BlockEntityType<ManaInfuserBlockEntity> MANA_INFUSER = register(
            "mana_infuser",
            ManaInfuserBlockEntity::new,
            ModBlocks.MANA_INFUSER
    );

    public static final BlockEntityType<SpellEnchantingTableBlockEntity> SPELL_ENCHANTING_TABLE = register(
            "spell_enchanting_table",
            SpellEnchantingTableBlockEntity::new,
            ModBlocks.SPELL_ENCHANTING_TABLE
    );

    public static final BlockEntityType<SpellDispenserBlockEntity> SPELL_DISPENSER = register(
            "spell_dispenser",
            SpellDispenserBlockEntity::new,
            ModBlocks.SPELL_DISPENSER
    );

    private static <T extends BlockEntity> BlockEntityType<T> register(
            String name,
            FabricBlockEntityTypeBuilder.Factory<? extends T> entityFactory,
            Block... blocks
    ) {
        Identifier id = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, name);
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, FabricBlockEntityTypeBuilder.<T>create(entityFactory, blocks).build());
    }

    public static void initialize() {
        // forces static init
    }
}