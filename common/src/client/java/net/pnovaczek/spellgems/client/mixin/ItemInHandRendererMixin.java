package net.pnovaczek.spellgems.client.mixin;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
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
 * Re-check {@code floatValue = 20.0F} if {@code renderArmWithItem} changes across MC versions.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @ModifyConstant(method = "renderArmWithItem", constant = @Constant(floatValue = 20.0F))
    private float spellgems$astralBowDrawDuration(
            float drawDuration,
            AbstractClientPlayer player,
            float frameInterp,
            float xRot,
            InteractionHand hand,
            float attack,
            ItemStack itemStack,
            float inverseArmHeight,
            com.mojang.blaze3d.vertex.PoseStack poseStack,
            net.minecraft.client.renderer.SubmitNodeCollector submitNodeCollector,
            int lightCoords
    ) {
        if (itemStack.is(ModItems.ASTRAL_BOW)) {
            return AstralBowItem.getDrawDurationTicks(itemStack, player);
        }

        return drawDuration;
    }
}