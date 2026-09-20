package net.pnovaczek.spellgems.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.pnovaczek.spellgems.ModComponents;
import net.pnovaczek.spellgems.ModSpells;
import net.pnovaczek.spellgems.ModTags;
import net.pnovaczek.spellgems.item.data.SpellGemData;
import net.pnovaczek.spellgems.spell.Spell;
import net.pnovaczek.spellgems.spell.SpellContext;
import net.pnovaczek.spellgems.spell.SpellIds;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Sockets a nova spell gem into a melee weapon and casts it when that weapon hits a living target.
 */
public final class WeaponSocketing {

    private WeaponSocketing() {
    }

    public static Optional<ItemStack> trySocket(ItemStack first, ItemStack second) {
        Optional<ItemStack> result = socket(first, second);
        if (result.isPresent()) {
            return result;
        }
        return socket(second, first);
    }

    private static Optional<ItemStack> socket(ItemStack weapon, ItemStack gem) {
        if (weapon.isEmpty() || gem.isEmpty()) {
            return Optional.empty();
        }
        if (!weapon.is(ModTags.SOCKETABLE_WEAPONS)) {
            return Optional.empty();
        }
        if (!(gem.getItem() instanceof SpellGemItem)) {
            return Optional.empty();
        }
        SpellGemData data = gem.get(ModComponents.SPELL_GEM_DATA);
        if (data == null || !SpellIds.NOVA.equals(data.spellId())) {
            return Optional.empty();
        }

        ItemStack result = weapon.copyWithCount(1);
        result.set(ModComponents.SPELL_GEM_DATA, data);
        return Optional.of(result);
    }

    public static Optional<ItemStack> tryUnsocket(ItemStack weapon) {
        if (getSocketedGem(weapon) == null) {
            return Optional.empty();
        }
        ItemStack result = weapon.copyWithCount(1);
        result.remove(ModComponents.SPELL_GEM_DATA);
        return Optional.of(result);
    }

    public static @Nullable SpellGemData getSocketedGem(ItemStack stack) {
        if (stack.isEmpty() || !stack.is(ModTags.SOCKETABLE_WEAPONS)) {
            return null;
        }
        return stack.get(ModComponents.SPELL_GEM_DATA);
    }

    public static void onPlayerAttackLiving(Player player, LivingEntity target, ItemStack weapon) {
        if (player.level().isClientSide() || !target.isAlive()) {
            return;
        }
        SpellGemData data = getSocketedGem(weapon);
        if (data == null) {
            return;
        }
        Spell spell = ModSpells.get(data.spellId());
        if (spell == null) {
            return;
        }
        SpellContext context = SpellContext.forWeapon(player.level(), player, weapon, data, target);
        if (spell.canCast(context)) {
            spell.cast(context);
        }
    }
}
