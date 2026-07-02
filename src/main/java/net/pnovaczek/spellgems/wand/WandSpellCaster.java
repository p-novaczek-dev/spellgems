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
import net.pnovaczek.spellgems.SpellgemsConfig;
import net.pnovaczek.spellgems.inventory.WandContainer;
import net.pnovaczek.spellgems.item.SpellGemItem;
import net.pnovaczek.spellgems.item.data.SpellGemData;
import net.pnovaczek.spellgems.item.data.WandData;
import net.pnovaczek.spellgems.spell.Spell;
import net.pnovaczek.spellgems.spell.SpellContext;
import net.pnovaczek.spellgems.spell.Spells;
import org.jspecify.annotations.Nullable;

public final class WandSpellCaster {

    private record CastRequest(Spell spell, SpellContext context) {
    }

    private WandSpellCaster() {
    }

    public static boolean tryCast(ServerPlayer player) {
        CastRequest request = prepareCast(player);
        if (request == null) {
            return false;
        }

        if (player.getCooldowns().isOnCooldown(request.context().castingItem())) {
            return false;
        }

        request.spell().cast(request.context());
        request.context().castingItem().hurtAndBreak(
                getDurabilityCost(request.spell().id()),
                player,
                InteractionHand.MAIN_HAND
        );
        player.swing(InteractionHand.MAIN_HAND);
        return true;
    }

    public static void tryCastVisuals(Player player) {
        if (!player.level().isClientSide()) {
            return;
        }

        CastRequest request = prepareCast(player);
        if (request == null) {
            return;
        }

        if (player.getCooldowns().isOnCooldown(request.context().castingItem())) {
            return;
        }

        request.spell().cast(request.context());
    }

    private static @Nullable CastRequest prepareCast(Player player) {
        ItemStack wand = player.getMainHandItem();
        if (!wand.is(ModItems.WAND) || wand.isBroken()) {
            return null;
        }

        WandData wandData = wand.getOrDefault(ModComponents.WAND_DATA, WandData.DEFAULT);
        SimpleContainer slots = new SimpleContainer(WandContainer.SIZE);
        WandContainer.loadInto(slots, wand);

        int slot = Mth.clamp(wandData.selectedSlot(), 0, WandContainer.SIZE - 1);
        ItemStack gemStack = slots.getItem(slot);
        if (gemStack.isEmpty()) {
            return null;
        }

        SpellGemData gemData = SpellGemItem.getSpellData(gemStack);
        Spell spell = SpellGemItem.getSpell(gemData);
        if (spell == null) {
            return null;
        }

        SpellContext context = new SpellContext(player.level(), player, wand, gemData);
        if (!spell.canCast(context)) {
            return null;
        }

        return new CastRequest(spell, context);
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
        if (!wand.is(ModItems.WAND) || wand.isBroken()) {
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

    public static int getDurabilityCost(Identifier spellId) {
        SpellgemsConfig.SpellConfigs spells = Spellgems.CONFIG.spells;
        if (spellId.equals(Spells.PROJECTILE)) {
            return spells.projectile.wandBaseDurabilityCost;
        }
        if (spellId.equals(Spells.NOVA)) {
            return spells.nova.wandBaseDurabilityCost;
        }
        if (spellId.equals(Spells.VORTEX)) {
            return spells.vortex.wandBaseDurabilityCost;
        }
        if (spellId.equals(Spells.POTION)) {
            return spells.potion.wandBaseDurabilityCost;
        }
        return 1;
    }
}