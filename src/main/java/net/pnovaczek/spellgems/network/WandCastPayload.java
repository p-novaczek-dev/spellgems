package net.pnovaczek.spellgems.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.Spellgems;

public record WandCastPayload() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<WandCastPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "wand_cast"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WandCastPayload> CODEC =
            StreamCodec.unit(new WandCastPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}