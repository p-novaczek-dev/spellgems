package net.pnovaczek.spellgems;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
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
import net.pnovaczek.spellgems.registry.ModRegistry;
import net.pnovaczek.spellgems.spell.SpellIds;

import java.util.function.Function;

/**
 * Items and block items. Requires {@link ModComponents} and {@link ModBlocks} first.
 * Fields are assigned in {@link #register()} (not class-init).
 */
public class ModItems {

    public static Item MANA_ESSENCE;
    public static Item SHIMMERSTEEL_INGOT;
    public static Item RAW_SPELL_GEM;
    public static BlockItem MANA_ROOT;
    public static BlockItem MANA_INFUSER;
    public static BlockItem SPELL_ENCHANTING_TABLE;
    public static BlockItem SPELL_DISPENSER;

    public static WandItem WAND;
    public static AstralBowItem ASTRAL_BOW;

    public static SpellGemItem SPELL_GEM_PROJECTILE;
    public static SpellGemItem SPELL_GEM_NOVA;
    public static SpellGemItem SPELL_GEM_VORTEX;
    public static SpellGemItem SPELL_GEM_BLINK;
    public static SpellGemItem SPELL_GEM_WIND_CHARGE;
    public static SpellGemItem SPELL_GEM_MAGNET;
    public static SpellGemItem SPELL_GEM_PLACE_BLOCK;
    public static SpellGemItem SPELL_GEM_BREAK_BLOCK;
    public static SpellGemItem SPELL_GEM_PLANT;
    public static SpellGemItem SPELL_GEM_HARVEST;
    public static SpellGemItem SPELL_GEM_FEED;
    public static SpellGemItem SPELL_GEM_GROW;
    public static SpellGemItem SPELL_GEM_POTION;

    public static SpellTomeItem SPELL_TOME;

    private static final TooltipDisplay HIDE_CONTAINER_TOOLTIP =
            TooltipDisplay.DEFAULT.withHidden(DataComponents.CONTAINER, true);

    private ModItems() {
    }

    public static void register() {
        MANA_ESSENCE = register("mana_essence", Item::new, new Item.Properties());
        SHIMMERSTEEL_INGOT = register("shimmersteel_ingot", Item::new, new Item.Properties());
        RAW_SPELL_GEM = register("raw_spell_gem", Item::new, new Item.Properties());

        MANA_ROOT = register(
                "mana_root",
                p -> new BlockItem(ModBlocks.MANA_ROOT, p),
                new Item.Properties()
        );
        MANA_INFUSER = registerBlockItem("mana_infuser", ModBlocks.MANA_INFUSER);
        SPELL_ENCHANTING_TABLE = registerBlockItem("spell_enchanting_table", ModBlocks.SPELL_ENCHANTING_TABLE);
        SPELL_DISPENSER = registerBlockItem("spell_dispenser", ModBlocks.SPELL_DISPENSER);

        WAND = register(
                "wand",
                WandItem::new,
                new Item.Properties()
                        .durability(256)
                        .stacksTo(1)
                        .enchantable(15)
                        .repairable(SHIMMERSTEEL_INGOT)
                        .component(DataComponents.TOOLTIP_DISPLAY, HIDE_CONTAINER_TOOLTIP)
        );

        ASTRAL_BOW = register(
                "astral_bow",
                AstralBowItem::new,
                new Item.Properties()
                        .durability(1561)
                        .enchantable(15)
                        .repairable(SHIMMERSTEEL_INGOT)
                        .component(DataComponents.TOOLTIP_DISPLAY, HIDE_CONTAINER_TOOLTIP)
        );

        SPELL_GEM_PROJECTILE = spellGem("spell_gem_projectile", SpellIds.PROJECTILE);
        SPELL_GEM_NOVA = spellGem("spell_gem_nova", SpellIds.NOVA);
        SPELL_GEM_VORTEX = spellGem("spell_gem_vortex", SpellIds.VORTEX);
        SPELL_GEM_BLINK = spellGem("spell_gem_blink", SpellIds.BLINK);
        SPELL_GEM_WIND_CHARGE = spellGem("spell_gem_wind_charge", SpellIds.WIND_CHARGE);
        SPELL_GEM_MAGNET = spellGem("spell_gem_magnet", SpellIds.MAGNET);
        SPELL_GEM_PLACE_BLOCK = spellGem("spell_gem_place_block", SpellIds.PLACE_BLOCK);
        SPELL_GEM_BREAK_BLOCK = spellGem("spell_gem_break_block", SpellIds.BREAK_BLOCK);
        SPELL_GEM_PLANT = spellGem("spell_gem_plant", SpellIds.PLANT);
        SPELL_GEM_HARVEST = spellGem("spell_gem_harvest", SpellIds.HARVEST);
        SPELL_GEM_FEED = spellGem("spell_gem_feed", SpellIds.FEED);
        SPELL_GEM_GROW = spellGem("spell_gem_grow", SpellIds.GROW);
        SPELL_GEM_POTION = spellGem("spell_gem_potion", SpellIds.POTION);

        SPELL_TOME = register(
                "spell_tome",
                SpellTomeItem::new,
                new Item.Properties()
                        .stacksTo(1)
                        .component(ModComponents.TOME_DATA, TomeData.create())
        );
    }

    private static SpellGemItem spellGem(String name, net.minecraft.resources.Identifier spellId) {
        return register(
                name,
                SpellGemItem::new,
                new Item.Properties()
                        .stacksTo(1)
                        .component(ModComponents.SPELL_GEM_DATA, SpellGemData.create(spellId))
        );
    }

    private static BlockItem registerBlockItem(String name, net.minecraft.world.level.block.Block block) {
        return register(
                name,
                p -> new BlockItem(block, p),
                new Item.Properties().useBlockDescriptionPrefix()
        );
    }

    private static <T extends Item> T register(String name, Function<Item.Properties, T> itemFactory, Item.Properties properties) {
        ResourceKey<Item> itemKey = keyOfItem(name);
        T item = itemFactory.apply(properties.setId(itemKey));
        return ModRegistry.register(BuiltInRegistries.ITEM, itemKey, item);
    }

    public static ResourceKey<Item> keyOfItem(String name) {
        return ResourceKey.create(Registries.ITEM, ModRegistry.id(name));
    }
}
