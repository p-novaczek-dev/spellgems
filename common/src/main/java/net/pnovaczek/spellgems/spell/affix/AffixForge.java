package net.pnovaczek.spellgems.spell.affix;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.pnovaczek.spellgems.ModComponents;
import net.pnovaczek.spellgems.ModItems;
import net.pnovaczek.spellgems.ModTags;
import net.pnovaczek.spellgems.item.SpellGemItem;
import net.pnovaczek.spellgems.item.data.SpellGemData;
import net.pnovaczek.spellgems.item.data.TomeData;
import net.pnovaczek.spellgems.spell.SpellIds;
import net.pnovaczek.spellgems.spell.enchantment.SpellAffix;
import net.pnovaczek.spellgems.spell.enchantment.SpellAffixes;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Gem-forge affix merges. Two tomes keep slot order: the left stack is the base and the right stack is incoming.
 * A tome and a combat gem apply the book onto the gem from either slot.
 */
public final class AffixForge {

    private AffixForge() {
    }

    public static boolean isAffixTome(ItemStack stack) {
        if (stack.isEmpty() || !stack.is(ModTags.CATALYST_BOOKS)) {
            return false;
        }
        TomeData data = stack.get(ModComponents.TOME_DATA);
        return data != null && data.enchantmentId() == null && !data.affixes().isEmpty();
    }

    public static boolean isCombatGem(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof SpellGemItem)) {
            return false;
        }
        SpellGemData data = stack.get(ModComponents.SPELL_GEM_DATA);
        if (data == null) {
            return false;
        }
        Identifier spellId = data.spellId();
        return SpellIds.PROJECTILE.equals(spellId)
                || SpellIds.NOVA.equals(spellId)
                || SpellIds.VORTEX.equals(spellId);
    }

    /**
     * Left tome is the base. A no-op merge (including two copies already at max level) is empty.
     */
    public static Optional<ItemStack> tryMergeTomes(ItemStack left, ItemStack right) {
        if (!isAffixTome(left) || !isAffixTome(right)) {
            return Optional.empty();
        }
        TomeData base = left.get(ModComponents.TOME_DATA);
        TomeData incoming = right.get(ModComponents.TOME_DATA);
        Optional<List<SpellAffix>> merged = merge(base.affixes(), incoming.affixes());
        if (merged.isEmpty()) {
            return Optional.empty();
        }
        ItemStack result = new ItemStack(ModItems.SPELL_TOME);
        result.set(ModComponents.TOME_DATA, TomeData.affixes(merged.get()));
        return Optional.of(result);
    }

    public static Optional<ItemStack> tryApplyToGem(ItemStack first, ItemStack second) {
        ItemStack gem = combatGem(first, second);
        ItemStack tome = affixTome(first, second);
        if (gem == null || tome == null) {
            return Optional.empty();
        }
        SpellGemData gemData = gem.get(ModComponents.SPELL_GEM_DATA);
        TomeData tomeData = tome.get(ModComponents.TOME_DATA);
        if (gemData == null || tomeData == null) {
            return Optional.empty();
        }
        Optional<List<SpellAffix>> merged = merge(gemData.affixes(), tomeData.affixes());
        if (merged.isEmpty()) {
            return Optional.empty();
        }
        ItemStack result = gem.copy();
        result.setCount(1);
        result.set(ModComponents.SPELL_GEM_DATA, gemData.withAffixes(merged.get()));
        return Optional.of(result);
    }

    /**
     * Experience removed when the shown gem application is taken. Book merges stay at 0.
     * A negative value is a refund.
     */
    public static int xpDelta(ItemStack first, ItemStack second) {
        ItemStack gem = combatGem(first, second);
        ItemStack tome = affixTome(first, second);
        if (gem == null || tome == null) {
            return 0;
        }
        SpellGemData gemData = gem.get(ModComponents.SPELL_GEM_DATA);
        TomeData tomeData = tome.get(ModComponents.TOME_DATA);
        if (gemData == null || tomeData == null) {
            return 0;
        }
        Optional<List<SpellAffix>> merged = merge(gemData.affixes(), tomeData.affixes());
        if (merged.isEmpty()) {
            return 0;
        }
        return AffixCosts.charge(gemData.affixes(), merged.get());
    }

    public static ItemStack lootingTome(int level) {
        ItemStack stack = new ItemStack(ModItems.SPELL_TOME);
        stack.set(ModComponents.TOME_DATA, TomeData.affix(new SpellAffix(SpellAffixes.LOOTING, level)));
        return stack;
    }

    public static ItemStack lootingProjectileGem(int level) {
        ItemStack stack = new ItemStack(ModItems.SPELL_GEM_PROJECTILE);
        stack.set(
                ModComponents.SPELL_GEM_DATA,
                SpellGemData.create(SpellIds.PROJECTILE).withAffixes(List.of(new SpellAffix(SpellAffixes.LOOTING, level)))
        );
        return stack;
    }

    private static Optional<List<SpellAffix>> merge(List<SpellAffix> base, List<SpellAffix> incoming) {
        Map<Identifier, Integer> levels = new LinkedHashMap<>(SpellAffixes.asMap(base));
        for (SpellAffix affix : incoming) {
            if (!SpellAffixes.isKnown(affix.id())) {
                continue;
            }
            Identifier partner = SpellAffixes.exclusivePartner(affix.id());
            if (partner != null) {
                levels.remove(partner);
            }
            int current = levels.getOrDefault(affix.id(), 0);
            int mergedLevel = combineLevel(current, affix.level(), SpellAffixes.maxLevel(affix.id()));
            if (mergedLevel <= 0) {
                levels.remove(affix.id());
            } else {
                levels.put(affix.id(), mergedLevel);
            }
        }
        List<SpellAffix> result = SpellAffixes.inDisplayOrder(levels);
        if (SpellAffixes.same(base, result)) {
            return Optional.empty();
        }
        return Optional.of(result);
    }

    /**
     * Anvil-style: equal levels become the next level, capped at max. Unequal levels keep the higher one.
     */
    static int combineLevel(int current, int incoming, int max) {
        int cap = Math.max(1, max);
        if (current <= 0) {
            return Math.min(incoming, cap);
        }
        if (incoming <= 0) {
            return Math.min(current, cap);
        }
        int merged = current == incoming ? current + 1 : Math.max(current, incoming);
        return Math.min(merged, cap);
    }

    private static @Nullable ItemStack combatGem(ItemStack first, ItemStack second) {
        if (isCombatGem(first)) {
            return first;
        }
        if (isCombatGem(second)) {
            return second;
        }
        return null;
    }

    private static @Nullable ItemStack affixTome(ItemStack first, ItemStack second) {
        if (isAffixTome(first)) {
            return first;
        }
        if (isAffixTome(second)) {
            return second;
        }
        return null;
    }
}
