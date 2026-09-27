package net.pnovaczek.spellgems.spell;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.pnovaczek.spellgems.entity.SpellProjectile;
import net.pnovaczek.spellgems.recipe.TransmuteRecipe;
import net.pnovaczek.spellgems.recipe.TransmuteRecipeInput;

import java.util.Optional;

public class Transmute extends AbstractSpell {

    @Override
    public Identifier id() {
        return SpellIds.TRANSMUTE;
    }

    @Override
    public boolean repeatWhileHeld() {
        return false;
    }

    @Override
    protected boolean performCast(SpellContext context) {
        Level level = context.level();
        if (level.isClientSide()) {
            return false;
        }

        Vec3 direction = context.lookAngle();
        ProjectileHitHandler handler = new ProjectileHitHandler() {
            @Override
            public void onHit(SpellProjectile projectile, net.minecraft.world.phys.EntityHitResult result) {
            }

            @Override
            public void onHitBlock(SpellProjectile projectile, BlockHitResult result) {
                tryTransmute(projectile, result);
            }
        };
        SpellProjectile projectile = new SpellProjectile(context, direction, handler);
        level.addFreshEntity(projectile);

        SpellSounds.play(
                context,
                context.eyeOrigin(),
                SoundEvents.ALLAY_THROW,
                SoundSource.PLAYERS,
                0.6F,
                0.8F + level.getRandom().nextFloat() * 0.2F
        );
        return true;
    }

    static void tryTransmute(SpellProjectile projectile, BlockHitResult hit) {
        if (!(projectile.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockPos pos = hit.getBlockPos();
        BlockState input = serverLevel.getBlockState(pos);
        if (input.isAir() || !input.getFluidState().isEmpty()) {
            return;
        }
        if (projectile.getOwner() instanceof Player player && !player.mayInteract(serverLevel, pos)) {
            return;
        }
        if (!(serverLevel.recipeAccess() instanceof RecipeManager recipeManager)) {
            return;
        }

        Optional<RecipeHolder<TransmuteRecipe>> found = recipeManager.getRecipeFor(
                TransmuteRecipe.TYPE, new TransmuteRecipeInput(input), serverLevel);
        if (found.isEmpty()) {
            return;
        }

        BlockState output = found.get().value().resultState(input);
        if (output.isAir() || output.getBlock() == input.getBlock()) {
            return;
        }

        serverLevel.setBlock(pos, output, 3);
        serverLevel.gameEvent(projectile.getOwner(), GameEvent.BLOCK_CHANGE, pos);
        if (!projectile.muffledCast()) {
            serverLevel.playSound(
                    null,
                    pos,
                    SoundEvents.AMETHYST_BLOCK_HIT,
                    SoundSource.BLOCKS,
                    1.0F,
                    0.8F + serverLevel.getRandom().nextFloat() * 0.4F
            );
        }
        spawnGlowParticles(serverLevel, pos);
    }

    private static void spawnGlowParticles(ServerLevel level, BlockPos pos) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        level.sendParticles(ParticleTypes.GLOW, x, y, z, 16, 0.45, 0.45, 0.45, 0.0);
    }
}
