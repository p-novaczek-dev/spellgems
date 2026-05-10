package net.pnovaczek.spellgems.spell.enchantment;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;

public record UtilityEnchantment(Identifier id) {

    public static final Codec<UtilityEnchantment> CODEC = Identifier.CODEC.xmap(
            UtilityEnchantment::new,
            UtilityEnchantment::id
    );

    public String tooltipNameKey() {
        return "tooltip.spellgems.spell_enchantment." + id.getPath() + ".name";
    }

    public String tooltipDescriptionKey() {
        return "tooltip.spellgems.spell_enchantment." + id.getPath() + ".description";
    }
}

