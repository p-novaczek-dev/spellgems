package net.pnovaczek.spellgems;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.pnovaczek.spellgems.item.AstralBowItem;
import net.pnovaczek.spellgems.item.SpellGemItem;
import net.pnovaczek.spellgems.item.SpellTomeItem;
import net.pnovaczek.spellgems.item.WandItem;
import net.pnovaczek.spellgems.item.data.SpellGemData;
import net.pnovaczek.spellgems.item.data.TomeData;
import net.pnovaczek.spellgems.spell.Spells;

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

    public static final WandItem WAND = register(
            "wand",
            WandItem::new,
            new Item.Properties().durability(384).stacksTo(1).repairable(ModItems.SHIMMERSTEEL_INGOT)
    );

    public static final AstralBowItem ASTRAL_BOW = register(
            "astral_bow",
            AstralBowItem::new,
            new Item.Properties().durability(384)
    );

    public static final SpellGemItem SPELL_GEM_PROJECTILE = register(
            "spell_gem_projectile",
            SpellGemItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .component(ModComponents.SPELL_GEM_DATA, SpellGemData.create(Spells.PROJECTILE))
    );

    public static final SpellGemItem SPELL_GEM_NOVA = register(
            "spell_gem_nova",
            SpellGemItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .component(ModComponents.SPELL_GEM_DATA, SpellGemData.create(Spells.NOVA))
    );

    public static final SpellGemItem SPELL_GEM_VORTEX = register(
            "spell_gem_vortex",
            SpellGemItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .component(ModComponents.SPELL_GEM_DATA, SpellGemData.create(Spells.VORTEX))
    );

    public static final SpellGemItem SPELL_GEM_POTION = register(
            "spell_gem_potion",
            SpellGemItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .component(ModComponents.SPELL_GEM_DATA, SpellGemData.create(Spells.POTION))
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