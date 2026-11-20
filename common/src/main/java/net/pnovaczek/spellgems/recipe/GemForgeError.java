package net.pnovaczek.spellgems.recipe;

import net.minecraft.world.item.ItemStack;
import net.pnovaczek.spellgems.ModComponents;
import net.pnovaczek.spellgems.item.GreaterSpellGems;
import net.pnovaczek.spellgems.item.SpellGemItem;
import net.pnovaczek.spellgems.item.SpellTomeApplier;
import net.pnovaczek.spellgems.item.data.SpellGemData;
import net.pnovaczek.spellgems.spell.affix.AffixForge;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Why two filled gem forge inputs have no result. Synced to the client by ordinal.
 */
public enum GemForgeError {
    NONE(null),
    GENERIC(null),
    INCOMPATIBLE_SPELL("container.spellgems.gem_forge.error.incompatible_spell"),
    NO_AFFIXES_ALLOWED("container.spellgems.gem_forge.error.no_affixes_allowed"),
    MAX_MODIFIER("container.spellgems.gem_forge.error.max_modifier"),
    MAX_STRIKE("container.spellgems.gem_forge.error.max_strike");

    private final @Nullable String messageKey;

    GemForgeError(@Nullable String messageKey) {
        this.messageKey = messageKey;
    }

    public @Nullable String messageKey() {
        return messageKey;
    }

    public static GemForgeError byId(int id) {
        GemForgeError[] values = values();
        return id >= 0 && id < values.length ? values[id] : NONE;
    }

    /** Explains a failed combination. Call only when both slots are filled and no recipe produced a result. */
    public static GemForgeError diagnose(ItemStack first, ItemStack second) {
        GemForgeError tomeError = SpellTomeApplier.diagnose(first, second);
        if (tomeError != NONE) {
            return tomeError;
        }
        if (isAffixTomeWithNonCombatGem(first, second)) {
            return INCOMPATIBLE_SPELL;
        }
        if (wouldCombineWithoutAffixes(first, second)) {
            return NO_AFFIXES_ALLOWED;
        }
        return GENERIC;
    }

    private static boolean isAffixTomeWithNonCombatGem(ItemStack first, ItemStack second) {
        return isAffixTomeOnNonCombatGem(first, second) || isAffixTomeOnNonCombatGem(second, first);
    }

    private static boolean isAffixTomeOnNonCombatGem(ItemStack gem, ItemStack tome) {
        return gem.getItem() instanceof SpellGemItem
                && !AffixForge.isCombatGem(gem)
                && AffixForge.isAffixTome(tome);
    }

    private static boolean wouldCombineWithoutAffixes(ItemStack first, ItemStack second) {
        if (!(first.getItem() instanceof SpellGemItem) || !(second.getItem() instanceof SpellGemItem)) {
            return false;
        }
        SpellGemData dataA = first.get(ModComponents.SPELL_GEM_DATA);
        SpellGemData dataB = second.get(ModComponents.SPELL_GEM_DATA);
        if (dataA == null || dataB == null || (dataA.affixes().isEmpty() && dataB.affixes().isEmpty())) {
            return false;
        }
        ItemStack strippedA = first.copy();
        strippedA.set(ModComponents.SPELL_GEM_DATA, dataA.withAffixes(List.of()));
        ItemStack strippedB = second.copy();
        strippedB.set(ModComponents.SPELL_GEM_DATA, dataB.withAffixes(List.of()));
        return GreaterSpellGems.tryCombine(strippedA, strippedB).isPresent();
    }
}
