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

    public static final int WAND_QUICK_CAST_SLOT_COUNT = 9;

    public static KeyMapping CYCLE_SPELL_KEY;
    public static final KeyMapping[] WAND_QUICK_CAST_KEYS = new KeyMapping[WAND_QUICK_CAST_SLOT_COUNT];

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

        for (int slot = 0; slot < WAND_QUICK_CAST_SLOT_COUNT; slot++) {
            WAND_QUICK_CAST_KEYS[slot] = KeyMappingHelper.registerKeyMapping(
                    new KeyMapping(
                            "key.spellgems.wand_quick_cast." + (slot + 1),
                            InputConstants.Type.KEYSYM,
                            InputConstants.UNKNOWN.getValue(),
                            CATEGORY,
                            slot + 1
                    )
            );
        }
    }
}