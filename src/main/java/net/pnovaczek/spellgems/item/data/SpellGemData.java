package net.pnovaczek.spellgems.item.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
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
