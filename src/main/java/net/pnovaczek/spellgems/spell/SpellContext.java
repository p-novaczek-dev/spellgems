package net.pnovaczek.spellgems.spell;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.pnovaczek.spellgems.item.data.SpellGemData;

public record SpellContext(
        Level level,
        LivingEntity caster,
        ItemStack castingItem,
        SpellGemData data
) {
    public Vec3 lookAngle() {
        return caster.getLookAngle().normalize();
    }
}
