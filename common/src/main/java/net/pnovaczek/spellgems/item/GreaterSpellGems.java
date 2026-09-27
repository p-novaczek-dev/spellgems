package net.pnovaczek.spellgems.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.pnovaczek.spellgems.ModComponents;
import net.pnovaczek.spellgems.ModItems;
import net.pnovaczek.spellgems.ModSpells;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.item.data.SpellGemData;
import net.pnovaczek.spellgems.spell.Spell;
import net.pnovaczek.spellgems.spell.SpellBurstScheduler;
import net.pnovaczek.spellgems.spell.SpellContext;
import net.pnovaczek.spellgems.spell.SpellIds;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Combines two combat gems into a greater gem and schedules the follow-up spell on direct hits.
 */
public final class GreaterSpellGems {

    public record Combo(net.minecraft.resources.Identifier first, net.minecraft.resources.Identifier second) {}

    public static final List<Combo> COMBOS = List.of(
            new Combo(SpellIds.PROJECTILE, SpellIds.NOVA),
            new Combo(SpellIds.PROJECTILE, SpellIds.VORTEX),
            new Combo(SpellIds.VORTEX, SpellIds.NOVA)
    );

    private GreaterSpellGems() {
    }

    public static Optional<ItemStack> tryCombine(ItemStack first, ItemStack second) {
        if (first.isEmpty() || second.isEmpty()) {
            return Optional.empty();
        }
        if (first.is(ModItems.GREATER_SPELL_GEM) || second.is(ModItems.GREATER_SPELL_GEM)) {
            return Optional.empty();
        }
        if (!(first.getItem() instanceof SpellGemItem) || !(second.getItem() instanceof SpellGemItem)) {
            return Optional.empty();
        }

        SpellGemData dataA = first.get(ModComponents.SPELL_GEM_DATA);
        SpellGemData dataB = second.get(ModComponents.SPELL_GEM_DATA);
        if (dataA == null || dataB == null || dataA.followUp().isPresent() || dataB.followUp().isPresent()) {
            return Optional.empty();
        }

        Combo combo = findCombo(dataA.spellId(), dataB.spellId());
        if (combo == null) {
            return Optional.empty();
        }

        SpellGemData firstPart = combo.first().equals(dataA.spellId()) ? dataA : dataB;
        SpellGemData secondPart = firstPart == dataA ? dataB : dataA;
        return Optional.of(createStack(firstPart.withoutFollowUp(), secondPart.withoutFollowUp()));
    }

    public static ItemStack createStack(SpellGemData first, SpellGemData second) {
        ItemStack stack = new ItemStack(ModItems.GREATER_SPELL_GEM);
        stack.set(ModComponents.SPELL_GEM_DATA, first.withoutFollowUp().withFollowUp(second.withoutFollowUp()));
        return stack;
    }

    public static List<ItemStack> allCombinationStacks() {
        return List.of(
                createStack(SpellGemData.create(SpellIds.PROJECTILE), SpellGemData.create(SpellIds.NOVA)),
                createStack(SpellGemData.create(SpellIds.PROJECTILE), SpellGemData.create(SpellIds.VORTEX)),
                createStack(SpellGemData.create(SpellIds.VORTEX), SpellGemData.create(SpellIds.NOVA))
        );
    }

    private static Combo findCombo(net.minecraft.resources.Identifier a, net.minecraft.resources.Identifier b) {
        for (Combo combo : COMBOS) {
            boolean match = (combo.first().equals(a) && combo.second().equals(b))
                    || (combo.first().equals(b) && combo.second().equals(a));
            if (match) {
                return combo;
            }
        }
        return null;
    }

    /**
     * After the first spell directly hits a living target, schedule the follow-up spell on that target.
     * Ignored for follow-up casts themselves and for gems without a chained spell.
     */
    public static void onDirectHit(SpellContext context, LivingEntity target) {
        if (context.source().isFollowUp() || target == null || !target.isAlive()) {
            return;
        }
        SpellGemData follow = context.data() == null ? null : context.data().followUp().orElse(null);
        if (follow == null) {
            return;
        }
        if (!(context.level() instanceof ServerLevel serverLevel) || serverLevel.getServer() == null) {
            return;
        }

        UUID targetId = target.getUUID();
        UUID casterId = context.caster() != null ? context.caster().getUUID() : null;
        ItemStack castingItem = context.castingItem().copy();
        SpellGemData followData = follow.withoutFollowUp();
        int delay = Spellgems.CONFIG.greaterGemFollowUpDelayTicks;

        Runnable action = () -> {
            Entity entity = serverLevel.getEntity(targetId);
            if (!(entity instanceof LivingEntity living) || !living.isAlive()) {
                return;
            }
            LivingEntity caster = null;
            if (casterId != null) {
                Entity casterEntity = serverLevel.getEntity(casterId);
                if (casterEntity instanceof LivingEntity livingCaster) {
                    if (!livingCaster.isAlive()) {
                        return;
                    }
                    caster = livingCaster;
                }
            }
            Spell spell = ModSpells.get(followData.spellId());
            if (spell == null) {
                return;
            }
            SpellContext followContext = SpellContext.forFollowUp(
                    serverLevel, caster, castingItem, followData, living
            ).withMuffled(context.muffled());
            if (spell.canCast(followContext)) {
                spell.cast(followContext);
            }
        };

        if (delay <= 0) {
            action.run();
        } else {
            SpellBurstScheduler.scheduleServer(serverLevel.getServer().getTickCount(), delay, action);
        }
    }
}
