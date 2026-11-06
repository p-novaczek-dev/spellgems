package net.pnovaczek.spellgems.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.pnovaczek.spellgems.spell.affix.AffixDropCapture;
import net.pnovaczek.spellgems.spell.affix.AffixDrops;
import net.pnovaczek.spellgems.spell.affix.AffixKills;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric has no living-drops event. Capture items spawned during death, rewrite them, then add the survivors.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityAffixLootMixin {

    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
    private void spellgems$beginAffixLoot(ServerLevel level, DamageSource source, CallbackInfo callback) {
        LivingEntity self = (LivingEntity) (Object) this;
        AffixKills.storeEquipment(self);
        AffixDropCapture.push(AffixDrops.shouldRewrite(self));
    }

    @Inject(method = "dropAllDeathLoot", at = @At("RETURN"))
    private void spellgems$finishAffixLoot(ServerLevel level, DamageSource source, CallbackInfo callback) {
        AffixDropCapture.finish((LivingEntity) (Object) this, level);
    }
}
