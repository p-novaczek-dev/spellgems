package net.pnovaczek.spellgems.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.pnovaczek.spellgems.spell.affix.AffixDropCapture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Holds death drops until {@link LivingEntityAffixLootMixin} rewrites them.
 */
@Mixin(Entity.class)
public abstract class EntityAffixSpawnMixin {

    @Inject(
            method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void spellgems$captureDeathDrop(
            ServerLevel level,
            ItemStack stack,
            Vec3 offset,
            CallbackInfoReturnable<ItemEntity> callback
    ) {
        ItemEntity[] held = new ItemEntity[1];
        if (AffixDropCapture.tryCapture((Entity) (Object) this, level, stack, offset, drop -> held[0] = drop)) {
            callback.setReturnValue(held[0]);
        }
    }
}
