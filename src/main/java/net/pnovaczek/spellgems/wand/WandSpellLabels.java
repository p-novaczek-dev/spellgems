package net.pnovaczek.spellgems.wand;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.pnovaczek.spellgems.ModComponents;
import net.pnovaczek.spellgems.ModItems;
import net.pnovaczek.spellgems.ModSpells;
import net.pnovaczek.spellgems.inventory.WandContainer;
import net.pnovaczek.spellgems.item.SpellGemItem;
import net.pnovaczek.spellgems.item.data.SpellGemData;
import net.pnovaczek.spellgems.item.data.WandData;
import net.pnovaczek.spellgems.spell.Spell;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class WandSpellLabels {

    private WandSpellLabels() {
    }

    public static Component formatSelection(SpellGemData data) {
        Spell spell = ModSpells.get(data.spellId());
        if (spell == null) {
            return Component.empty();
        }

        MutableComponent line = Component.translatable(spell.tooltipNameKey())
                .withStyle(data.isEnchanted() ? ChatFormatting.AQUA : ChatFormatting.YELLOW);

        List<Component> enchantments = collectEnchantmentNames(data);
        if (!enchantments.isEmpty()) {
            line.append(Component.literal(" [").withStyle(ChatFormatting.GRAY));
            for (int i = 0; i < enchantments.size(); i++) {
                if (i > 0) {
                    line.append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
                }
                line.append(enchantments.get(i).copy().withStyle(ChatFormatting.GRAY));
            }
            line.append(Component.literal("]").withStyle(ChatFormatting.GRAY));
        }

        return line;
    }

    private static List<Component> collectEnchantmentNames(SpellGemData data) {
        List<Component> enchantments = new ArrayList<>();

        data.modifierEffects().forEach(effect ->
                enchantments.add(Component.translatable(effect.tooltipNameKey())));
        data.strikeEffects().forEach(effect ->
                enchantments.add(Component.translatable(effect.tooltipNameKey())));
        data.utilityEffects().forEach(effect ->
                enchantments.add(Component.translatable(effect.tooltipNameKey())));
        data.potionEffects().forEach(effect -> enchantments.add(effect.displayName()));

        return enchantments;
    }

    public static @Nullable SpellGemData getSelectedGemData(Player player) {
        return getGemDataInSlot(player, 0);
    }

    public static @Nullable SpellGemData previewCycledGemData(Player player, int direction) {
        return getGemDataInSlot(player, direction);
    }

    private static @Nullable SpellGemData getGemDataInSlot(Player player, int direction) {
        ItemStack wand = player.getMainHandItem();
        if (!wand.is(ModItems.WAND) || wand.isBroken()) {
            return null;
        }

        SimpleContainer slots = new SimpleContainer(WandContainer.SIZE);
        WandContainer.loadInto(slots, wand);

        WandData wandData = wand.getOrDefault(ModComponents.WAND_DATA, WandData.DEFAULT);
        int current = Mth.clamp(wandData.selectedSlot(), 0, WandContainer.SIZE - 1);
        int targetSlot = direction == 0
                ? current
                : WandSpellCaster.findNextOccupiedSlot(slots, current, Integer.signum(direction));

        ItemStack gemStack = slots.getItem(targetSlot);
        if (gemStack.isEmpty()) {
            return null;
        }

        return SpellGemItem.getSpellData(gemStack);
    }
}