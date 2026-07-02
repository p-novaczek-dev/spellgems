package net.pnovaczek.spellgems.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.Spellgems;

public record WandCycleSpellPayload(int direction) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<WandCycleSpellPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "wand_cycle_spell"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WandCycleSpellPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, WandCycleSpellPayload::direction,
                    WandCycleSpellPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}