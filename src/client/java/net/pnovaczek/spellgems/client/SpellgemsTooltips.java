package net.pnovaczek.spellgems.client;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.pnovaczek.spellgems.ModComponents;
import net.pnovaczek.spellgems.ModItems;
import net.pnovaczek.spellgems.ModSpells;
import net.pnovaczek.spellgems.item.SpellGemItem;
import net.pnovaczek.spellgems.item.SpellTomeItem;
import net.pnovaczek.spellgems.spell.Spell;

public class SpellgemsTooltips {
    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipFlag, lines) -> {

            class LineAdder {
                void addLine(String key, ChatFormatting formatting) {
                    lines.add(Component.translatable(key).withStyle(formatting));
                }
                void addLineHighlight(String key) { addLine(key, ChatFormatting.YELLOW); }
                void addLineAttribute(String key) { addLine(key, ChatFormatting.GRAY); }
                void addLineDetail(String key) {
                    lines.add(Component.literal(" ")
                            .append(Component.translatable(key).withStyle(ChatFormatting.DARK_GRAY))
                    );
                }
                void addLineHoldShift() { addLine("tooltip.spellgems.shift_hint", ChatFormatting.DARK_GRAY); }
            }

            LineAdder tooltip = new LineAdder();

            if (stack.is(ModItems.ASTRAL_BOW)) {
                if (Minecraft.getInstance().hasShiftDown()) {
                    tooltip.addLineDetail("tooltip.spellgems.astral_bow.astral_arrows");
                    tooltip.addLineDetail("tooltip.spellgems.astral_bow.potion_gems");
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
                        tooltip.addLineHighlight(spell.tooltipNameKey());
                        if (Minecraft.getInstance().hasShiftDown()) {
                            tooltip.addLineDetail(spell.tooltipDescriptionKey());
                        } else {
                            tooltip.addLineHoldShift();
                        }

                        for (var effect : data.modifierEffects()) {
                            tooltip.addLineAttribute(effect.tooltipNameKey());
                            if (Minecraft.getInstance().hasShiftDown()) {
                                tooltip.addLineDetail(effect.tooltipDescriptionKey());
                            }
                        }

                        for (var effect : data.strikeEffects()) {
                            tooltip.addLineAttribute(effect.tooltipNameKey());
                            if (Minecraft.getInstance().hasShiftDown()) {
                                tooltip.addLineDetail(effect.tooltipDescriptionKey());
                            }
                        }

                        for (var effect : data.utilityEffects()) {
                            tooltip.addLineAttribute(effect.tooltipNameKey());
                            if (Minecraft.getInstance().hasShiftDown()) {
                                tooltip.addLineDetail(effect.tooltipDescriptionKey());
                            }
                        }

                        for (var effect : data.potionEffects()) {
                            tooltip.addLineAttribute(effect.potion().value().name());
                        }
                    }
                }
            }

        });
    }
}