package net.pnovaczek.spellgems.spell;

import net.minecraft.world.phys.EntityHitResult;
import net.pnovaczek.spellgems.entity.SpellProjectile;

@FunctionalInterface
public interface ProjectileHitHandler {
    void onHit(SpellProjectile projectile, EntityHitResult result);
}