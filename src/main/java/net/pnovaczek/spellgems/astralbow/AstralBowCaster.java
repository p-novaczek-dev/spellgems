package net.pnovaczek.spellgems.astralbow;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.pnovaczek.spellgems.ModComponents;
import net.pnovaczek.spellgems.ModItems;
import net.pnovaczek.spellgems.inventory.AstralBowContainer;
import net.pnovaczek.spellgems.item.SpellGemItem;
import net.pnovaczek.spellgems.item.data.AstralBowData;
import net.pnovaczek.spellgems.item.data.SpellGemData;
import net.pnovaczek.spellgems.spell.enchantment.PotionEnchantment;
import net.pnovaczek.spellgems.spell.enchantment.PotionEnchantments;
import net.pnovaczek.spellgems.wand.WandSpellCaster;
import org.jspecify.annotations.Nullable;

public final class AstralBowCaster {

    private AstralBowCaster() {
    }

    public static @Nullable ItemStack getSelectedGemStack(ItemStack bow) {
        if (!bow.is(ModItems.ASTRAL_BOW)) {
            return null;
        }

        SimpleContainer slots = new SimpleContainer(AstralBowContainer.SIZE);
        AstralBowContainer.loadInto(slots, bow);

        AstralBowData bowData = bow.getOrDefault(ModComponents.ASTRAL_BOW_DATA, AstralBowData.DEFAULT);
        int slot = Mth.clamp(bowData.selectedSlot(), 0, AstralBowContainer.SIZE - 1);
        ItemStack gemStack = slots.getItem(slot);
        return gemStack.isEmpty() ? null : gemStack;
    }

    public static @Nullable PotionEnchantment getSelectedPotionEnchantment(ItemStack bow) {
        ItemStack gemStack = getSelectedGemStack(bow);
        if (gemStack == null) {
            return null;
        }
        return PotionEnchantments.fromSpellGem(gemStack);
    }

    public static @Nullable SpellGemData getSelectedGemData(Player player) {
        ItemStack bow = player.getMainHandItem();
        if (!bow.is(ModItems.ASTRAL_BOW) || bow.isBroken()) {
            return null;
        }

        ItemStack gemStack = getSelectedGemStack(bow);
        if (gemStack == null) {
            return null;
        }

        return SpellGemItem.getSpellData(gemStack);
    }

    public static @Nullable SpellGemData previewCycledGemData(Player player, int direction) {
        ItemStack bow = player.getMainHandItem();
        if (!bow.is(ModItems.ASTRAL_BOW) || bow.isBroken()) {
            return null;
        }

        SimpleContainer slots = new SimpleContainer(AstralBowContainer.SIZE);
        AstralBowContainer.loadInto(slots, bow);

        AstralBowData bowData = bow.getOrDefault(ModComponents.ASTRAL_BOW_DATA, AstralBowData.DEFAULT);
        int current = Mth.clamp(bowData.selectedSlot(), 0, AstralBowContainer.SIZE - 1);
        int targetSlot = direction == 0
                ? current
                : WandSpellCaster.findNextOccupiedSlot(slots, current, Integer.signum(direction));

        ItemStack gemStack = slots.getItem(targetSlot);
        if (gemStack.isEmpty()) {
            return null;
        }

        return SpellGemItem.getSpellData(gemStack);
    }

    public static boolean cycleSelectedGem(ServerPlayer player, int direction) {
        return applyCycle(player, direction);
    }

    public static boolean applyLocalCycle(Player player, int direction) {
        return applyCycle(player, direction);
    }

    private static boolean applyCycle(Player player, int direction) {
        if (direction == 0) {
            return false;
        }

        ItemStack bow = player.getMainHandItem();
        if (!bow.is(ModItems.ASTRAL_BOW) || bow.isBroken()) {
            return false;
        }

        SimpleContainer slots = new SimpleContainer(AstralBowContainer.SIZE);
        AstralBowContainer.loadInto(slots, bow);

        AstralBowData bowData = bow.getOrDefault(ModComponents.ASTRAL_BOW_DATA, AstralBowData.DEFAULT);
        int current = Mth.clamp(bowData.selectedSlot(), 0, AstralBowContainer.SIZE - 1);
        int next = WandSpellCaster.findNextOccupiedSlot(slots, current, Integer.signum(direction));
        if (next == current) {
            return false;
        }

        bow.set(ModComponents.ASTRAL_BOW_DATA, new AstralBowData(next));
        return true;
    }
}