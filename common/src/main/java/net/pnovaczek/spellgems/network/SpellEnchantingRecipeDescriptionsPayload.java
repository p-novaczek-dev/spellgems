package net.pnovaczek.spellgems.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.Spellgems;

import java.util.ArrayList;
import java.util.List;

public record SpellEnchantingRecipeDescriptionsPayload(int containerId, List<String> descriptions)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SpellEnchantingRecipeDescriptionsPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(
                    Spellgems.MOD_ID, "spell_enchanting_recipe_descriptions"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpellEnchantingRecipeDescriptionsPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, SpellEnchantingRecipeDescriptionsPayload::containerId,
                    ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
                    SpellEnchantingRecipeDescriptionsPayload::descriptions,
                    SpellEnchantingRecipeDescriptionsPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}