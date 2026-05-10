package net.pnovaczek.spellgems;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.pnovaczek.spellgems.entity.AstralArrow;
import net.pnovaczek.spellgems.entity.SpellProjectile;

public class ModEntities {

    public static final EntityType<AstralArrow> ASTRAL_ARROW = register(
            "astral_arrow",
            EntityType.Builder.<AstralArrow>of(
                (entityType, level) -> new AstralArrow((EntityType<? extends net.minecraft.world.entity.projectile.arrow.Arrow>) entityType, level), MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .eyeHeight(0.13F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
    );


    public static final EntityType<SpellProjectile> SPELL_PROJECTILE = register(
            "spell_projectile",
            EntityType.Builder.<SpellProjectile>of(
                (entityType, level) -> new SpellProjectile((EntityType<SpellProjectile>) entityType, level), MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4));

    private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> entityKey = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, name));
        EntityType<T> entityType = builder.build(entityKey);
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, entityKey, entityType);
    }

    public static void initialize() {
        // forces static initialization
    }
}