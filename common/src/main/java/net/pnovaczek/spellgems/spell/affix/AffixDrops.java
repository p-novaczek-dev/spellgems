package net.pnovaczek.spellgems.spell.affix;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.pnovaczek.spellgems.spell.enchantment.SpellAffixes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Rewrites a mob's death items for Looting, No Loot, Scavenge, and Decapitate.
 * No Loot removes loot-table items and leaves scavenge gear and a decapitate head.
 */
public final class AffixDrops {

    private AffixDrops() {
    }

    public static boolean shouldRewrite(LivingEntity entity) {
        if (entity instanceof Player) {
            return false;
        }
        SpellKillCredit credit = AffixKills.creditOf(entity);
        if (credit == null || !credit.active()) {
            return false;
        }
        return SpellAffixes.level(credit.affixes(), SpellAffixes.LOOTING) > 0
                || SpellAffixes.has(credit.affixes(), SpellAffixes.NO_LOOT)
                || SpellAffixes.has(credit.affixes(), SpellAffixes.DECAPITATE)
                || SpellAffixes.has(credit.affixes(), SpellAffixes.SCAVENGE);
    }

    public static List<ItemEntity> rewrite(LivingEntity entity, ServerLevel level, Collection<ItemEntity> drops) {
        List<ItemEntity> original = new ArrayList<>();
        for (ItemEntity drop : drops) {
            if (drop != null && !drop.getItem().isEmpty()) {
                original.add(drop);
            }
        }
        if (entity instanceof Player) {
            return original;
        }
        SpellKillCredit credit = AffixKills.creditOf(entity);
        if (credit == null || !credit.active()) {
            return original;
        }

        int looting = SpellAffixes.level(credit.affixes(), SpellAffixes.LOOTING);
        boolean noLoot = SpellAffixes.has(credit.affixes(), SpellAffixes.NO_LOOT);
        List<AffixEquipment.Piece> equipment = AffixKills.equipmentOf(entity);

        List<ItemEntity> equipmentDrops = new ArrayList<>();
        List<ItemEntity> tableDrops = new ArrayList<>();
        for (ItemEntity drop : original) {
            if (AffixEquipment.isEquipmentDrop(equipment, drop.getItem())) {
                equipmentDrops.add(drop);
            } else {
                tableDrops.add(drop);
            }
        }

        List<ItemEntity> result = new ArrayList<>(equipmentDrops);
        if (noLoot) {
            // Loot-table items stay out. Scavenge and decapitate are added below.
        } else if (looting > 0) {
            List<ItemStack> rolled = AffixLoot.roll(entity, level, looting);
            if (rolled == null) {
                result.addAll(tableDrops);
            } else {
                for (ItemStack stack : rolled) {
                    if (!stack.isEmpty()) {
                        result.add(newDrop(level, entity, stack));
                    }
                }
            }
        } else {
            result.addAll(tableDrops);
        }

        if (SpellAffixes.has(credit.affixes(), SpellAffixes.SCAVENGE)) {
            for (AffixEquipment.Piece piece : equipment) {
                if (!AffixEquipment.shouldScavenge(piece) || AffixEquipment.alreadyPresent(result, piece.stack())) {
                    continue;
                }
                result.add(newDrop(level, entity, piece.stack().copy()));
            }
        }

        if (SpellAffixes.has(credit.affixes(), SpellAffixes.DECAPITATE)) {
            Item head = headFor(entity);
            if (head != null && !containsItem(result, head) && !nearbyHas(level, entity, head)) {
                result.add(newDrop(level, entity, new ItemStack(head)));
            }
        }
        return result;
    }

    private static ItemEntity newDrop(ServerLevel level, LivingEntity entity, ItemStack stack) {
        ItemEntity drop = new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), stack);
        drop.setDefaultPickUpDelay();
        return drop;
    }

    private static boolean containsItem(List<ItemEntity> drops, Item item) {
        for (ItemEntity drop : drops) {
            if (drop.getItem().is(item)) {
                return true;
            }
        }
        return false;
    }

    private static boolean nearbyHas(ServerLevel level, LivingEntity entity, Item item) {
        AABB box = entity.getBoundingBox().inflate(1.0);
        for (ItemEntity nearby : level.getEntitiesOfClass(ItemEntity.class, box, drop -> drop.getItem().is(item))) {
            return nearby != null;
        }
        return false;
    }

    /**
     * Heads whose 26.1.2 death or charged-creeper tables can roll one, plus zombie villagers.
     * Creeper heads stay on the charged-creeper drop and are not added here.
     */
    private static Item headFor(LivingEntity entity) {
        EntityType<?> type = entity.getType();
        if (type == EntityType.SKELETON) {
            return Items.SKELETON_SKULL;
        }
        if (type == EntityType.WITHER_SKELETON) {
            return Items.WITHER_SKELETON_SKULL;
        }
        if (type == EntityType.ZOMBIE || type == EntityType.ZOMBIE_VILLAGER) {
            return Items.ZOMBIE_HEAD;
        }
        if (type == EntityType.PIGLIN) {
            return Items.PIGLIN_HEAD;
        }
        return null;
    }
}
