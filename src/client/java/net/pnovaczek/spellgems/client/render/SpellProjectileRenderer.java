package net.pnovaczek.spellgems.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;   // ← NEW import
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.pnovaczek.spellgems.entity.SpellProjectile;

public class SpellProjectileRenderer extends EntityRenderer<SpellProjectile, SpellProjectileRenderState> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("spellgems", "textures/entity/projectiles/spell_projectile.png");

    public SpellProjectileRenderer(final Context context) {
        super(context);
        this.shadowRadius = 0F;
        this.shadowStrength = 0F;
    }

    @Override
    public void extractRenderState(SpellProjectile entity, SpellProjectileRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.tintColor = entity.getTintColor();
    }

    @Override
    public SpellProjectileRenderState createRenderState() {
        return new SpellProjectileRenderState();
    }

    @Override
    public void submit(
            final SpellProjectileRenderState state,
            final PoseStack poseStack,
            final SubmitNodeCollector submitNodeCollector,
            final CameraRenderState camera
    ) {
        poseStack.pushPose();

        poseStack.mulPose(camera.orientation);
        poseStack.translate(0.0F, 0.0F, 0.05F);

        float scale = 1.0F;
        poseStack.scale(scale, scale, scale);

        submitNodeCollector.submitCustomGeometry(
                poseStack,
                RenderTypes.entityCutout(TEXTURE),
                (pose, buffer) -> {

                    buffer.addVertex(pose.pose(), -0.5F, -0.5F, 0.0F)
                            .setColor(state.tintColor)
                            .setUv(0.0F, 1.0F)
                            .setOverlay(OverlayTexture.NO_OVERLAY)
                            .setLight(state.lightCoords)
                            .setNormal(pose, 0.0F, 0.0F, 1.0F);

                    buffer.addVertex(pose.pose(), 0.5F, -0.5F, 0.0F)
                            .setColor(state.tintColor)
                            .setUv(1.0F, 1.0F)
                            .setOverlay(OverlayTexture.NO_OVERLAY)
                            .setLight(state.lightCoords)
                            .setNormal(pose, 0.0F, 0.0F, 1.0F);

                    buffer.addVertex(pose.pose(), 0.5F, 0.5F, 0.0F)
                            .setColor(state.tintColor)
                            .setUv(1.0F, 0.0F)
                            .setOverlay(OverlayTexture.NO_OVERLAY)
                            .setLight(state.lightCoords)
                            .setNormal(pose, 0.0F, 0.0F, 1.0F);

                    buffer.addVertex(pose.pose(), -0.5F, 0.5F, 0.0F)
                            .setColor(state.tintColor)
                            .setUv(0.0F, 0.0F)
                            .setOverlay(OverlayTexture.NO_OVERLAY)
                            .setLight(state.lightCoords)
                            .setNormal(pose, 0.0F, 0.0F, 1.0F);
                });

        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }
}