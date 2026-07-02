package net.pnovaczek.spellgems;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeModeTabs {

    public static final ResourceKey<CreativeModeTab> SPELLGEMS = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "spellgems")
    );

    public static void initialize() {
        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                SPELLGEMS,
                FabricCreativeModeTab.builder()
                        .title(Component.translatable("itemGroup.spellgems"))
                        .icon(() -> new ItemStack(ModItems.RAW_SPELL_GEM))
                        .displayItems((parameters, output) -> {
                            output.accept(ModBlocks.MANA_INFUSER);
                            output.accept(ModBlocks.SPELL_ENCHANTING_TABLE);
                            output.accept(ModItems.MANA_ROOT);
                            output.accept(ModItems.MANA_ESSENCE);
                            output.accept(ModItems.SHIMMERSTEEL_INGOT);
                            output.accept(ModItems.RAW_SPELL_GEM);
                            output.accept(ModItems.WAND);
                            output.accept(ModItems.ASTRAL_BOW);
                            output.accept(ModItems.SPELL_GEM_PROJECTILE);
                            output.accept(ModItems.SPELL_GEM_NOVA);
                            output.accept(ModItems.SPELL_GEM_VORTEX);
                        })
                        .build()
        );
    }
}