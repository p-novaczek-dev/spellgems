package net.pnovaczek.spellgems.client.render;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.entity.AstralArrow;
import org.jspecify.annotations.NonNull;

public class AstralArrowRenderer extends ArrowRenderer<AstralArrow, ArrowRenderState> {
    public static final Identifier ASTRAL_ARROW_LOCATION = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID,"textures/entity/projectiles/arrow_astral.png");

    public AstralArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected @NonNull Identifier getTextureLocation(final @NonNull ArrowRenderState state) {
        return ASTRAL_ARROW_LOCATION;
    }

    public @NonNull ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }
}