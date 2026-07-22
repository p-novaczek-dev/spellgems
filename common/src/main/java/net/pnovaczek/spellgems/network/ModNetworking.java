package net.pnovaczek.spellgems.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.pnovaczek.spellgems.ModItems;
import net.pnovaczek.spellgems.astralbow.AstralBowCaster;
import net.pnovaczek.spellgems.platform.Platform;
import net.pnovaczek.spellgems.screen.SpellEnchantingMenu;
import net.pnovaczek.spellgems.wand.WandSpellCaster;

import java.util.List;

/**
 * Common networking orchestration. Loader-specific payload registration/send lives in {@link Platform#network()}.
 */
public final class ModNetworking {

    private ModNetworking() {
    }

    public static void registerPayloadTypes() {
        Platform.network().registerPayloadTypes();
    }

    public static void registerServerReceivers() {
        Platform.network().registerServerReceivers();
    }

    public static void handleWandInput(ServerPlayer player, WandInputPayload payload) {
        switch (payload.action()) {
            case CAST -> WandSpellCaster.tryCast(player);
            case QUICK_CAST -> WandSpellCaster.tryCastFromSlot(player, payload.value());
            case CYCLE -> {
                ItemStack held = player.getMainHandItem();
                if (held.is(ModItems.WAND)) {
                    WandSpellCaster.cycleSelectedSpell(player, payload.value());
                } else if (held.is(ModItems.ASTRAL_BOW)) {
                    AstralBowCaster.cycleSelectedGem(player, payload.value());
                }
            }
        }
    }

    public static void sendRecipeDescriptions(ServerPlayer player, SpellEnchantingMenu menu, List<String> descriptions) {
        var type = SpellEnchantingRecipeDescriptionsPayload.TYPE;
        if (!Platform.network().canSend(player, type)) {
            return;
        }
        Platform.network().send(
                player,
                new SpellEnchantingRecipeDescriptionsPayload(menu.containerId, descriptions)
        );
    }
}
