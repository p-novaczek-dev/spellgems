package net.pnovaczek.spellgems.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.Spellgems;

public record WandQuickCastPayload(int slot) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<WandQuickCastPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "wand_quick_cast"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WandQuickCastPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, WandQuickCastPayload::slot,
                    WandQuickCastPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}