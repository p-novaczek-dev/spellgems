package net.pnovaczek.spellgems.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.pnovaczek.spellgems.ModItems;
import net.pnovaczek.spellgems.client.network.WandClientNetworking;
import net.pnovaczek.spellgems.platform.client.ClientPlatform;
import net.pnovaczek.spellgems.wand.WandSpellCaster;

public final class WandClientInput {

    private WandClientInput() {
    }

    public static void register() {
        ClientPlatform.client().onPreAttack(WandClientInput::onPreAttack);
        ClientPlatform.client().onEndClientTick(WandClientInput::onClientTick);
    }

    private static boolean onPreAttack(Minecraft client, net.minecraft.client.player.LocalPlayer player, int clickCount) {
        if (!player.getMainHandItem().is(ModItems.WAND) || !WandSpellCaster.canCast(player)) {
            return false;
        }

        // clickCount==0 is a synthetic callback; miss-time is loader-private on Neo so we only gate on clickCount.
        if (clickCount != 0) {
            // Server is authoritative; local prediction only for responsive FX.
            WandClientNetworking.sendCast();
            WandSpellCaster.tryPredictCast(player);
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
        WandSpellCaster.tryPredictCastFromSlot(player, slot);
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
