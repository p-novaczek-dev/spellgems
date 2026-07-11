package net.pnovaczek.spellgems;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.TooltipDisplay;
import net.pnovaczek.spellgems.item.AstralBowItem;
import net.pnovaczek.spellgems.item.SpellGemItem;
import net.pnovaczek.spellgems.item.SpellTomeItem;
import net.pnovaczek.spellgems.item.WandItem;
import net.pnovaczek.spellgems.item.data.SpellGemData;
import net.pnovaczek.spellgems.item.data.TomeData;
import net.pnovaczek.spellgems.spell.SpellIds;

import java.util.function.Function;

public class ModItems {

    public static final Item MANA_ESSENCE = register(
            "mana_essence",
            Item::new,
            new Item.Properties());

    public static final Item SHIMMERSTEEL_INGOT = register(
            "shimmersteel_ingot",
            Item::new,
            new Item.Properties());

    public static final Item RAW_SPELL_GEM = register(
            "raw_spell_gem",
            Item::new,
            new Item.Properties());

    public static final BlockItem MANA_ROOT = register(
            "mana_root",
            p -> new BlockItem(ModBlocks.MANA_ROOT, p),
            new Item.Properties());

    private static final TooltipDisplay HIDE_CONTAINER_TOOLTIP =
            TooltipDisplay.DEFAULT.withHidden(DataComponents.CONTAINER, true);

    public static final WandItem WAND = register(
            "wand",
            WandItem::new,
            new Item.Properties()
                    .durability(384)
                    .stacksTo(1)
                    .enchantable(15)
                    .repairable(ModItems.SHIMMERSTEEL_INGOT)
                    .component(DataComponents.TOOLTIP_DISPLAY, HIDE_CONTAINER_TOOLTIP)
    );

    public static final AstralBowItem ASTRAL_BOW = register(
            "astral_bow",
            AstralBowItem::new,
            new Item.Properties()
                    .durability(1561)
                    .enchantable(15)
                    .repairable(ModItems.SHIMMERSTEEL_INGOT)
                    .component(DataComponents.TOOLTIP_DISPLAY, HIDE_CONTAINER_TOOLTIP)
    );

    public static final SpellGemItem SPELL_GEM_PROJECTILE = register(
            "spell_gem_projectile",
            SpellGemItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .component(ModComponents.SPELL_GEM_DATA, SpellGemData.create(SpellIds.PROJECTILE))
    );

    public static final SpellGemItem SPELL_GEM_NOVA = register(
            "spell_gem_nova",
            SpellGemItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .component(ModComponents.SPELL_GEM_DATA, SpellGemData.create(SpellIds.NOVA))
    );

    public static final SpellGemItem SPELL_GEM_VORTEX = register(
            "spell_gem_vortex",
            SpellGemItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .component(ModComponents.SPELL_GEM_DATA, SpellGemData.create(SpellIds.VORTEX))
    );

    public static final SpellGemItem SPELL_GEM_BLINK = register(
            "spell_gem_blink",
            SpellGemItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .component(ModComponents.SPELL_GEM_DATA, SpellGemData.create(SpellIds.BLINK))
    );

    public static final SpellGemItem SPELL_GEM_WIND_CHARGE = register(
            "spell_gem_wind_charge",
            SpellGemItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .component(ModComponents.SPELL_GEM_DATA, SpellGemData.create(SpellIds.WIND_CHARGE))
    );

    public static final SpellGemItem SPELL_GEM_MAGNET = register(
            "spell_gem_magnet",
            SpellGemItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .component(ModComponents.SPELL_GEM_DATA, SpellGemData.create(SpellIds.MAGNET))
    );

    public static final SpellGemItem SPELL_GEM_PLACE_BLOCK = register(
            "spell_gem_place_block",
            SpellGemItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .component(ModComponents.SPELL_GEM_DATA, SpellGemData.create(SpellIds.PLACE_BLOCK))
    );

    public static final SpellGemItem SPELL_GEM_BREAK_BLOCK = register(
            "spell_gem_break_block",
            SpellGemItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .component(ModComponents.SPELL_GEM_DATA, SpellGemData.create(SpellIds.BREAK_BLOCK))
    );

    public static final SpellGemItem SPELL_GEM_PLANT = register(
            "spell_gem_plant",
            SpellGemItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .component(ModComponents.SPELL_GEM_DATA, SpellGemData.create(SpellIds.PLANT))
    );

    public static final SpellGemItem SPELL_GEM_HARVEST = register(
            "spell_gem_harvest",
            SpellGemItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .component(ModComponents.SPELL_GEM_DATA, SpellGemData.create(SpellIds.HARVEST))
    );

    public static final SpellGemItem SPELL_GEM_FEED = register(
            "spell_gem_feed",
            SpellGemItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .component(ModComponents.SPELL_GEM_DATA, SpellGemData.create(SpellIds.FEED))
    );

    public static final SpellGemItem SPELL_GEM_GROW = register(
            "spell_gem_grow",
            SpellGemItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .component(ModComponents.SPELL_GEM_DATA, SpellGemData.create(SpellIds.GROW))
    );

    public static final SpellGemItem SPELL_GEM_POTION = register(
            "spell_gem_potion",
            SpellGemItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .component(ModComponents.SPELL_GEM_DATA, SpellGemData.create(SpellIds.POTION))
    );

    public static final SpellTomeItem SPELL_TOME = register(
            "spell_tome",
            SpellTomeItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .component(ModComponents.TOME_DATA, TomeData.create())
    );

    private static <T extends Item> T register(String name, Function<Item.Properties, T> itemFactory, Item.Properties properties) {
        ResourceKey<Item> itemKey = keyOfItem(name);
        T item = itemFactory.apply(properties.setId(itemKey));
        return Registry.register(BuiltInRegistries.ITEM, itemKey, item);
    }

    public static ResourceKey<Item> keyOfItem(String name) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, name));
    }

    public static void initialize() {
        // forces static initialization
    }
}