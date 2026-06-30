package net.pnovaczek.spellgems.anvil;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AnvilMenu;
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

public final class SpellTomeAnvilHandler {

    public static final int XP_COST = 2;

    private SpellTomeAnvilHandler() {
    }

    public record CombineResult(ItemStack result, int xpCost) {
    }

    public enum EnchantmentType {
        MODIFIER,
        STRIKE,
        UTILITY,
        UNKNOWN
    }

    public static Optional<CombineResult> tryCombine(ItemStack gemStack, ItemStack tomeStack) {
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

        int repairCost = gemStack.getOrDefault(DataComponents.REPAIR_COST, 0);
        result.set(DataComponents.REPAIR_COST, AnvilMenu.calculateIncreasedRepairCost(repairCost));

        return Optional.of(new CombineResult(result, XP_COST));
    }

    public static EnchantmentType getEnchantmentType(Identifier enchantmentId) {
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
                || enchantmentId.equals(UtilityEnchantments.SILK_TOUCH);
    }

    private static boolean canApply(
            ItemStack gemStack,
            SpellGemData gemData,
            Identifier enchantmentId,
            EnchantmentType type
    ) {
        return switch (type) {
            case MODIFIER -> !gemData.modifierEffects().isEmpty()
                    ? false
                    : ModifierEnchantments.getCompatible(gemData.spellId()).contains(enchantmentId);
            case STRIKE -> !gemData.strikeEffects().isEmpty()
                    ? false
                    : gemStack.is(ModTags.COMBAT_SPELL_GEMS);
            case UTILITY -> !gemData.utilityEffects().isEmpty()
                    ? false
                    : gemStack.is(ModTags.UTILITY_SPELL_GEMS);
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