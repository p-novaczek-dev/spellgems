package net.pnovaczek.spellgems.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.pnovaczek.spellgems.ModItems;
import net.pnovaczek.spellgems.client.network.WandClientNetworking;
import net.pnovaczek.spellgems.wand.WandSpellCaster;

public final class WandClientInput {

    private WandClientInput() {
    }

    public static void register() {
        ClientPreAttackCallback.EVENT.register(WandClientInput::onPreAttack);
        ClientTickEvents.END_CLIENT_TICK.register(WandClientInput::onClientTick);
    }

    private static boolean onPreAttack(Minecraft client, net.minecraft.client.player.LocalPlayer player, int clickCount) {
        if (!player.getMainHandItem().is(ModItems.WAND) || !WandSpellCaster.canCast(player)) {
            return false;
        }

        if (clickCount != 0 && client.missTime <= 0) {
            WandClientNetworking.sendCast();
            WandSpellCaster.tryCastVisuals(player);
        }

        return true;
    }

    private static void onClientTick(Minecraft client) {
        if (client.player == null || client.screen != null) {
            return;
        }

        if (!client.player.getMainHandItem().is(ModItems.WAND)) {
            return;
        }

        for (int slot = 0; slot < SpellgemsKeyMappings.WAND_QUICK_CAST_SLOT_COUNT; slot++) {
            KeyMapping key = SpellgemsKeyMappings.WAND_QUICK_CAST_KEYS[slot];
            if (key != null && key.consumeClick()) {
                onQuickCast(client, slot);
                return;
            }
        }
    }

    private static void onQuickCast(Minecraft client, int slot) {
        var player = client.player;
        if (!WandSpellCaster.canCastFromSlot(player, slot)) {
            return;
        }

        WandClientNetworking.sendQuickCast(slot);
        WandSpellCaster.tryCastVisualsFromSlot(player, slot);
    }

    public static boolean shouldCycleSpell(Minecraft client) {
        KeyMapping cycleKey = SpellgemsKeyMappings.CYCLE_SPELL_KEY;
        return cycleKey != null
                && client.player != null
                && client.screen == null
                && cycleKey.isDown()
                && (client.player.getMainHandItem().is(ModItems.WAND)
                        || client.player.getMainHandItem().is(ModItems.ASTRAL_BOW));
    }
}