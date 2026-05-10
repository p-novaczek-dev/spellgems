package net.pnovaczek.spellgems.spell.enchantment;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.world.item.alchemy.Potion;

public record PotionEnchantment(Holder<Potion> potion) {

    public static final Codec<PotionEnchantment> CODEC = Potion.CODEC
            .xmap(PotionEnchantment::new, PotionEnchantment::potion);

}
