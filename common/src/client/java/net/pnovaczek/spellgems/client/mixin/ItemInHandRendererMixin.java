package net.pnovaczek.spellgems.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.pnovaczek.spellgems.ModItems;
import net.pnovaczek.spellgems.item.AstralBowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Shared Fabric + NeoForge: astral bow custom draw duration (vanilla bow uses 20 ticks).
 * Re-check {@code floatValue = 20.0F} if the first-person arm submit method changes across MC versions.
 */
@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class ItemInHandRendererMixin {

    @ModifyConstant(method = "submitArmWithItem", constant = @Constant(floatValue = 20.0F))
    private float spellgems$astralBowDrawDuration(
            float drawDuration,
            PlayerRenderState playerState,
            FirstPersonHandsAndItemsRenderState handsState,
            float partialTick,
            float pitch,
            InteractionHand hand,
            float swingProgress,
            ItemStack itemStack
    ) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && itemStack.is(ModItems.ASTRAL_BOW)) {
            return AstralBowItem.getDrawDurationTicks(itemStack, player);
        }

        return drawDuration;
    }
}