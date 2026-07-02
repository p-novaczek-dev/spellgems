package net.pnovaczek.spellgems.client;

import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.pnovaczek.spellgems.ModItems;
import net.pnovaczek.spellgems.client.network.WandClientNetworking;
import net.pnovaczek.spellgems.wand.WandSpellCaster;

public final class WandClientInput {

    private WandClientInput() {
    }

    public static void register() {
        ClientPreAttackCallback.EVENT.register(WandClientInput::onPreAttack);
    }

    private static boolean onPreAttack(Minecraft client, net.minecraft.client.player.LocalPlayer player, int clickCount) {
        ItemStack wand = player.getMainHandItem();
        if (!wand.is(ModItems.WAND) || wand.isBroken()) {
            return false;
        }

        if (clickCount != 0
                && !player.getCooldowns().isOnCooldown(wand)
                && client.missTime <= 0) {
            WandClientNetworking.sendCast();
            WandSpellCaster.tryCastVisuals(player);
        }

        return true;
    }

    public static boolean shouldCycleSpell(Minecraft client) {
        KeyMapping cycleKey = SpellgemsKeyMappings.CYCLE_SPELL_KEY;
        return cycleKey != null
                && client.player != null
                && client.screen == null
                && cycleKey.isDown()
                && client.player.getMainHandItem().is(ModItems.WAND);
    }
}