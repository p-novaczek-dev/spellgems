package net.pnovaczek.spellgems.spell.affix;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.entity.EntitySpawnReason;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Rerolls a mob loot table with a temporary attacker whose main hand has the affix's Looting level.
 * The dummy is never added to the world.
 */
public final class AffixLoot {

    private AffixLoot() {
    }

    public static @Nullable List<ItemStack> roll(LivingEntity mob, ServerLevel level, int lootingLevel) {
        if (lootingLevel <= 0 || mob.getLootTable().isEmpty()) {
            return List.of();
        }
        Zombie dummy = EntityType.ZOMBIE.create(level, EntitySpawnReason.TRIGGERED);
        if (dummy == null) {
            return null;
        }
        Holder<Enchantment> looting = level.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.LOOTING);
        ItemStack tool = new ItemStack(Items.STICK);
        tool.enchant(looting, lootingLevel);
        dummy.setItemSlot(EquipmentSlot.MAINHAND, tool);

        LootParams.Builder builder = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, mob)
                .withParameter(LootContextParams.ORIGIN, mob.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, mob.damageSources().magic())
                .withParameter(LootContextParams.ATTACKING_ENTITY, dummy)
                .withParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, dummy);
        if (mob.getLastHurtByPlayer() != null && mob.getLastHurtByPlayerMemoryTime() > 0) {
            builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, mob.getLastHurtByPlayer())
                    .withLuck(mob.getLastHurtByPlayer().getLuck());
        }
        LootTable table = level.getServer().reloadableRegistries().getLootTable(mob.getLootTable().get());
        List<ItemStack> items = new ArrayList<>();
        table.getRandomItems(builder.create(LootContextParamSets.ENTITY), mob.getLootTableSeed(), items::add);
        return items;
    }
}
