package net.pnovaczek.spellgems.spell;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.pnovaczek.spellgems.spell.enchantment.UtilityEnchantment;
import net.pnovaczek.spellgems.spell.enchantment.UtilityEnchantments;

import java.util.List;
import java.util.Optional;

public class BreakBlock extends AbstractSpell {

    @Override
    public Identifier id() {
        return SpellIds.BREAK_BLOCK;
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

        var caster = context.caster();
        double reach = caster instanceof Player player ? player.blockInteractionRange() : 5.0;
        BlockHitResult hit = SpellTargeting.resolveBlockHit(caster, reach);
        if (hit == null) {
            return false;
        }

        var pos = hit.getBlockPos();
        if (caster instanceof Player player && !player.isWithinBlockInteractionRange(pos, 0.0)) {
            return false;
        }

        BlockState state = context.level().getBlockState(pos);
        if (state.isAir()) {
            return false;
        }

        List<UtilityEnchantment> utilities = (context.data() != null)
                ? context.data().utilityEffects()
                : List.of();

        boolean hasSilkTouch = utilities.stream().anyMatch(u -> u.is(UtilityEnchantments.SILK_TOUCH));
        boolean hasSmelt = utilities.stream().anyMatch(u -> u.is(UtilityEnchantments.SMELT));

        if (caster instanceof ServerPlayer serverPlayer) {
            if (hasSilkTouch || hasSmelt) {
                handleCustomBlockBreak(context, pos, state, serverPlayer, hasSilkTouch, hasSmelt);
            } else {
                serverPlayer.gameMode.destroyBlock(pos);
            }
        } else {
            context.level().destroyBlock(pos, true, caster);
        }

        return true;
    }

    private void handleCustomBlockBreak(SpellContext context, BlockPos pos, BlockState state,
                                        ServerPlayer player, boolean silkTouch, boolean smelt) {
        ServerLevel level = (ServerLevel) context.level();

        List<ItemStack> drops;
        if (silkTouch) {
            ItemStack silkDrop = state.getCloneItemStack(level, pos, false);
            drops = silkDrop.isEmpty() ? List.of() : List.of(silkDrop);
        } else {
            drops = Block.getDrops(state, level, pos, level.getBlockEntity(pos), player, ItemStack.EMPTY);
        }

        if (smelt) {
            drops = drops.stream()
                    .map(stack -> trySmelt(stack, level))
                    .toList();
        }

        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                Block.popResource(level, pos, drop);
            }
        }

        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    }

    private ItemStack trySmelt(ItemStack input, ServerLevel level) {
        if (input.isEmpty()) return input;
        Optional<RecipeHolder<SmeltingRecipe>> recipeOpt = level.getServer().getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input), level);
        if (recipeOpt.isPresent()) {
            ItemStack result = recipeOpt.get().value().assemble(new SingleRecipeInput(input)).copy();
            // preserve stack size scaling if input >1 (basic)
            result.setCount(result.getCount() * input.getCount());
            return result;
        }
        return input;
    }
}