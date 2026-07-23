package net.pnovaczek.spellgems.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.platform.client.ClientPlatform;
import org.lwjgl.glfw.GLFW;

public final class SpellgemsKeyMappings {

    public static final int WAND_QUICK_CAST_SLOT_COUNT = 9;

    public static KeyMapping.Category CATEGORY;
    public static KeyMapping CYCLE_SPELL_KEY;
    public static final KeyMapping[] WAND_QUICK_CAST_KEYS = new KeyMapping[WAND_QUICK_CAST_SLOT_COUNT];

    private SpellgemsKeyMappings() {
    }

    public static void register() {
        // Fabric: Category.register(Identifier). NeoForge: RegisterKeyMappingsEvent#registerCategory.
        CATEGORY = ClientPlatform.client().registerKeyCategory(
                Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "spellgems")
        );

        CYCLE_SPELL_KEY = ClientPlatform.client().registerKeyMapping(
                new KeyMapping(
                        "key.spellgems.cycle_spell",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_V,
                        CATEGORY
                )
        );

        for (int slot = 0; slot < WAND_QUICK_CAST_SLOT_COUNT; slot++) {
            WAND_QUICK_CAST_KEYS[slot] = ClientPlatform.client().registerKeyMapping(
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
