package net.pnovaczek.spellgems.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.pnovaczek.spellgems.ModBlockEntities;
import net.pnovaczek.spellgems.block.entity.SpellEnchantingTableBlockEntity;
import net.pnovaczek.spellgems.screen.SpellEnchantingMenu;
import org.jspecify.annotations.Nullable;

public class SpellEnchantingTableBlock extends BaseEntityBlock {
    public static final MapCodec<SpellEnchantingTableBlock> CODEC = simpleCodec(SpellEnchantingTableBlock::new);
    private static final VoxelShape SHAPE = Block.column(16.0, 0.0, 12.0);

    public SpellEnchantingTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<SpellEnchantingTableBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpellEnchantingTableBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide()
                ? createTickerHelper(type, ModBlockEntities.SPELL_ENCHANTING_TABLE, SpellEnchantingTableBlockEntity::clientTick)
                : null;
    }

    @Override
    protected InteractionResult useWithoutItem(
            final BlockState state, final Level level, final BlockPos pos, final Player player, final BlockHitResult hitResult
    ) {
        if (!level.isClientSide()) {
            player.openMenu(state.getMenuProvider(level, pos));
        }

        return InteractionResult.SUCCESS;
    }


    @Override
    protected @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof SpellEnchantingTableBlockEntity blockEntity) {
            Component title = blockEntity.getDisplayName();
            return new SimpleMenuProvider(
                    (containerId, inventory, p) -> new SpellEnchantingMenu(containerId, inventory, ContainerLevelAccess.create(level, pos)),
                    title
            );
        }
        return null;
    }
}
