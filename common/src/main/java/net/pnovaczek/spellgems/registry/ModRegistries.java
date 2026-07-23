package net.pnovaczek.spellgems.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
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
import net.pnovaczek.spellgems.platform.Platform;

import java.util.HashSet;
import java.util.Set;

/**
 * Content registration for both loaders.
 * <p>
 * <b>Fabric:</b> {@link #registerAll()} once at mod init (immediate {@code BuiltInRegistries}).
 * <p>
 * <b>NeoForge:</b> {@link #registerFor(ResourceKey)} once per registry from {@code RegisterEvent}.
 * <p>
 * Dependency order when using {@link #registerAll()}:
 * <ol>
 *   <li>{@link ModComponents}</li>
 *   <li>{@link ModBlocks}</li>
 *   <li>{@link ModItems}</li>
 *   <li>{@link ModBlockEntities}</li>
 *   <li>{@link ModEntities}</li>
 *   <li>{@link ModMenuTypes}</li>
 *   <li>{@link ModRecipeTypes} (types + serializers)</li>
 *   <li>{@link ModCreativeModeTabs}</li>
 *   <li>{@link ModEntityDataSerializers}</li>
 * </ol>
 */
public final class ModRegistries {
    private static final Set<String> DONE = new HashSet<>();
    private static boolean allRegistered;

    private ModRegistries() {
    }

    /**
     * Immediate full registration (Fabric / datagen).
     */
    public static void registerAll() {
        if (allRegistered) {
            return;
        }
        Spellgems.LOGGER.info("Registering Spellgems content (immediate)");
        registerOnce("data_component_type", ModComponents::register);
        registerOnce("block", ModBlocks::register);
        registerOnce("item", ModItems::register);
        registerOnce("block_entity_type", ModBlockEntities::register);
        registerOnce("entity_type", ModEntities::register);
        registerOnce("menu", ModMenuTypes::register);
        registerOnce("recipe_type", ModRecipeTypes::registerTypes);
        registerOnce("recipe_serializer", ModRecipeTypes::registerSerializers);
        registerOnce("creative_mode_tab", ModCreativeModeTabs::register);
        registerOnce("entity_data_serializer", ModEntityDataSerializers::register);
        allRegistered = true;
    }

    /**
     * NeoForge {@code RegisterEvent} dispatch — registers only the matching registry slice.
     */
    public static void registerFor(ResourceKey<? extends Registry<?>> registryKey) {
        if (registryKey.equals(Registries.DATA_COMPONENT_TYPE)) {
            registerOnce("data_component_type", ModComponents::register);
        } else if (registryKey.equals(Registries.BLOCK)) {
            registerOnce("block", ModBlocks::register);
        } else if (registryKey.equals(Registries.ITEM)) {
            registerOnce("item", ModItems::register);
        } else if (registryKey.equals(Registries.BLOCK_ENTITY_TYPE)) {
            registerOnce("block_entity_type", ModBlockEntities::register);
        } else if (registryKey.equals(Registries.ENTITY_TYPE)) {
            registerOnce("entity_type", ModEntities::register);
        } else if (registryKey.equals(Registries.MENU)) {
            registerOnce("menu", ModMenuTypes::register);
        } else if (registryKey.equals(Registries.RECIPE_TYPE)) {
            registerOnce("recipe_type", ModRecipeTypes::registerTypes);
        } else if (registryKey.equals(Registries.RECIPE_SERIALIZER)) {
            registerOnce("recipe_serializer", ModRecipeTypes::registerSerializers);
        } else if (registryKey.equals(Registries.CREATIVE_MODE_TAB)) {
            registerOnce("creative_mode_tab", ModCreativeModeTabs::register);
        } else {
            var edsKey = Platform.registries().entityDataSerializerRegistryKey();
            if (edsKey != null && registryKey.equals(edsKey)) {
                registerOnce("entity_data_serializer", ModEntityDataSerializers::register);
            }
        }

        if (DONE.contains("data_component_type")
                && DONE.contains("block")
                && DONE.contains("item")
                && DONE.contains("block_entity_type")
                && DONE.contains("entity_type")
                && DONE.contains("menu")
                && DONE.contains("recipe_type")
                && DONE.contains("recipe_serializer")
                && DONE.contains("creative_mode_tab")
                && DONE.contains("entity_data_serializer")) {
            allRegistered = true;
        }
    }

    private static void registerOnce(String id, Runnable action) {
        if (!DONE.add(id)) {
            return;
        }
        Spellgems.LOGGER.debug("Registering Spellgems content slice: {}", id);
        action.run();
    }

    public static boolean isRegistered() {
        return allRegistered;
    }
}
