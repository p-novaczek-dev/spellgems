package net.pnovaczek.spellgems.client;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.pnovaczek.spellgems.ModComponents;
import net.pnovaczek.spellgems.ModItems;
import net.pnovaczek.spellgems.ModSpells;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.SpellgemsConfig;
import net.pnovaczek.spellgems.inventory.AstralBowContainer;
import net.pnovaczek.spellgems.inventory.WandContainer;
import net.pnovaczek.spellgems.item.SpellGemItem;
import net.pnovaczek.spellgems.item.SpellTomeItem;
import net.pnovaczek.spellgems.item.WeaponSocketing;
import net.pnovaczek.spellgems.item.data.AstralBowData;
import net.pnovaczek.spellgems.item.data.InventoryBinding;
import net.pnovaczek.spellgems.item.data.SpellGemData;
import net.pnovaczek.spellgems.item.data.WandData;
import net.pnovaczek.spellgems.platform.client.ClientPlatform;
import net.pnovaczek.spellgems.spell.Spell;
import net.pnovaczek.spellgems.spell.SpellIds;
import net.pnovaczek.spellgems.spell.enchantment.ModifierEnchantments;
import net.pnovaczek.spellgems.spell.enchantment.PotionEnchantment;
import net.pnovaczek.spellgems.spell.enchantment.UtilityEnchantments;
import net.pnovaczek.spellgems.wand.WandDepletion;
import net.pnovaczek.spellgems.wand.WandSpellCaster;
import net.pnovaczek.spellgems.wand.WandSpellLabels;

import java.util.List;
import java.util.function.BiConsumer;

public class SpellgemsTooltips {
    public static void register() {
        ClientPlatform.client().onItemTooltip((stack, tooltipContext, tooltipFlag, lines) -> {

            class LineAdder {
                void addLine(String key, ChatFormatting formatting) {
                    lines.add(Component.translatable(key).withStyle(formatting));
                }
                void addLine(MutableComponent component, ChatFormatting formatting) {
                    lines.add(component.withStyle(formatting));
                }
                void addLineAttention(String key) { addLine(key, ChatFormatting.RED); }
                void addLineHighlight(String key) { addLine(key, ChatFormatting.YELLOW); }
                void addLineAttribute(String key) { addLine(key, ChatFormatting.GRAY); }
                void addLineAttribute(MutableComponent component) { addLine(component, ChatFormatting.GRAY); }
                void addLineStat(String key) { addLine(key, ChatFormatting.DARK_GREEN); }
                void addLineStat(MutableComponent component) { addLine(component, ChatFormatting.DARK_GREEN); }
                void addLineDetail(String key, Object... args) { lines.add(Component.translatable(key, args).withStyle(ChatFormatting.DARK_GRAY)); }
                void addLineDetail(MutableComponent component) { addLine(component, ChatFormatting.DARK_GRAY); }
                void addLineHoldShift() { addLine("tooltip.spellgems.shift_hint", ChatFormatting.DARK_GRAY); }
            }

            LineAdder tooltip = new LineAdder();

            if (stack.is(ModItems.ASTRAL_BARRIER)) {
                tooltip.addLineHighlight("tooltip.spellgems.astral_barrier.wither_proof");
                tooltip.addLineDetail("tooltip.spellgems.astral_barrier.description");
            }

            if (stack.is(ModItems.SPELL_DISPENSER)) {
                tooltip.addLineDetail("tooltip.spellgems.spell_dispenser.muffle");
            }

            if (stack.is(ModItems.WAND) || stack.is(ModItems.ASTRAL_BOW)) {
                stripVanillaContainerLines(lines);
            }

            if (stack.is(ModItems.WAND)) {
                if (WandDepletion.isDepleted(stack)) {
                    tooltip.addLineAttention("tooltip.spellgems.wand.depleted");
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
                    tooltip.addLineDetail(Component.translatable(
                                    "tooltip.spellgems.wand.cycle",
                                    KeyMapping.createNameSupplier(SpellgemsKeyMappings.CYCLE_SPELL_KEY.getName()).get()
                            ));
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
                    tooltip.addLineDetail(Component.translatable(
                                    "tooltip.spellgems.astral_bow.cycle",
                                    KeyMapping.createNameSupplier(SpellgemsKeyMappings.CYCLE_SPELL_KEY.getName()).get()
                            ));
                } else {
                    tooltip.addLineHoldShift();
                }
            }
            else if (stack.is(ModItems.SPELL_TOME)) {
                var data = SpellTomeItem.getTomeData(stack);

                if (data != null && data.isEnchanted()) {
                    tooltip.addLineAttribute(data.tooltipNameKey());
                    if (Minecraft.getInstance().hasShiftDown()) {
                        tooltip.addLineDetail(
                                data.tooltipDescriptionKey(),
                                enchantmentDescriptionArgs(data.enchantmentId())
                        );
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
                appendGreaterOrSingleGemTooltip(stack.get(ModComponents.SPELL_GEM_DATA), lines, tooltipContext, true, stack);
            } else {
                appendGreaterOrSingleGemTooltip(WeaponSocketing.getSocketedGem(stack), lines, tooltipContext, false, ItemStack.EMPTY);
            }
        });
    }

    private static void appendGreaterOrSingleGemTooltip(
            SpellGemData data,
            List<Component> lines,
            net.minecraft.world.item.Item.TooltipContext tooltipContext,
            boolean includeCastStats,
            ItemStack stack
    ) {
        if (data == null) {
            return;
        }
        boolean shiftDown = Minecraft.getInstance().hasShiftDown();
        if (data.followUp().isPresent()) {
            appendSpellGemTooltip(data.withoutFollowUp(), lines, tooltipContext, false, ItemStack.EMPTY);
            appendSpellGemTooltip(data.followUp().get(), lines, tooltipContext, false, ItemStack.EMPTY);
        } else {
            appendSpellGemTooltip(data, lines, tooltipContext, includeCastStats, stack);
        }
        if (!shiftDown) {
            lines.add(Component.translatable("tooltip.spellgems.shift_hint").withStyle(ChatFormatting.DARK_GRAY));
        } else if (includeCastStats && data.followUp().isPresent()) {
            appendCastStats(data, lines);
        }
    }

    private static void appendSpellGemTooltip(
            SpellGemData data,
            List<Component> lines,
            net.minecraft.world.item.Item.TooltipContext tooltipContext,
            boolean includeCastStats,
            ItemStack stack
    ) {
        if (data == null) {
            return;
        }
        Spell spell = ModSpells.get(data.spellId());
        if (spell == null) {
            return;
        }

        boolean shiftDown = Minecraft.getInstance().hasShiftDown();
        lines.add(Component.translatable(spell.tooltipNameKey()).withStyle(ChatFormatting.YELLOW));

        if (shiftDown) {
            if (spell.id().equals(SpellIds.POTION)) {
                if (data.potionEffects().isEmpty()) {
                    lines.add(Component.translatable(spell.tooltipDescriptionKey()).withStyle(ChatFormatting.DARK_GRAY));
                }
                lines.add(Component.translatable("tooltip.spellgems.spell_gem_potion.astral_bow").withStyle(ChatFormatting.DARK_GRAY));
            } else {
                lines.add(Component.translatable(spell.tooltipDescriptionKey()).withStyle(ChatFormatting.DARK_GRAY));
            }
            if (spell.id().equals(SpellIds.ITEM_TRANSPORT)) {
                lines.add(Component.translatable("tooltip.spellgems.spell.item_transport.bind").withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        for (var effect : data.modifierEffects()) {
            lines.add(Component.translatable(effect.tooltipNameKey()).withStyle(ChatFormatting.GRAY));
            if (shiftDown) {
                lines.add(Component.translatable(
                        effect.tooltipDescriptionKey(),
                        enchantmentDescriptionArgs(effect.id())
                ).withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        for (var effect : data.strikeEffects()) {
            lines.add(Component.translatable(effect.tooltipNameKey()).withStyle(ChatFormatting.GRAY));
            if (shiftDown) {
                lines.add(Component.translatable(effect.tooltipDescriptionKey()).withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        for (var effect : data.utilityEffects()) {
            lines.add(Component.translatable(effect.tooltipNameKey()).withStyle(ChatFormatting.GRAY));
            if (shiftDown) {
                lines.add(Component.translatable(effect.tooltipDescriptionKey()).withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        for (PotionEnchantment enchantment : data.potionEffects()) {
            lines.add(enchantment.displayName().copy().withStyle(ChatFormatting.GRAY));
            if (shiftDown) {
                PotionContents.addPotionTooltip(
                        enchantment.contents().getAllEffects(),
                        lines::add,
                        enchantment.durationScale(),
                        tooltipContext.tickRate()
                );
            }
        }

        if (shiftDown) {
            appendDamageStat(data, lines);
            appendUtilityStats(data, lines, stack);
            if (includeCastStats) {
                appendCastStats(data, lines);
            }
        }
    }

    private static void appendDamageStat(SpellGemData data, List<Component> lines) {
        SpellgemsConfig.SpellConfig config = Spellgems.CONFIG.getSpellConfig(data.spellId());
        if (!(config instanceof SpellgemsConfig.SpellCombatConfig combat) || combat.damage <= 0.0F) {
            return;
        }
        float damage = combat.damage;
        for (var effect : data.modifierEffects()) {
            if (effect.is(ModifierEnchantments.POWER)) {
                damage *= combat.powerDamageMultiplier;
                break;
            }
        }
        if (damage <= 0.0F) {
            return;
        }
        appendIndentedStat(lines, Component.translatable("tooltip.spellgems.spell_gem.damage", formatStatNumber(damage)));
    }

    /** Range, area, and jump count for utility gems. Shown above wand cost and dispenser cooldown. */
    private static void appendUtilityStats(SpellGemData data, List<Component> lines, ItemStack stack) {
        Identifier spellId = data.spellId();
        boolean extended = data.utilityEffects().stream().anyMatch(effect -> effect.is(UtilityEnchantments.EXTEND));
        if (spellId.equals(SpellIds.BLINK)) {
            SpellgemsConfig.BlinkSpellConfig blink = Spellgems.CONFIG.spells.blink;
            if (blink == null) {
                return;
            }
            double range = extended ? blink.maxDistance * blink.extendMultiplier : blink.maxDistance;
            appendRangeStat(lines, range);
        } else if (spellId.equals(SpellIds.MAGNET)) {
            SpellgemsConfig.MagnetSpellConfig magnet = Spellgems.CONFIG.spells.magnet;
            if (magnet == null) {
                return;
            }
            double range = extended ? magnet.range * magnet.extendMultiplier : magnet.range;
            appendRangeStat(lines, range);
        } else if (spellId.equals(SpellIds.HARVEST)) {
            appendConfiguredArea(lines, Spellgems.CONFIG.spells.harvest);
        } else if (spellId.equals(SpellIds.PLANT)) {
            appendConfiguredArea(lines, Spellgems.CONFIG.spells.plant);
        } else if (spellId.equals(SpellIds.GROW)) {
            appendConfiguredArea(lines, Spellgems.CONFIG.spells.grow);
        } else if (spellId.equals(SpellIds.FEED)) {
            SpellgemsConfig.FeedSpellConfig feed = Spellgems.CONFIG.spells.feed;
            if (feed != null) {
                appendRangeStat(lines, feed.range);
            }
        } else if (spellId.equals(SpellIds.GEODE_RESONANCE)) {
            SpellgemsConfig.GeodeResonanceSpellConfig geode = Spellgems.CONFIG.spells.geodeResonance;
            if (geode == null) {
                return;
            }
            appendIndentedStat(lines, Component.translatable(
                    "tooltip.spellgems.spell_gem.jumps",
                    Integer.toString(geode.jumpCount)
            ));
            appendRangeStat(lines, geode.searchRadius);
        } else if (spellId.equals(SpellIds.ITEM_TRANSPORT)) {
            SpellgemsConfig.ItemTransportSpellConfig transport = Spellgems.CONFIG.spells.itemTransport;
            if (transport != null) {
                appendIndentedStat(lines, Component.translatable(
                        "tooltip.spellgems.spell_gem.stacks",
                        Integer.toString(transport.maxStacks)
                ));
            }
            appendBoundStat(lines, stack);
        }
    }

    private static void appendBoundStat(List<Component> lines, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        InventoryBinding binding = stack.get(ModComponents.INVENTORY_BINDING);
        if (binding == null) {
            return;
        }
        BlockPos pos = binding.pos();
        var level = Minecraft.getInstance().level;
        boolean otherDimension = level == null || !level.dimension().equals(binding.dimension());
        if (otherDimension) {
            appendIndentedStat(lines, Component.translatable(
                    "tooltip.spellgems.spell_gem.bound_dimension",
                    Component.translatable(binding.dimension().identifier().toLanguageKey("dimension")),
                    Integer.toString(pos.getX()),
                    Integer.toString(pos.getY()),
                    Integer.toString(pos.getZ())
            ));
        } else {
            appendIndentedStat(lines, Component.translatable(
                    "tooltip.spellgems.spell_gem.bound",
                    Integer.toString(pos.getX()),
                    Integer.toString(pos.getY()),
                    Integer.toString(pos.getZ())
            ));
        }
    }

    private static void appendRangeStat(List<Component> lines, double range) {
        appendIndentedStat(lines, Component.translatable("tooltip.spellgems.spell_gem.range", formatStatNumber(range)));
    }

    private static void appendConfiguredArea(List<Component> lines, SpellgemsConfig.AreaSpellConfig config) {
        if (config != null) {
            appendAreaStat(lines, config.areaRadius);
        }
    }

    private static void appendConfiguredArea(List<Component> lines, SpellgemsConfig.GrowSpellConfig config) {
        if (config != null) {
            appendAreaStat(lines, config.areaRadius);
        }
    }

    private static void appendAreaStat(List<Component> lines, int radius) {
        int side = radius * 2 + 1;
        appendIndentedStat(lines, Component.translatable("tooltip.spellgems.spell_gem.area", side, side));
    }

    private static String formatStatNumber(double value) {
        return value == (int) value
                ? Integer.toString((int) value)
                : String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    private static void appendCastStats(SpellGemData data, List<Component> lines) {
        int wandCost = WandSpellCaster.getDurabilityCost(data.spellId(), data);
        appendIndentedStat(lines, Component.translatable("tooltip.spellgems.spell_gem.wand_cost", wandCost));
        int dispenserCooldown = Spellgems.CONFIG.getDispenserCooldownTicks(data.spellId());
        if (data.followUp().isPresent()) {
            dispenserCooldown += Spellgems.CONFIG.getDispenserCooldownTicks(data.followUp().get().spellId());
        }
        appendIndentedStat(lines, Component.translatable("tooltip.spellgems.spell_gem.dispenser_cooldown", dispenserCooldown));
    }

    private static void appendIndentedStat(List<Component> lines, Component text) {
        lines.add(CommonComponents.space().append(text).withStyle(ChatFormatting.DARK_GREEN));
    }

    private static Object[] enchantmentDescriptionArgs(Identifier enchantmentId) {
        if (enchantmentId == null) {
            return new Object[0];
        }
        if (enchantmentId.equals(ModifierEnchantments.CHAINING)) {
            return new Object[]{Spellgems.CONFIG.chainingCount};
        }
        if (enchantmentId.equals(ModifierEnchantments.MULTISHOT)) {
            return new Object[]{Spellgems.CONFIG.multishotCount};
        }
        if (enchantmentId.equals(ModifierEnchantments.SPLIT)) {
            return new Object[]{Spellgems.CONFIG.splitCount};
        }
        return new Object[0];
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
            lines.add(gemLine.copy().withStyle(ChatFormatting.GRAY));
        }

        if (!anyGems) {
            lines.add(Component.translatable("tooltip.spellgems.equipped_gems.empty")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}