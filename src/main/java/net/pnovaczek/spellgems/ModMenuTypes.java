package net.pnovaczek.spellgems;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.pnovaczek.spellgems.screen.ManaInfuserMenu;
import net.pnovaczek.spellgems.screen.SpellEnchantingMenu;

public class ModMenuTypes {

    public static final MenuType<ManaInfuserMenu> MANA_INFUSER = register(
            "mana_infuser",
            ManaInfuserMenu::new
    );

    public static final MenuType<SpellEnchantingMenu> SPELL_ENCHANTING_TABLE = register(
            "spell_enchanting_table",
            SpellEnchantingMenu::new
    );

    private static <T extends net.minecraft.world.inventory.AbstractContainerMenu> MenuType<T> register(
            String name, MenuType.MenuSupplier<T> factory) {
        ResourceKey<MenuType<?>> key = ResourceKey.create(Registries.MENU,
                Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, name));
        MenuType<T> type = new MenuType<>(factory, net.minecraft.world.flag.FeatureFlags.VANILLA_SET);
        return Registry.register(BuiltInRegistries.MENU, key, type);
    }

    public static void initialize() {
        // forces static initialization
    }
}