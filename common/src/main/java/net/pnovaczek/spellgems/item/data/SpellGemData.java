package net.pnovaczek.spellgems.item.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.spell.enchantment.ModifierEnchantment;
import net.pnovaczek.spellgems.spell.enchantment.PotionEnchantment;
import net.pnovaczek.spellgems.spell.enchantment.SpellAffix;
import net.pnovaczek.spellgems.spell.enchantment.StrikeEnchantment;
import net.pnovaczek.spellgems.spell.enchantment.UtilityEnchantment;

import java.util.List;
import java.util.Optional;

public record SpellGemData(
        Identifier spellId,
        List<ModifierEnchantment> modifierEffects,
        List<StrikeEnchantment> strikeEffects,
        List<UtilityEnchantment> utilityEffects,
        List<PotionEnchantment> potionEffects,
        List<SpellAffix> affixes,
        Optional<SpellGemData> followUp
) {

    public static final Codec<SpellGemData> CODEC = Codec.recursive("spell_gem_data", self ->
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Identifier.CODEC.fieldOf("spell_id").forGetter(SpellGemData::spellId),
                            ModifierEnchantment.CODEC.listOf().fieldOf("modifiers").forGetter(SpellGemData::modifierEffects),
                            StrikeEnchantment.CODEC.listOf().fieldOf("strikes").forGetter(SpellGemData::strikeEffects),
                            UtilityEnchantment.CODEC.listOf().fieldOf("utilities").forGetter(SpellGemData::utilityEffects),
                            PotionEnchantment.CODEC.listOf().fieldOf("potions").forGetter(SpellGemData::potionEffects),
                            SpellAffix.CODEC.listOf().optionalFieldOf("affixes", List.of()).forGetter(SpellGemData::affixes),
                            self.optionalFieldOf("follow_up").forGetter(SpellGemData::followUp)
                    ).apply(instance, SpellGemData::new)
            )
    );

    public SpellGemData {
        modifierEffects = copy(modifierEffects);
        strikeEffects = copy(strikeEffects);
        utilityEffects = copy(utilityEffects);
        potionEffects = copy(potionEffects);
        affixes = copy(affixes);
        if (followUp == null) {
            followUp = Optional.empty();
        }
    }

    public SpellGemData(
            Identifier spellId,
            List<ModifierEnchantment> modifierEffects,
            List<StrikeEnchantment> strikeEffects,
            List<UtilityEnchantment> utilityEffects,
            List<PotionEnchantment> potionEffects
    ) {
        this(spellId, modifierEffects, strikeEffects, utilityEffects, potionEffects, List.of(), Optional.empty());
    }

    public static SpellGemData create(Identifier identifier) {
        return new SpellGemData(identifier, List.of(), List.of(), List.of(), List.of());
    }

    public SpellGemData withModifiers(List<ModifierEnchantment> modifiers) {
        return new SpellGemData(spellId, modifiers, strikeEffects, utilityEffects, potionEffects, affixes, followUp);
    }

    public SpellGemData withStrikes(List<StrikeEnchantment> strikes) {
        return new SpellGemData(spellId, modifierEffects, strikes, utilityEffects, potionEffects, affixes, followUp);
    }

    public SpellGemData withUtilities(List<UtilityEnchantment> utilities) {
        return new SpellGemData(spellId, modifierEffects, strikeEffects, utilities, potionEffects, affixes, followUp);
    }

    public SpellGemData withPotions(List<PotionEnchantment> potions) {
        return new SpellGemData(spellId, modifierEffects, strikeEffects, utilityEffects, potions, affixes, followUp);
    }

    public SpellGemData withAffixes(List<SpellAffix> affixes) {
        return new SpellGemData(spellId, modifierEffects, strikeEffects, utilityEffects, potionEffects, affixes, followUp);
    }

    public SpellGemData withFollowUp(SpellGemData followUp) {
        return new SpellGemData(
                spellId, modifierEffects, strikeEffects, utilityEffects, potionEffects, affixes, Optional.of(followUp));
    }

    public SpellGemData withoutFollowUp() {
        if (followUp.isEmpty()) {
            return this;
        }
        return new SpellGemData(
                spellId, modifierEffects, strikeEffects, utilityEffects, potionEffects, affixes, Optional.empty());
    }

    private static <T> List<T> copy(List<T> list) {
        return list == null ? List.of() : List.copyOf(list);
    }

    public boolean isEnchanted() {
        return enchantmentCount() > 0
                || followUp.map(SpellGemData::isEnchanted).orElse(false);
    }

    public int enchantmentCount() {
        return modifierEffects.size()
                + strikeEffects.size()
                + utilityEffects.size()
                + potionEffects.size();
    }

    public CompoundTag save(CompoundTag tag) {
        return CODEC.encodeStart(NbtOps.INSTANCE, this)
                .result()
                .map(nbt -> (CompoundTag) nbt)
                .orElse(tag);
    }

    public static SpellGemData load(CompoundTag tag) {
        return CODEC.parse(NbtOps.INSTANCE, tag)
                .result()
                .orElse(null);
    }

    public Integer getTintColor() {
        if (strikeEffects.isEmpty()) {
            return 0xFFFFFF;
        }

        long r = 0, g = 0, b = 0;
        int count = strikeEffects.size();

        for (StrikeEnchantment strike : strikeEffects) {
            int color = strike.getTintColor();
            r += (color >> 16) & 0xFF;
            g += (color >> 8) & 0xFF;
            b += color & 0xFF;
        }

        r /= count;
        g /= count;
        b /= count;

        return (int) ((r << 16) | (g << 8) | b);
    }
}
