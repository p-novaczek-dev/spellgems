package net.pnovaczek.spellgems;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.pnovaczek.spellgems.block.entity.ManaInfuserBlockEntity;
import net.pnovaczek.spellgems.block.entity.SpellDispenserBlockEntity;
import net.pnovaczek.spellgems.block.entity.SpellEnchantingTableBlockEntity;
import net.pnovaczek.spellgems.platform.Platform;
import net.pnovaczek.spellgems.platform.PlatformRegistries;
import net.pnovaczek.spellgems.registry.ModRegistry;

/**
 * Block entity types. Requires {@link ModBlocks} first.
 * Fields are assigned in {@link #register()} (not class-init).
 */
public class ModBlockEntities {

    public static BlockEntityType<ManaInfuserBlockEntity> MANA_INFUSER;
    public static BlockEntityType<SpellEnchantingTableBlockEntity> SPELL_ENCHANTING_TABLE;
    public static BlockEntityType<SpellDispenserBlockEntity> SPELL_DISPENSER;

    private ModBlockEntities() {
    }

    public static void register() {
        MANA_INFUSER = register(
                "mana_infuser",
                ManaInfuserBlockEntity::new,
                ModBlocks.MANA_INFUSER
        );
        SPELL_ENCHANTING_TABLE = register(
                "spell_enchanting_table",
                SpellEnchantingTableBlockEntity::new,
                ModBlocks.SPELL_ENCHANTING_TABLE
        );
        SPELL_DISPENSER = register(
                "spell_dispenser",
                SpellDispenserBlockEntity::new,
                ModBlocks.SPELL_DISPENSER
        );
    }

    private static <T extends BlockEntity> BlockEntityType<T> register(
            String name,
            PlatformRegistries.BlockEntityFactory<? extends T> entityFactory,
            Block... blocks
    ) {
        BlockEntityType<T> type = Platform.registries().createBlockEntityType(entityFactory, blocks);
        return ModRegistry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, name, type);
    }
}
