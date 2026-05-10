package net.pnovaczek.spellgems.item.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.spell.enchantment.ModifierEnchantment;
import net.pnovaczek.spellgems.spell.enchantment.PotionEnchantment;
import net.pnovaczek.spellgems.spell.enchantment.StrikeEnchantment;
import net.pnovaczek.spellgems.spell.enchantment.UtilityEnchantment;

import java.util.List;

public record SpellGemData(
        Identifier spellId,
        List<ModifierEnchantment> modifierEffects,
        List<StrikeEnchantment> strikeEffects,
        List<UtilityEnchantment> utilityEffects,
        List<PotionEnchantment> potionEffects
) {

    public static final Codec<SpellGemData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("spell_id").forGetter(SpellGemData::spellId),
                    ModifierEnchantment.CODEC.listOf().fieldOf("modifiers").forGetter(SpellGemData::modifierEffects),
                    StrikeEnchantment.CODEC.listOf().fieldOf("strikes").forGetter(SpellGemData::strikeEffects),
                    UtilityEnchantment.CODEC.listOf().fieldOf("utilities").forGetter(SpellGemData::utilityEffects),
                    PotionEnchantment.CODEC.listOf().fieldOf("potions").forGetter(SpellGemData::potionEffects)
            ).apply(instance, SpellGemData::new)
    );

    public static SpellGemData create(Identifier identifier) {
        return new SpellGemData(identifier, List.of(), List.of(), List.of(), List.of());
    }

    public boolean isEnchanted() {
        return
                !modifierEffects.isEmpty() ||
                !strikeEffects.isEmpty() ||
                !utilityEffects.isEmpty() ||
                !potionEffects.isEmpty();
    }
}
