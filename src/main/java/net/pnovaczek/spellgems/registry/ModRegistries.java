package net.pnovaczek.spellgems.registry;

import net.pnovaczek.spellgems.ModBlockEntities;
import net.pnovaczek.spellgems.ModBlocks;
import net.pnovaczek.spellgems.ModComponents;
import net.pnovaczek.spellgems.ModCreativeModeTabs;
import net.pnovaczek.spellgems.ModEntities;
import net.pnovaczek.spellgems.ModEntityDataSerializers;
import net.pnovaczek.spellgems.ModItems;
import net.pnovaczek.spellgems.ModMenuTypes;
import net.pnovaczek.spellgems.ModRecipeTypes;
import net.pnovaczek.spellgems.Spellgems;

/**
 * Single entry for content registration. Safe to call more than once (idempotent).
 * <p>
 * <b>Order (do not reorder without checking dependencies):</b>
 * <ol>
 *   <li>{@link ModComponents} — item default components</li>
 *   <li>{@link ModBlocks} — blocks only (no items)</li>
 *   <li>{@link ModItems} — items + block items (needs blocks + components)</li>
 *   <li>{@link ModBlockEntities} — needs blocks</li>
 *   <li>{@link ModEntities}</li>
 *   <li>{@link ModMenuTypes}</li>
 *   <li>{@link ModRecipeTypes}</li>
 *   <li>{@link ModCreativeModeTabs} — needs items/blocks</li>
 *   <li>{@link ModEntityDataSerializers}</li>
 * </ol>
 * Gameplay registries (spells, enchantment helpers) are registered separately
 * after content registries.
 */
public final class ModRegistries {
    private static boolean registered;

    private ModRegistries() {
    }

    public static void registerAll() {
        if (registered) {
            return;
        }
        registered = true;

        Spellgems.LOGGER.info("Registering Spellgems content");

        ModComponents.register();
        ModBlocks.register();
        ModItems.register();
        ModBlockEntities.register();
        ModEntities.register();
        ModMenuTypes.register();
        ModRecipeTypes.register();
        ModCreativeModeTabs.register();
        ModEntityDataSerializers.register();
    }

    public static boolean isRegistered() {
        return registered;
    }
}
