package net.pnovaczek.spellgems.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.ScrollWheelHandler;
import net.pnovaczek.spellgems.client.WandClientInput;
import net.pnovaczek.spellgems.client.WandSpellHighlight;
import net.pnovaczek.spellgems.client.network.WandClientNetworking;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Shared Fabric + NeoForge: while the cycle key is held, hotbar scroll cycles wand/bow gems.
 */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Redirect(
            method = "onScroll",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/ScrollWheelHandler;getNextScrollWheelSelection(DII)I"
            )
    )
    private int spellgems$redirectHotbarScroll(double wheel, int currentSlot, int slotCount) {
        if (WandClientInput.shouldCycleSpell(this.minecraft)) {
            int direction = (int) Math.signum(wheel);
            WandClientNetworking.sendCycle(direction);
            WandSpellHighlight.onCycled(this.minecraft, direction);
            return currentSlot;
        }

        return ScrollWheelHandler.getNextScrollWheelSelection(wheel, currentSlot, slotCount);
    }
}