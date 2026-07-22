package net.pnovaczek.spellgems.spell.enchantment;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;

public record ModifierEnchantment(Identifier id) {

    public static final Codec<ModifierEnchantment> CODEC = Identifier.CODEC.xmap(
            ModifierEnchantment::new,
            ModifierEnchantment::id
    );

    public boolean is(Identifier strikeId) {
        return id.equals(strikeId);
    }

    public String tooltipNameKey() {
        return "tooltip.spellgems.spell_enchantment." + id.getPath() + ".name";
    }

    public String tooltipDescriptionKey() {
        return "tooltip.spellgems.spell_enchantment." + id.getPath() + ".description";
    }
}
