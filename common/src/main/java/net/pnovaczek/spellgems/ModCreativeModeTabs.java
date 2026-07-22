package net.pnovaczek.spellgems;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.pnovaczek.spellgems.platform.Platform;
import net.pnovaczek.spellgems.registry.ModRegistry;
import net.pnovaczek.spellgems.wand.WandDepletion;

/**
 * Creative tabs. Requires items/blocks already registered.
 */
public class ModCreativeModeTabs {

    public static final ResourceKey<CreativeModeTab> SPELLGEMS = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            ModRegistry.id("spellgems")
    );

    private ModCreativeModeTabs() {
    }

    public static void register() {
        ModRegistry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                SPELLGEMS,
                Platform.registries().creativeTabBuilder()
                        .title(Component.translatable("itemGroup.spellgems"))
                        .icon(() -> new ItemStack(ModItems.RAW_SPELL_GEM))
                        .displayItems((parameters, output) -> {
                            output.accept(ModBlocks.MANA_INFUSER);
                            output.accept(ModBlocks.SPELL_ENCHANTING_TABLE);
                            output.accept(ModBlocks.SPELL_DISPENSER);
                            output.accept(ModItems.MANA_ROOT);
                            output.accept(ModItems.MANA_ESSENCE);
                            output.accept(ModItems.SHIMMERSTEEL_INGOT);
                            output.accept(ModItems.RAW_SPELL_GEM);
                            output.accept(ModItems.WAND);
                            // Pre-enchanted book for the Recharge enchantment.
                            // Only available in creative mode / via cheats; intentionally no crafting recipe
                            // or spell enchanting table recipe.
                            parameters.holders()
                                    .lookup(Registries.ENCHANTMENT)
                                    .flatMap(lookup -> lookup.get(WandDepletion.RECHARGE))
                                    .ifPresent(recharge -> {
                                        ItemStack rechargeBook = EnchantmentHelper.createBook(
                                                new EnchantmentInstance(recharge, 1));
                                        output.accept(rechargeBook);
                                    });
                            output.accept(ModItems.ASTRAL_BOW);
                            output.accept(ModItems.SPELL_GEM_PROJECTILE);
                            output.accept(ModItems.SPELL_GEM_NOVA);
                            output.accept(ModItems.SPELL_GEM_VORTEX);
                            output.accept(ModItems.SPELL_GEM_BLINK);
                            output.accept(ModItems.SPELL_GEM_WIND_CHARGE);
                            output.accept(ModItems.SPELL_GEM_MAGNET);
                            output.accept(ModItems.SPELL_GEM_PLACE_BLOCK);
                            output.accept(ModItems.SPELL_GEM_BREAK_BLOCK);
                            output.accept(ModItems.SPELL_GEM_HARVEST);
                            output.accept(ModItems.SPELL_GEM_PLANT);
                            output.accept(ModItems.SPELL_GEM_FEED);
                            output.accept(ModItems.SPELL_GEM_GROW);
                            output.accept(ModItems.SPELL_GEM_POTION);
                        })
                        .build()
        );
    }
}
