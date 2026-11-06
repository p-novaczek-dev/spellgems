package net.pnovaczek.spellgems.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.pnovaczek.spellgems.spell.affix.AffixExperience;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Both loaders. NeoForge's experience event does not run when vanilla skips the drop,
 * so dispenser kills scale experience here.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityAffixExperienceMixin {

    @Inject(method = "dropExperience", at = @At("HEAD"), cancellable = true)
    private void spellgems$affixExperience(ServerLevel level, Entity killer, CallbackInfo callback) {
        if (AffixExperience.redirect((LivingEntity) (Object) this, level, killer)) {
            callback.cancel();
        }
    }
}
