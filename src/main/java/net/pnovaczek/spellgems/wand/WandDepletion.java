package net.pnovaczek.spellgems.wand;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.RemoveBinomial;
import net.pnovaczek.spellgems.ModItems;

public final class WandDepletion {

    private static final RemoveBinomial NON_ARMOR_UNBREAKING =
            new RemoveBinomial(new LevelBasedValue.Fraction(LevelBasedValue.perLevel(1.0F), LevelBasedValue.perLevel(2.0F, 1.0F)));

    private WandDepletion() {
    }

    public static boolean isDepleted(ItemStack stack) {
        return stack.is(ModItems.WAND)
                && stack.isDamageableItem()
                && (stack.nextDamageWillBreak() || stack.isBroken());
    }

    public static int getRemainingDurability(ItemStack stack) {
        if (!stack.isDamageableItem()) {
            return 0;
        }

        return stack.getMaxDamage() - stack.getDamageValue();
    }

    public static boolean canAffordDurabilityCost(ItemStack stack, int effectiveCost) {
        if (effectiveCost <= 0) {
            return stack.is(ModItems.WAND);
        }

        return stack.is(ModItems.WAND) && getRemainingDurability(stack) >= effectiveCost;
    }

    public static int resolveEffectiveDurabilityCost(Player player, ItemStack wand, int nominalCost) {
        if (nominalCost <= 0 || !wand.isDamageableItem() || player.hasInfiniteMaterials()) {
            return 0;
        }

        if (player.level() instanceof ServerLevel serverLevel) {
            return EnchantmentHelper.processDurabilityChange(serverLevel, wand, nominalCost);
        }

        return estimateUnbreakingCost(wand, nominalCost, player.getRandom());
    }

    public static void applyDurabilityCost(ItemStack wand, int effectiveCost, ServerPlayer player) {
        if (effectiveCost <= 0 || !wand.isDamageableItem()) {
            return;
        }

        int newDamage = Math.min(wand.getDamageValue() + effectiveCost, wand.getMaxDamage() - 1);
        CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(player, wand, newDamage);
        wand.setDamageValue(newDamage);
    }

    private static int estimateUnbreakingCost(ItemStack wand, int nominalCost, RandomSource random) {
        int unbreakingLevel = getUnbreakingLevel(wand);
        if (unbreakingLevel <= 0) {
            return nominalCost;
        }

        return Math.round(NON_ARMOR_UNBREAKING.process(unbreakingLevel, random, nominalCost));
    }

    private static int getUnbreakingLevel(ItemStack wand) {
        ItemEnchantments enchantments = wand.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (var entry : enchantments.entrySet()) {
            if (entry.getKey().is(Enchantments.UNBREAKING)) {
                return entry.getIntValue();
            }
        }

        return 0;
    }
}