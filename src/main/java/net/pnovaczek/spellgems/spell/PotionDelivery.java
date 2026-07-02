package net.pnovaczek.spellgems.spell;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.pnovaczek.spellgems.spell.enchantment.PotionDeliveryType;
import net.pnovaczek.spellgems.spell.enchantment.PotionEnchantment;

import java.util.List;

public final class PotionDelivery {

    private PotionDelivery() {
    }

    public static void apply(ServerLevel level, LivingEntity caster, PotionEnchantment enchantment) {
        switch (enchantment.delivery()) {
            case DRINK -> applyDrink(level, caster, enchantment);
            case SPLASH -> applySplashAt(level, caster, caster.position(), enchantment);
            case LINGERING -> applyLingeringAt(level, caster, caster.position(), enchantment);
        }
    }

    public static void applyOnEntityHit(
            ServerLevel level,
            LivingEntity shooter,
            LivingEntity target,
            Vec3 hitPos,
            PotionEnchantment enchantment
    ) {
        switch (enchantment.delivery()) {
            case DRINK -> applyDrinkToTarget(level, target, enchantment);
            case SPLASH -> applySplashAt(level, shooter, hitPos, enchantment);
            case LINGERING -> applyLingeringAt(level, shooter, hitPos, enchantment);
        }
    }

    public static void applyOnBlockHit(
            ServerLevel level,
            LivingEntity shooter,
            Vec3 hitPos,
            PotionEnchantment enchantment
    ) {
        switch (enchantment.delivery()) {
            case DRINK -> { }
            case SPLASH -> applySplashAt(level, shooter, hitPos, enchantment);
            case LINGERING -> applyLingeringAt(level, shooter, hitPos, enchantment);
        }
    }

    private static void applyDrink(ServerLevel level, LivingEntity caster, PotionEnchantment enchantment) {
        enchantment.contents().applyToLivingEntity(caster, 1.0F);
        playDrinkSound(level, caster);
    }

    private static void applyDrinkToTarget(ServerLevel level, LivingEntity target, PotionEnchantment enchantment) {
        enchantment.contents().applyToLivingEntity(target, enchantment.durationScale());
    }

    private static void applySplashAt(
            ServerLevel level,
            LivingEntity source,
            Vec3 hitPos,
            PotionEnchantment enchantment
    ) {
        PotionContents contents = enchantment.contents();
        float durationScale = enchantment.durationScale();

        AABB potionAabb = new AABB(hitPos, hitPos).inflate(0.5, 0.25, 0.5);
        AABB effectAabb = potionAabb.inflate(4.0, 2.0, 4.0);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, effectAabb);
        Iterable<MobEffectInstance> mobEffects = contents.getAllEffects();
        float margin = 0.3F;

        if (!entities.isEmpty()) {
            for (LivingEntity entity : entities) {
                if (!entity.isAffectedByPotions()) {
                    continue;
                }

                double dist = potionAabb.distanceToSqr(entity.getBoundingBox().inflate(margin));
                if (dist < 16.0) {
                    double scale = 1.0 - Math.sqrt(dist) / 4.0;
                    applyScaledEffects(level, source, entity, mobEffects, durationScale, scale);
                }
            }
        }

        playSplashEffects(level, hitPos, contents);
    }

    private static void applyLingeringAt(
            ServerLevel level,
            LivingEntity source,
            Vec3 hitPos,
            PotionEnchantment enchantment
    ) {
        ItemStack potionItem = enchantment.toItemStack();
        AreaEffectCloud cloud = new AreaEffectCloud(level, hitPos.x, hitPos.y, hitPos.z);
        cloud.setOwner(source);
        cloud.setRadius(3.0F);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setDuration(600);
        cloud.setWaitTime(10);
        cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());
        cloud.applyComponentsFromItemStack(potionItem);
        level.addFreshEntity(cloud);

        PotionContents contents = enchantment.contents();
        int eventType = contents.potion().isPresent()
                && contents.potion().get().value().hasInstantEffects() ? 2007 : 2002;
        level.levelEvent(eventType, BlockPos.containing(hitPos), contents.getColor());
    }

    private static void playSplashEffects(ServerLevel level, Vec3 hitPos, PotionContents contents) {
        int eventType = contents.potion().isPresent()
                && contents.potion().get().value().hasInstantEffects() ? 2007 : 2002;
        level.levelEvent(eventType, BlockPos.containing(hitPos), contents.getColor());
    }

    private static void applyScaledEffects(
            ServerLevel level,
            Entity source,
            LivingEntity target,
            Iterable<MobEffectInstance> mobEffects,
            float durationScale,
            double scale
    ) {
        for (MobEffectInstance effectInstance : mobEffects) {
            Holder<MobEffect> effect = effectInstance.getEffect();
            if (effect.value().isInstantenous()) {
                effect.value().applyInstantenousEffect(level, source, source instanceof LivingEntity living ? living : null,
                        target, effectInstance.getAmplifier(), scale);
            } else {
                int duration = effectInstance.mapDuration(d -> (int) (scale * d * durationScale + 0.5));
                MobEffectInstance newEffect = new MobEffectInstance(
                        effect,
                        duration,
                        effectInstance.getAmplifier(),
                        effectInstance.isAmbient(),
                        effectInstance.isVisible()
                );
                if (!newEffect.endsWithin(20)) {
                    target.addEffect(newEffect, source);
                }
            }
        }
    }

    private static void playDrinkSound(Level level, LivingEntity caster) {
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.5F,
                level.getRandom().nextFloat() * 0.1F + 0.9F);
    }

    public static void playClientEffects(Level level, LivingEntity caster, PotionEnchantment enchantment) {
        if (!level.isClientSide()) {
            return;
        }

        PotionContents contents = enchantment.contents();
        int eventType = contents.potion().isPresent()
                && contents.potion().get().value().hasInstantEffects() ? 2007 : 2002;

        if (enchantment.delivery() == PotionDeliveryType.DRINK) {
            playDrinkSound(level, caster);
        } else {
            level.levelEvent(eventType, caster.blockPosition(), contents.getColor());
        }
    }
}