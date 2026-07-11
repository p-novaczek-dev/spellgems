package net.pnovaczek.spellgems.client;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.pnovaczek.spellgems.ModComponents;
import net.pnovaczek.spellgems.ModItems;
import net.pnovaczek.spellgems.ModSpells;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.inventory.AstralBowContainer;
import net.pnovaczek.spellgems.inventory.WandContainer;
import net.pnovaczek.spellgems.item.SpellGemItem;
import net.pnovaczek.spellgems.item.SpellTomeItem;
import net.pnovaczek.spellgems.item.data.AstralBowData;
import net.pnovaczek.spellgems.item.data.SpellGemData;
import net.pnovaczek.spellgems.item.data.WandData;
import net.pnovaczek.spellgems.spell.Spell;
import net.pnovaczek.spellgems.spell.Spells;
import net.pnovaczek.spellgems.spell.enchantment.PotionEnchantment;
import net.pnovaczek.spellgems.wand.WandDepletion;
import net.pnovaczek.spellgems.wand.WandSpellLabels;

import java.util.List;
import java.util.function.BiConsumer;

public class SpellgemsTooltips {
    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipFlag, lines) -> {

            class LineAdder {
                void addLine(String key, ChatFormatting formatting) {
                    lines.add(Component.translatable(key).withStyle(formatting));
                }
                void addLine(MutableComponent component, ChatFormatting formatting) {
                    lines.add(component.withStyle(formatting));
                }
                void addLineHighlight(String key) { addLine(key, ChatFormatting.YELLOW); }
                void addLineAttribute(String key) { addLine(key, ChatFormatting.GRAY); }
                void addLineAttribute(MutableComponent component) { addLine(component, ChatFormatting.GRAY); }
                void addLineDetail(String key) {
                    lines.add(Component.literal(" ")
                            .append(Component.translatable(key).withStyle(ChatFormatting.DARK_GRAY))
                    );
                }
                void addLineHoldShift() { addLine("tooltip.spellgems.shift_hint", ChatFormatting.DARK_GRAY); }
            }

            LineAdder tooltip = new LineAdder();

            if (stack.is(ModItems.WAND) || stack.is(ModItems.ASTRAL_BOW)) {
                stripVanillaContainerLines(lines);
            }

            if (stack.is(ModItems.WAND)) {
                if (WandDepletion.isDepleted(stack)) {
                    tooltip.addLine("tooltip.spellgems.wand.depleted", ChatFormatting.RED);
                }
                if (Minecraft.getInstance().hasShiftDown()) {
                    appendEquippedGems(stack, WandContainer.SIZE, WandContainer::loadInto,
                            stack.getOrDefault(ModComponents.WAND_DATA, WandData.DEFAULT).selectedSlot(),
                            lines);
                    if (WandDepletion.isDepleted(stack)) {
                        tooltip.addLineDetail("tooltip.spellgems.wand.repair");
                    }
                    tooltip.addLineDetail("tooltip.spellgems.wand.configure");
                    tooltip.addLineDetail("tooltip.spellgems.wand.cast");
                    lines.add(Component.literal(" ")
                            .append(Component.translatable(
                                    "tooltip.spellgems.wand.cycle",
                                    KeyMapping.createNameSupplier(SpellgemsKeyMappings.CYCLE_SPELL_KEY.getName()).get()
                            ).withStyle(ChatFormatting.DARK_GRAY)));
                } else {
                    tooltip.addLineHoldShift();
                }
            }
            else if (stack.is(ModItems.ASTRAL_BOW)) {
                if (Minecraft.getInstance().hasShiftDown()) {
                    appendEquippedGems(stack, AstralBowContainer.SIZE, AstralBowContainer::loadInto,
                            stack.getOrDefault(ModComponents.ASTRAL_BOW_DATA, AstralBowData.DEFAULT).selectedSlot(),
                            lines);
                    tooltip.addLineDetail("tooltip.spellgems.astral_bow.astral_arrows");
                    tooltip.addLineDetail("tooltip.spellgems.astral_bow.configure");
                    lines.add(Component.literal(" ")
                            .append(Component.translatable(
                                    "tooltip.spellgems.astral_bow.cycle",
                                    KeyMapping.createNameSupplier(SpellgemsKeyMappings.CYCLE_SPELL_KEY.getName()).get()
                            ).withStyle(ChatFormatting.DARK_GRAY)));
                } else {
                    tooltip.addLineHoldShift();
                }
            }
            else if (stack.is(ModItems.RAW_SPELL_GEM)) {
                if (Minecraft.getInstance().hasShiftDown()) {
                    tooltip.addLineDetail("tooltip.spellgems.raw_spell_gem.description");
                } else {
                    tooltip.addLineHoldShift();
                }
            }
            else if (stack.is(ModItems.SPELL_TOME)) {
                var data = SpellTomeItem.getTomeData(stack);

                if (data != null && data.isEnchanted()) {
                    tooltip.addLineAttribute(data.tooltipNameKey());
                    if (Minecraft.getInstance().hasShiftDown()) {
                        tooltip.addLineDetail(data.tooltipDescriptionKey());
                    } else {
                        tooltip.addLineHoldShift();
                    }
                } else if (Minecraft.getInstance().hasShiftDown()) {
                    tooltip.addLineDetail("tooltip.spellgems.spell_tome.description");
                } else {
                    tooltip.addLineHoldShift();
                }
            }
            else if (stack.getItem() instanceof SpellGemItem) {
                var data = stack.getComponents().get(ModComponents.SPELL_GEM_DATA);

                if (data != null) {
                    Spell spell = ModSpells.get(data.spellId());

                    if (spell != null) {
                        boolean shiftDown = Minecraft.getInstance().hasShiftDown();

                        tooltip.addLineHighlight(spell.tooltipNameKey());

                        if (shiftDown) {
                            if (spell.id().equals(Spells.POTION)) {
                                if (data.potionEffects().isEmpty()) {
                                    tooltip.addLineDetail(spell.tooltipDescriptionKey());
                                }
                                tooltip.addLineDetail("tooltip.spellgems.spell_gem_potion.astral_bow");
                            } else {
                                tooltip.addLineDetail(spell.tooltipDescriptionKey());
                            }
                        } else {
                            tooltip.addLineHoldShift();
                        }

                        for (var effect : data.modifierEffects()) {
                            tooltip.addLineAttribute(effect.tooltipNameKey());
                            if (shiftDown) {
                                tooltip.addLineDetail(effect.tooltipDescriptionKey());
                            }
                        }

                        for (var effect : data.strikeEffects()) {
                            tooltip.addLineAttribute(effect.tooltipNameKey());
                            if (shiftDown) {
                                tooltip.addLineDetail(effect.tooltipDescriptionKey());
                            }
                        }

                        for (var effect : data.utilityEffects()) {
                            tooltip.addLineAttribute(effect.tooltipNameKey());
                            if (shiftDown) {
                                tooltip.addLineDetail(effect.tooltipDescriptionKey());
                            }
                        }

                        for (PotionEnchantment enchantment : data.potionEffects()) {
                            tooltip.addLineAttribute(enchantment.displayName().copy());
                            if (shiftDown) {
                                PotionContents.addPotionTooltip(
                                        enchantment.contents().getAllEffects(),
                                        lines::add,
                                        enchantment.durationScale(),
                                        tooltipContext.tickRate()
                                );
                            }
                        }
                    }
                }
            }
        });
    }

    private static void stripVanillaContainerLines(List<Component> lines) {
        lines.removeIf(SpellgemsTooltips::isVanillaContainerLine);
    }

    private static boolean isVanillaContainerLine(Component line) {
        if (line.getContents() instanceof TranslatableContents translatable) {
            String key = translatable.getKey();
            return "item.container.item_count".equals(key) || "item.container.more_items".equals(key);
        }
        return false;
    }

    private static void appendEquippedGems(
            ItemStack containerItem,
            int slotCount,
            BiConsumer<SimpleContainer, ItemStack> loader,
            int selectedSlot,
            List<Component> lines
    ) {
        SimpleContainer slots = new SimpleContainer(slotCount);
        loader.accept(slots, containerItem);

        int selected = Mth.clamp(selectedSlot, 0, slotCount - 1);
        boolean anyGems = false;

        for (int i = 0; i < slotCount; i++) {
            ItemStack gemStack = slots.getItem(i);
            if (gemStack.isEmpty()) {
                continue;
            }

            SpellGemData data = SpellGemItem.getSpellData(gemStack);
            if (data == null) {
                continue;
            }

            anyGems = true;
            Component gemLine = WandSpellLabels.formatSelection(data);
            if (i == selected) {
                gemLine = Component.literal("> ").append(gemLine);
            } else {
                gemLine = Component.literal("  ").append(gemLine);
            }
            lines.add(Component.literal(" ").append(gemLine.copy().withStyle(ChatFormatting.GRAY)));
        }

        if (!anyGems) {
            lines.add(Component.translatable("tooltip.spellgems.equipped_gems.empty")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}