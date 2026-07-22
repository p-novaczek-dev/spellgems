package net.pnovaczek.spellgems;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.pnovaczek.spellgems.platform.Platform;
import net.pnovaczek.spellgems.registry.ModRegistry;

public class ModEntityDataSerializers {

    /**
     * Serializer instance is created eagerly (needed by entity class static accessors).
     * Registry binding happens in {@link #register()}.
     */
    public static final EntityDataSerializer<CompoundTag> SPELL_GEM_SERIALIZER =
            EntityDataSerializer.forValueType(ByteBufCodecs.COMPOUND_TAG);

    private ModEntityDataSerializers() {
    }

    public static void register() {
        Platform.registries().registerEntityDataSerializer(
                ModRegistry.id("spell_gem_serializer"),
                SPELL_GEM_SERIALIZER
        );
    }
}
