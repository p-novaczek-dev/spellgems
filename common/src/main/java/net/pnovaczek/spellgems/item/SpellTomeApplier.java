package net.pnovaczek.spellgems.item;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.pnovaczek.spellgems.ModComponents;
import net.pnovaczek.spellgems.ModTags;
import net.pnovaczek.spellgems.item.data.SpellGemData;
import net.pnovaczek.spellgems.item.data.TomeData;
import net.pnovaczek.spellgems.spell.enchantment.ModifierEnchantment;
import net.pnovaczek.spellgems.spell.enchantment.ModifierEnchantments;
import net.pnovaczek.spellgems.spell.enchantment.StrikeEnchantment;
import net.pnovaczek.spellgems.spell.enchantment.StrikeEnchantments;
import net.pnovaczek.spellgems.spell.enchantment.UtilityEnchantment;
import net.pnovaczek.spellgems.spell.enchantment.UtilityEnchantments;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Applies a spell tome's enchantment onto a spell gem.
 * Slot order does not matter.
 */
public final class SpellTomeApplier {

    private SpellTomeApplier() {
    }

    private enum EnchantmentType {
        MODIFIER,
        STRIKE,
        UTILITY,
        UNKNOWN
    }

    public static Optional<ItemStack> tryApply(ItemStack first, ItemStack second) {
        Optional<ItemStack> result = apply(first, second);
        if (result.isPresent()) {
            return result;
        }
        return apply(second, first);
    }

    private static Optional<ItemStack> apply(ItemStack gemStack, ItemStack tomeStack) {
        if (gemStack.isEmpty() || tomeStack.isEmpty()) {
            return Optional.empty();
        }

        if (!gemStack.is(ModTags.COMBAT_SPELL_GEMS) && !gemStack.is(ModTags.UTILITY_SPELL_GEMS)) {
            return Optional.empty();
        }

        if (!tomeStack.is(ModTags.CATALYST_BOOKS)) {
            return Optional.empty();
        }

        TomeData tomeData = tomeStack.get(ModComponents.TOME_DATA);
        if (tomeData == null || !tomeData.isEnchanted()) {
            return Optional.empty();
        }

        SpellGemData gemData = gemStack.get(ModComponents.SPELL_GEM_DATA);
        if (gemData == null) {
            return Optional.empty();
        }

        Identifier enchantmentId = tomeData.enchantmentId();
        EnchantmentType type = getEnchantmentType(enchantmentId);
        if (type == EnchantmentType.UNKNOWN || !canApply(gemStack, gemData, enchantmentId, type)) {
            return Optional.empty();
        }

        SpellGemData newData = applyEnchantment(gemData, enchantmentId, type);
        ItemStack result = gemStack.copy();
        result.set(ModComponents.SPELL_GEM_DATA, newData);
        return Optional.of(result);
    }

    private static EnchantmentType getEnchantmentType(Identifier enchantmentId) {
        if (ModifierEnchantments.getAll().contains(enchantmentId)) {
            return EnchantmentType.MODIFIER;
        }
        if (StrikeEnchantments.getAll().contains(enchantmentId)) {
            return EnchantmentType.STRIKE;
        }
        if (isUtilityEnchantment(enchantmentId)) {
            return EnchantmentType.UTILITY;
        }
        return EnchantmentType.UNKNOWN;
    }

    private static boolean isUtilityEnchantment(Identifier enchantmentId) {
        return enchantmentId.equals(UtilityEnchantments.SMELT)
                || enchantmentId.equals(UtilityEnchantments.SILK_TOUCH)
                || enchantmentId.equals(UtilityEnchantments.EXTEND);
    }

    private static boolean canApply(
            ItemStack gemStack,
            SpellGemData gemData,
            Identifier enchantmentId,
            EnchantmentType type
    ) {
        return switch (type) {
            case MODIFIER -> gemData.modifierEffects().isEmpty()
                    && ModifierEnchantments.getCompatible(gemData.spellId()).contains(enchantmentId);
            case STRIKE -> gemData.strikeEffects().isEmpty()
                    && gemStack.is(ModTags.COMBAT_SPELL_GEMS);
            case UTILITY -> gemData.utilityEffects().isEmpty()
                    && gemStack.is(ModTags.UTILITY_SPELL_GEMS);
            case UNKNOWN -> false;
        };
    }

    private static SpellGemData applyEnchantment(SpellGemData gemData, Identifier enchantmentId, EnchantmentType type) {
        return switch (type) {
            case MODIFIER -> new SpellGemData(
                    gemData.spellId(),
                    List.of(new ModifierEnchantment(enchantmentId)),
                    gemData.strikeEffects(),
                    gemData.utilityEffects(),
                    gemData.potionEffects()
            );
            case STRIKE -> new SpellGemData(
                    gemData.spellId(),
                    gemData.modifierEffects(),
                    List.of(new StrikeEnchantment(enchantmentId)),
                    gemData.utilityEffects(),
                    gemData.potionEffects()
            );
            case UTILITY -> {
                List<UtilityEnchantment> utilities = new ArrayList<>(gemData.utilityEffects());
                utilities.add(new UtilityEnchantment(enchantmentId));
                yield new SpellGemData(
                        gemData.spellId(),
                        gemData.modifierEffects(),
                        gemData.strikeEffects(),
                        utilities,
                        gemData.potionEffects()
                );
            }
            case UNKNOWN -> gemData;
        };
    }
}
