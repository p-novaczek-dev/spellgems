package net.pnovaczek.spellgems.spell;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.pnovaczek.spellgems.spell.enchantment.UtilityEnchantment;
import net.pnovaczek.spellgems.spell.enchantment.UtilityEnchantments;

import java.util.List;
import java.util.Optional;

public class Harvest extends AbstractSpell {

    private static final int AREA_RADIUS = 4;

    @Override
    public Identifier id() {
        return SpellIds.HARVEST;
    }

    @Override
    protected int getCooldownTicks() {
        return 10;
    }

    @Override
    protected boolean performCast(SpellContext context) {
        if (context.level().isClientSide()) {
            return false;
        }

        var level = context.level();
        BlockPos center = context.originBlockPos();
        boolean harvestedAny = false;

        List<UtilityEnchantment> utilities = (context.data() != null) ? context.data().utilityEffects() : List.of();
        boolean hasSmelt = utilities.stream().anyMatch(u -> u.is(UtilityEnchantments.SMELT));
        Entity breaker = context.caster();

        for (int dx = -AREA_RADIUS; dx <= AREA_RADIUS; dx++) {
            for (int dz = -AREA_RADIUS; dz <= AREA_RADIUS; dz++) {
                BlockPos base = center.offset(dx, 0, dz);
                if (tryHarvestCrop(level, base, breaker, hasSmelt) || tryHarvestCrop(level, base.above(), breaker, hasSmelt)) {
                    harvestedAny = true;
                }
            }
        }

        return harvestedAny;
    }

    private static boolean tryHarvestCrop(Level level, BlockPos pos, Entity breaker, boolean smelt) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof CropBlock crop) || !crop.isMaxAge(state)) {
            return false;
        }

        if (smelt && level instanceof ServerLevel serverLevel) {
            List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, serverLevel.getBlockEntity(pos), breaker, ItemStack.EMPTY);
            boolean dropped = false;
            for (ItemStack drop : drops) {
                ItemStack smelted = trySmelt(drop, serverLevel);
                if (!smelted.isEmpty()) {
                    Block.popResource(serverLevel, pos, smelted);
                    dropped = true;
                }
            }
            if (dropped) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                return true;
            }
        }

        return level.destroyBlock(pos, true, breaker);
    }

    private static ItemStack trySmelt(ItemStack input, ServerLevel level) {
        if (input.isEmpty()) return input;
        Optional<RecipeHolder<SmeltingRecipe>> recipeOpt = level.getServer().getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input), level);
        if (recipeOpt.isPresent()) {
            ItemStack result = recipeOpt.get().value().assemble(new SingleRecipeInput(input)).copy();
            result.setCount(result.getCount() * input.getCount());
            return result;
        }
        return input;
    }
}
