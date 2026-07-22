package net.pnovaczek.spellgems;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.pnovaczek.spellgems.registry.ModRegistry;
import net.pnovaczek.spellgems.screen.AstralBowMenu;
import net.pnovaczek.spellgems.screen.ManaInfuserMenu;
import net.pnovaczek.spellgems.screen.SpellDispenserMenu;
import net.pnovaczek.spellgems.screen.SpellEnchantingMenu;
import net.pnovaczek.spellgems.screen.WandMenu;

/**
 * Menu types. Fields are assigned in {@link #register()} (not class-init).
 */
public class ModMenuTypes {

    public static MenuType<ManaInfuserMenu> MANA_INFUSER;
    public static MenuType<SpellEnchantingMenu> SPELL_ENCHANTING_TABLE;
    public static MenuType<SpellDispenserMenu> SPELL_DISPENSER;
    public static MenuType<WandMenu> WAND;
    public static MenuType<AstralBowMenu> ASTRAL_BOW;

    private ModMenuTypes() {
    }

    public static void register() {
        MANA_INFUSER = register("mana_infuser", ManaInfuserMenu::new);
        SPELL_ENCHANTING_TABLE = register("spell_enchanting_table", SpellEnchantingMenu::new);
        SPELL_DISPENSER = register("spell_dispenser", SpellDispenserMenu::new);
        WAND = register("wand", WandMenu::new);
        ASTRAL_BOW = register("astral_bow", AstralBowMenu::new);
    }

    private static <T extends net.minecraft.world.inventory.AbstractContainerMenu> MenuType<T> register(
            String name,
            MenuType.MenuSupplier<T> factory
    ) {
        ResourceKey<MenuType<?>> key = ResourceKey.create(Registries.MENU, ModRegistry.id(name));
        MenuType<T> type = new MenuType<>(factory, FeatureFlags.VANILLA_SET);
        return ModRegistry.register(BuiltInRegistries.MENU, key, type);
    }
}
