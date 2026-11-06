package net.pnovaczek.spellgems.spell.affix;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.tags.ItemTags;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Equipment captured before death loot runs, so scavenge can tell gear from loot-table items by stack identity.
 */
public final class AffixEquipment {

    public record Piece(EquipmentSlot slot, ItemStack stack) {
    }

    private AffixEquipment() {
    }

    public static List<Piece> snapshot(LivingEntity entity) {
        List<Piece> pieces = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                pieces.add(new Piece(slot, stack));
            }
        }
        return pieces;
    }

    public static boolean isEquipmentDrop(List<Piece> equipment, ItemStack stack) {
        for (Piece piece : equipment) {
            if (piece.stack() == stack) {
                return true;
            }
        }
        return false;
    }

    public static boolean alreadyPresent(Collection<ItemEntity> drops, ItemStack stack) {
        for (ItemEntity drop : drops) {
            if (drop.getItem() == stack) {
                return true;
            }
        }
        return false;
    }

    public static boolean shouldScavenge(Piece piece) {
        EquipmentSlot slot = piece.slot();
        if (slot == EquipmentSlot.HEAD || slot == EquipmentSlot.CHEST || slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET) {
            return true;
        }
        if (slot != EquipmentSlot.MAINHAND && slot != EquipmentSlot.OFFHAND) {
            return false;
        }
        ItemStack stack = piece.stack();
        if (stack.is(ItemTags.SWORDS)
                || stack.is(ItemTags.AXES)
                || stack.is(ItemTags.HOES)
                || stack.is(ItemTags.PICKAXES)
                || stack.is(ItemTags.SHOVELS)
                || stack.is(ItemTags.WEAPON_ENCHANTABLE)
                || stack.is(ItemTags.MELEE_WEAPON_ENCHANTABLE)) {
            return true;
        }
        return stack.getItem() instanceof BowItem
                || stack.getItem() instanceof CrossbowItem
                || stack.getItem() instanceof TridentItem
                || stack.getItem() instanceof MaceItem
                || stack.getItem() instanceof FishingRodItem;
    }
}
