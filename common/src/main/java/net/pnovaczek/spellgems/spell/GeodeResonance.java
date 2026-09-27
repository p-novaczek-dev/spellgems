package net.pnovaczek.spellgems.spell;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.SpellgemsConfig;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Jumps between budding amethyst (and other budding-amethyst subclasses, such as GeOre)
 * and grows or harvests the clusters on each one.
 */
public class GeodeResonance extends AbstractSpell {

    private static final List<Block> VANILLA_STAGES = List.of(
            Blocks.SMALL_AMETHYST_BUD,
            Blocks.MEDIUM_AMETHYST_BUD,
            Blocks.LARGE_AMETHYST_BUD,
            Blocks.AMETHYST_CLUSTER
    );

    private static final Map<Block, List<Block>> STAGE_CACHE = new IdentityHashMap<>();

    @Override
    public Identifier id() {
        return SpellIds.GEODE_RESONANCE;
    }

    @Override
    protected int getCooldownTicks() {
        SpellgemsConfig.GeodeResonanceSpellConfig config = Spellgems.CONFIG.spells.geodeResonance;
        return Math.max(10, config.jumpCount * config.jumpDelayTicks);
    }

    @Override
    protected void performPredictedFx(SpellContext context) {
        // The path is chosen on the server. Particles and sound are broadcast from there.
    }

    @Override
    protected boolean performCast(SpellContext context) {
        if (!(context.level() instanceof ServerLevel level)) {
            return false;
        }

        SpellgemsConfig.GeodeResonanceSpellConfig config = Spellgems.CONFIG.spells.geodeResonance;
        BlockPos origin = context.originBlockPos();
        if (findRandomBud(level, origin, Set.of()) == null) {
            return false;
        }

        continueWave(
                level,
                context.caster(),
                origin,
                new HashSet<>(),
                config.jumpCount,
                config.jumpDelayTicks,
                config.growthChance,
                context.muffled()
        );
        return true;
    }

    private static void continueWave(
            ServerLevel level,
            @Nullable Entity caster,
            BlockPos from,
            Set<BlockPos> visited,
            int jumpsLeft,
            int delayTicks,
            float growthChance,
            boolean muffled
    ) {
        if (jumpsLeft <= 0 || level.getServer() == null) {
            return;
        }
        if (caster != null && !caster.isAlive()) {
            return;
        }
        if (!level.isLoaded(from)) {
            return;
        }

        BlockPos next = findRandomBud(level, from, visited);
        if (next == null) {
            return;
        }
        visited.add(next);
        resonate(level, caster, next, growthChance, muffled);

        if (jumpsLeft <= 1) {
            return;
        }
        int tick = level.getServer().getTickCount();
        SpellBurstScheduler.scheduleServer(tick, delayTicks, () -> continueWave(
                level, caster, next, visited, jumpsLeft - 1, delayTicks, growthChance, muffled
        ));
    }

    private static void resonate(
            ServerLevel level,
            @Nullable Entity caster,
            BlockPos budPos,
            float growthChance,
            boolean muffled
    ) {
        if (!muffled) {
            level.playSound(
                    null,
                    budPos,
                    SoundEvents.AMETHYST_BLOCK_RESONATE,
                    SoundSource.BLOCKS,
                    0.6F,
                    1.0F
            );
        }
        spawnSparkParticles(level, budPos);

        BlockState budState = level.getBlockState(budPos);
        if (!(budState.getBlock() instanceof BuddingAmethystBlock)) {
            return;
        }
        List<Block> stages = stagesFor(budState.getBlock());
        if (stages.isEmpty()) {
            return;
        }

        RandomSource random = level.getRandom();
        Block mature = stages.get(stages.size() - 1);
        for (Direction direction : Direction.values()) {
            BlockPos side = budPos.relative(direction);
            if (!level.isLoaded(side)) {
                continue;
            }
            BlockState sideState = level.getBlockState(side);
            if (isClusterOnBud(sideState, direction)) {
                int stage = stages.indexOf(sideState.getBlock());
                if (stage < 0) {
                    continue;
                }
                if (sideState.is(mature)) {
                    level.destroyBlock(side, true, caster);
                } else if (random.nextFloat() < growthChance) {
                    placeCluster(level, side, sideState, stages.get(stage + 1), direction);
                }
            } else if (BuddingAmethystBlock.canClusterGrowAtState(sideState) && random.nextFloat() < growthChance) {
                placeCluster(level, side, sideState, stages.get(0), direction);
            }
        }
    }

    private static void spawnSparkParticles(ServerLevel level, BlockPos pos) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 16, 0.45, 0.45, 0.45, 0.0);
    }

    private static boolean isClusterOnBud(BlockState state, Direction outward) {
        return state.getBlock() instanceof AmethystClusterBlock
                && state.getValue(AmethystClusterBlock.FACING) == outward;
    }

    private static void placeCluster(ServerLevel level, BlockPos pos, BlockState previous, Block stage, Direction facing) {
        BlockState placed = stage.defaultBlockState()
                .setValue(AmethystClusterBlock.FACING, facing)
                .setValue(AmethystClusterBlock.WATERLOGGED, previous.getFluidState().is(Fluids.WATER));
        level.setBlockAndUpdate(pos, placed);
    }

    private static @Nullable BlockPos findRandomBud(ServerLevel level, BlockPos center, Set<BlockPos> visited) {
        int radius = Spellgems.CONFIG.spells.geodeResonance.searchRadius;
        long radiusSqr = (long) radius * radius;
        List<BlockPos> candidates = new ArrayList<>();
        BlockPos min = center.offset(-radius, -radius, -radius);
        BlockPos max = center.offset(radius, radius, radius);
        for (BlockPos cursor : BlockPos.betweenClosed(min, max)) {
            if (center.distSqr(cursor) > radiusSqr) {
                continue;
            }
            if (!level.isLoaded(cursor)) {
                continue;
            }
            BlockPos pos = cursor.immutable();
            if (visited.contains(pos)) {
                continue;
            }
            if (level.getBlockState(pos).getBlock() instanceof BuddingAmethystBlock) {
                candidates.add(pos);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(level.getRandom().nextInt(candidates.size()));
    }

    private static List<Block> stagesFor(Block budding) {
        return STAGE_CACHE.computeIfAbsent(budding, GeodeResonance::resolveStages);
    }

    /**
     * Vanilla budding amethyst uses the amethyst bud chain. Subclasses such as GeOre's
     * budding blocks keep their own small/medium/large/cluster blocks in supplier fields.
     */
    private static List<Block> resolveStages(Block budding) {
        if (budding.getClass() == BuddingAmethystBlock.class) {
            return VANILLA_STAGES;
        }

        Block[] named = new Block[4];
        List<Block> declared = new ArrayList<>();
        Class<?> type = budding.getClass();
        while (type != null && BuddingAmethystBlock.class.isAssignableFrom(type)) {
            for (Field field : type.getDeclaredFields()) {
                Block stage = supplierBlock(budding, field);
                if (stage == null) {
                    continue;
                }
                declared.add(stage);
                String name = field.getName().toLowerCase(Locale.ROOT);
                if (name.contains("small")) {
                    named[0] = stage;
                } else if (name.contains("medium")) {
                    named[1] = stage;
                } else if (name.contains("large")) {
                    named[2] = stage;
                } else if (name.contains("cluster")) {
                    named[3] = stage;
                }
            }
            type = type.getSuperclass();
        }

        List<Block> namedStages = new ArrayList<>();
        for (Block stage : named) {
            if (stage != null) {
                namedStages.add(stage);
            }
        }
        if (namedStages.size() >= 2) {
            return List.copyOf(namedStages);
        }
        if (declared.size() >= 2) {
            return List.copyOf(declared);
        }
        return List.of();
    }

    private static @Nullable Block supplierBlock(Block budding, Field field) {
        if (!Supplier.class.isAssignableFrom(field.getType())) {
            return null;
        }
        try {
            field.setAccessible(true);
            if (!(field.get(budding) instanceof Supplier<?> supplier)) {
                return null;
            }
            return supplier.get() instanceof Block block ? block : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
