package net.pnovaczek.spellgems.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.Spellgems;
import org.lwjgl.glfw.GLFW;

public final class SpellgemsKeyMappings {

    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "spellgems")
    );

    public static KeyMapping CYCLE_SPELL_KEY;

    private SpellgemsKeyMappings() {
    }

    public static void register() {
        CYCLE_SPELL_KEY = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.spellgems.cycle_spell",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_V,
                        CATEGORY
                )
        );
    }
}