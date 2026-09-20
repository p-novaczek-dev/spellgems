package net.pnovaczek.spellgems.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.pnovaczek.spellgems.Spellgems;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Translucent barrier with two modes. Default: players pass, mobs collide.
 * Reversed: mobs pass, players collide. A redstone pulse flips this block and
 * every face-adjacent astral barrier in the same connected group.
 */
public class AstralBarrierBlock extends TransparentBlock {
    public static final MapCodec<AstralBarrierBlock> CODEC = simpleCodec(AstralBarrierBlock::new);
    public static final BooleanProperty REVERSED = BooleanProperty.create("reversed");
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private static long flipGameTime = Long.MIN_VALUE;
    private static final Set<BlockPos> flippedThisTick = new HashSet<>();

    @SuppressWarnings("this-escape")
    public AstralBarrierBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(REVERSED, false)
                .setValue(POWERED, false));
    }

    @Override
    public MapCodec<? extends AstralBarrierBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(REVERSED, POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState neighborState, Direction direction) {
        return neighborState.is(this)
                && neighborState.getValue(REVERSED) == state.getValue(REVERSED);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return false;
    }

    @Override
    protected int getLightDampening(BlockState state) {
        return 15;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return canPass(state, context) ? Shapes.empty() : Shapes.block();
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.block();
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        // Default: mobs treat as blocked. Reversed: mobs can path through.
        return state.getValue(REVERSED);
    }

    @Override
    protected void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            Block block,
            @Nullable Orientation orientation,
            boolean movedByPiston
    ) {
        boolean powered = level.hasNeighborSignal(pos);
        if (powered == state.getValue(POWERED)) {
            return;
        }
        level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_CLIENTS);
        if (powered && !level.isClientSide()) {
            flipCompound(level, pos);
        }
    }

    private void flipCompound(Level level, BlockPos origin) {
        long gameTime = level.getGameTime();
        if (gameTime != flipGameTime) {
            flipGameTime = gameTime;
            flippedThisTick.clear();
        }
        if (flippedThisTick.contains(origin)) {
            return;
        }

        List<BlockPos> compound = collectCompound(level, origin);
        for (BlockPos pos : compound) {
            flippedThisTick.add(pos);
            BlockState current = level.getBlockState(pos);
            if (current.is(this)) {
                level.setBlock(pos, current.cycle(REVERSED), Block.UPDATE_CLIENTS);
            }
        }
        level.playSound(
                null,
                origin,
                SoundEvents.ENDER_EYE_DEATH,
                SoundSource.BLOCKS,
                0.6F,
                1.2F + level.getRandom().nextFloat() * 0.2F
        );
    }

    private List<BlockPos> collectCompound(Level level, BlockPos origin) {
        int maxSize = Spellgems.CONFIG.astralBarrierMaxCompoundSize;
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        List<BlockPos> result = new ArrayList<>();

        BlockPos start = origin.immutable();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty() && result.size() < maxSize) {
            BlockPos pos = queue.removeFirst();
            if (!level.getBlockState(pos).is(this)) {
                continue;
            }
            result.add(pos);
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = pos.relative(direction);
                if (visited.add(neighbor) && level.getBlockState(neighbor).is(this)) {
                    queue.add(neighbor);
                }
            }
        }
        return result;
    }

    private static boolean canPass(BlockState state, CollisionContext context) {
        boolean reversed = state.getValue(REVERSED);
        if (!(context instanceof EntityCollisionContext entityContext) || entityContext.getEntity() == null) {
            // Empty-context / cached shape: MoveControl uses this to decide jumps.
            // Reversed (mobs pass) must look empty here or mobs jump every tick while inside.
            return reversed;
        }
        Entity entity = entityContext.getEntity();
        if (entity instanceof Player) {
            return !reversed;
        }
        if (entity instanceof Mob) {
            return reversed;
        }
        return false;
    }
}
