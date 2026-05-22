package net.pnovaczek.spellgems;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityDataRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;

public class ModEntityDataSerializers {

    public static final EntityDataSerializer<CompoundTag> SPELL_GEM_SERIALIZER =
            EntityDataSerializer.forValueType(ByteBufCodecs.COMPOUND_TAG);

    public static void register() {
        FabricEntityDataRegistry.register(
                Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "spell_gem_serializer"),
                SPELL_GEM_SERIALIZER
        );
    }

    private ModEntityDataSerializers() {
    }
}