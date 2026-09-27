package net.pnovaczek.spellgems.spell;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.spell.enchantment.UtilityEnchantment;
import net.pnovaczek.spellgems.spell.enchantment.UtilityEnchantments;

import java.util.List;

public class Magnet extends AbstractSpell {

    private static final float PULL_SPEED = 0.65F;

    @Override
    public Identifier id() {
        return SpellIds.MAGNET;
    }

    @Override
    protected void performPredictedFx(SpellContext context) {
        // No local FX. The default would run the pull on the client.
    }

    @Override
    protected boolean performCast(SpellContext context) {
        var level = context.level();
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        float baseRange = Spellgems.CONFIG.spells.magnet.range;
        List<UtilityEnchantment> utilities = (context.data() != null) ? context.data().utilityEffects() : List.of();
        boolean hasExtend = utilities.stream().anyMatch(u -> u.is(UtilityEnchantments.EXTEND));
        float range = hasExtend
                ? (float) (baseRange * Spellgems.CONFIG.spells.magnet.extendMultiplier)
                : baseRange;

        Vec3 pullTarget = magnetPullTarget(context);

        AABB searchBox = new AABB(pullTarget, pullTarget).inflate(range);
        double rangeSqr = range * range;
        List<ItemEntity> items = serverLevel.getEntitiesOfClass(
                ItemEntity.class,
                searchBox,
                item -> item.isAlive()
                        && !item.getItem().isEmpty()
                        && item.position().distanceToSqr(pullTarget) <= rangeSqr
        );

        boolean pulledAny = false;
        for (ItemEntity item : items) {
            Vec3 offset = pullTarget.subtract(item.position());
            if (offset.lengthSqr() < 1.0E-8) {
                continue;
            }

            item.setDeltaMovement(offset.normalize().scale(PULL_SPEED));
            // Player casts still magnetize pickup; dispenser casts only pull (BE absorbs items later).
            if (context.caster() instanceof Player player && !context.isDispenserCast()) {
                item.setTarget(player.getUUID());
                item.setPickUpDelay(0);
            }
            pulledAny = true;
        }

        if (pulledAny) {
            SpellSounds.play(
                    context,
                    pullTarget,
                    SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS,
                    0.35F,
                    1.2F
            );
        }

        return true;
    }

    /**
     * Pull destination: player eye mid-height for living casters; raw origin for machines.
     */
    private static Vec3 magnetPullTarget(SpellContext context) {
        if (context.caster() != null && !context.isDispenserCast()) {
            var caster = context.caster();
            return caster.position().add(0.0, caster.getEyeHeight() * 0.5, 0.0);
        }
        return context.origin();
    }
}
