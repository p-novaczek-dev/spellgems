package net.pnovaczek.spellgems.astralbow;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.pnovaczek.spellgems.entity.AstralArrow;
import net.pnovaczek.spellgems.ModComponents;
import net.pnovaczek.spellgems.ModItems;
import net.pnovaczek.spellgems.Spellgems;
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

    public static int getDurabilityCost(boolean potionShot) {
        var config = Spellgems.CONFIG.astralBow;
        return potionShot ? config.potionShotDurabilityCost : config.normalShotDurabilityCost;
    }

    public static void fireVolley(ServerLevel level, Player player, ItemStack bow, InteractionHand hand, float power) {
        PotionEnchantment potion = getSelectedPotionEnchantment(bow);
        int count = EnchantmentHelper.processProjectileCount(level, bow, player, 1);
        float maxSpread = EnchantmentHelper.processProjectileSpread(level, bow, player, 0.0F);
        float angleStep = count == 1 ? 0.0F : 2.0F * maxSpread / (count - 1);
        float angleOffset = (count - 1) % 2 * angleStep / 2.0F;
        float direction = 1.0F;
        float velocity = power * 3.0F;

        for (int i = 0; i < count; i++) {
            float angle = angleOffset + direction * ((i + 1) / 2) * angleStep;
            direction = -direction;

            AstralArrow arrow = new AstralArrow(level, player, bow);
            if (potion != null) {
                arrow.setPotionEnchantment(potion);
            }
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot() + angle, 0.0F, velocity, 1.0F);
            level.addFreshEntity(arrow);
        }

        bow.hurtAndBreak(getDurabilityCost(potion != null), player, hand);
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