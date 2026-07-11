package net.pnovaczek.spellgems.wand;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.pnovaczek.spellgems.ModComponents;
import net.pnovaczek.spellgems.ModItems;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.WandConfig;
import net.pnovaczek.spellgems.inventory.WandContainer;
import net.pnovaczek.spellgems.item.SpellGemItem;
import net.pnovaczek.spellgems.item.data.SpellGemData;
import net.pnovaczek.spellgems.item.data.WandData;
import net.pnovaczek.spellgems.spell.Spell;
import net.pnovaczek.spellgems.spell.SpellContext;
import net.pnovaczek.spellgems.spell.Spells;
import org.jspecify.annotations.Nullable;

public final class WandSpellCaster {

    private record CastRequest(Spell spell, SpellContext context, int effectiveDurabilityCost) {
    }

    private WandSpellCaster() {
    }

    public static boolean canCast(Player player) {
        return prepareCast(player, null) != null;
    }

    public static boolean canCastFromSlot(Player player, int slot) {
        return prepareCast(player, slot) != null;
    }

    public static boolean tryCast(ServerPlayer player) {
        return tryCastFromSlot(player, null);
    }

    public static boolean tryCastFromSlot(ServerPlayer player, @Nullable Integer slot) {
        CastRequest request = prepareCast(player, slot);
        if (request == null) {
            return false;
        }

        request.spell().cast(request.context());
        InteractionHand hand = getWandHand(player);
        WandDepletion.applyDurabilityCost(request.context().castingItem(), request.effectiveDurabilityCost(), player);
        player.swing(hand);
        return true;
    }

    public static void tryCastVisuals(Player player) {
        tryCastVisualsFromSlot(player, null);
    }

    public static void tryCastVisualsFromSlot(Player player, @Nullable Integer slot) {
        if (!player.level().isClientSide()) {
            return;
        }

        CastRequest request = prepareCast(player, slot);
        if (request == null) {
            return;
        }

        request.spell().cast(request.context());
    }

    private static @Nullable CastRequest prepareCast(Player player, @Nullable Integer slotOverride) {
        ItemStack wand = player.getMainHandItem();
        if (!wand.is(ModItems.WAND) || WandDepletion.isDepleted(wand)) {
            return null;
        }

        SimpleContainer slots = new SimpleContainer(WandContainer.SIZE);
        WandContainer.loadInto(slots, wand);

        int slot = slotOverride != null
                ? Mth.clamp(slotOverride, 0, WandContainer.SIZE - 1)
                : Mth.clamp(wand.getOrDefault(ModComponents.WAND_DATA, WandData.DEFAULT).selectedSlot(), 0, WandContainer.SIZE - 1);
        ItemStack gemStack = slots.getItem(slot);
        if (gemStack.isEmpty()) {
            return null;
        }

        SpellGemData gemData = SpellGemItem.getSpellData(gemStack);
        Spell spell = SpellGemItem.getSpell(gemData);
        if (spell == null) {
            return null;
        }

        int nominalCost = getDurabilityCost(spell.id(), gemData);
        int effectiveCost = WandDepletion.resolveEffectiveDurabilityCost(player, wand, nominalCost);
        if (!WandDepletion.canAffordDurabilityCost(wand, effectiveCost)) {
            return null;
        }

        SpellContext context = new SpellContext(player.level(), player, wand, gemData);
        if (!spell.canCast(context)) {
            return null;
        }

        return new CastRequest(spell, context, effectiveCost);
    }

    public static boolean cycleSelectedSpell(ServerPlayer player, int direction) {
        return applyCycle(player, direction);
    }

    public static boolean applyLocalCycle(Player player, int direction) {
        return applyCycle(player, direction);
    }

    private static boolean applyCycle(Player player, int direction) {
        if (direction == 0) {
            return false;
        }

        ItemStack wand = player.getMainHandItem();
        if (!wand.is(ModItems.WAND)) {
            return false;
        }

        SimpleContainer slots = new SimpleContainer(WandContainer.SIZE);
        WandContainer.loadInto(slots, wand);

        WandData wandData = wand.getOrDefault(ModComponents.WAND_DATA, WandData.DEFAULT);
        int current = Mth.clamp(wandData.selectedSlot(), 0, WandContainer.SIZE - 1);
        int next = findNextOccupiedSlot(slots, current, Integer.signum(direction));
        if (next == current) {
            return false;
        }

        wand.set(ModComponents.WAND_DATA, new WandData(next));
        return true;
    }

    public static int findNextOccupiedSlot(SimpleContainer slots, int current, int direction) {
        for (int step = 1; step <= WandContainer.SIZE; step++) {
            int index = (current + direction * step + WandContainer.SIZE) % WandContainer.SIZE;
            if (!slots.getItem(index).isEmpty()) {
                return index;
            }
        }
        return current;
    }

    public static int getDurabilityCost(Identifier spellId, SpellGemData gemData) {
        float cost = getBaseDurabilityCost(spellId);
        if (gemData != null) {
            float multiplier = Spellgems.CONFIG.wand.spellEnchantmentDurabilityCostMultiplier;
            for (int i = 0; i < gemData.enchantmentCount(); i++) {
                cost *= multiplier;
            }
        }
        return Math.max(1, Math.round(cost));
    }

    private static int getBaseDurabilityCost(Identifier spellId) {
        WandConfig.WandSpellDurabilityCosts costs = Spellgems.CONFIG.wand.spells;
        if (spellId.equals(Spells.PROJECTILE)) {
            return costs.projectile;
        }
        if (spellId.equals(Spells.NOVA)) {
            return costs.nova;
        }
        if (spellId.equals(Spells.VORTEX)) {
            return costs.vortex;
        }
        if (spellId.equals(Spells.BLINK)) {
            return costs.blink;
        }
        if (spellId.equals(Spells.WIND_CHARGE)) {
            return costs.windCharge;
        }
        if (spellId.equals(Spells.MAGNET)) {
            return costs.magnet;
        }
        if (spellId.equals(Spells.PLACE_BLOCK)) {
            return costs.placeBlock;
        }
        if (spellId.equals(Spells.BREAK_BLOCK)) {
            return costs.breakBlock;
        }
        if (spellId.equals(Spells.PLANT)) {
            return costs.plant;
        }
        if (spellId.equals(Spells.HARVEST)) {
            return costs.harvest;
        }
        if (spellId.equals(Spells.FEED)) {
            return costs.feed;
        }
        if (spellId.equals(Spells.GROW)) {
            return costs.grow;
        }
        if (spellId.equals(Spells.POTION)) {
            return costs.potion;
        }
        return 1;
    }

    private static InteractionHand getWandHand(Player player) {
        return player.getMainHandItem().is(ModItems.WAND)
                ? InteractionHand.MAIN_HAND
                : InteractionHand.OFF_HAND;
    }
}