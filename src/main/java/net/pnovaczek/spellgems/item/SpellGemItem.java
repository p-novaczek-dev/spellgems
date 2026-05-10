package net.pnovaczek.spellgems.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.pnovaczek.spellgems.ModComponents;
import net.pnovaczek.spellgems.spell.Spell;
import net.pnovaczek.spellgems.spell.SpellContext;
import net.pnovaczek.spellgems.item.data.SpellGemData;
import net.pnovaczek.spellgems.ModSpells;

public class SpellGemItem extends Item {

    public SpellGemItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        SpellGemData spellData = getSpellData(stack);
        Spell spell = getSpell(spellData);
        if (spell != null) {
            SpellContext context = new SpellContext(level, player, stack, spellData);
            if (spell.canCast(context))
                spell.cast(context);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        if (stack.isEmpty())
            return false;
        var data = getSpellData(stack);
        if (data == null)
            return false;
        return data.isEnchanted();
    }

    public static SpellGemData getSpellData(ItemStack stack) {
        if (stack.isEmpty())
            return null;
        return stack.get(ModComponents.SPELL_GEM_DATA);
    }

    public static Spell getSpell(SpellGemData data) {
        if (data == null)
            return null;
        return ModSpells.get(data.spellId());
    }
}