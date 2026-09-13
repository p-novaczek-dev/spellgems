package net.pnovaczek.spellgems.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.entity.SpellProjectile;

public class SpellProjectileRenderer extends EntityRenderer<SpellProjectile, SpellProjectileRenderState> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("spellgems", "textures/entity/projectiles/spell_projectile.png");
    private static final RenderType RENDER_TYPE = RenderTypes.entityCutout(TEXTURE);

    public SpellProjectileRenderer(final Context context) {
        super(context);
        this.shadowRadius = 0F;
        this.shadowStrength = 0F;
    }

    @Override
    protected int getBlockLightLevel(SpellProjectile entity, BlockPos blockPos) {
        return 15;
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

        // Tint is stored as RGB; vertex color is ARGB. Alpha 0 is discarded by shader packs.
        int tint = 0xFF000000 | (state.tintColor & 0xFFFFFF);
        submitNodeCollector.submitCustomGeometry(poseStack, RENDER_TYPE, (pose, buffer) -> {
            vertex(buffer, pose, state.lightCoords, tint, -0.5F, -0.5F, 0.0F, 1.0F);
            vertex(buffer, pose, state.lightCoords, tint, 0.5F, -0.5F, 1.0F, 1.0F);
            vertex(buffer, pose, state.lightCoords, tint, 0.5F, 0.5F, 1.0F, 0.0F);
            vertex(buffer, pose, state.lightCoords, tint, -0.5F, 0.5F, 0.0F, 0.0F);
        });

        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    private static void vertex(
            VertexConsumer builder,
            PoseStack.Pose pose,
            int lightCoords,
            int color,
            float x,
            float y,
            float u,
            float v
    ) {
        builder.addVertex(pose, x, y, 0.0F)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
