package net.pnovaczek.spellgems.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.Spellgems;

/**
 * Single serverbound payload for all wand (and shared cycle) client inputs.
 * Replaces separate cast / quick-cast / cycle packet types.
 *
 * @param action which input
 * @param value  action-specific: quick-cast slot index, cycle direction (±1); unused for CAST
 */
public record WandInputPayload(Action action, int value) implements CustomPacketPayload {

    public enum Action {
        /** Cast the currently selected wand spell. */
        CAST,
        /** Cast a specific wand gem slot (quick-cast keybind). */
        QUICK_CAST,
        /** Cycle selected gem on wand or astral bow. */
        CYCLE
    }

    public static final CustomPacketPayload.Type<WandInputPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, "wand_input"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WandInputPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    payload -> payload.action().ordinal(),
                    ByteBufCodecs.VAR_INT,
                    WandInputPayload::value,
                    (actionOrdinal, value) -> {
                        Action[] actions = Action.values();
                        int index = actionOrdinal;
                        if (index < 0 || index >= actions.length) {
                            index = Action.CAST.ordinal();
                        }
                        return new WandInputPayload(actions[index], value);
                    }
            );

    public static WandInputPayload cast() {
        return new WandInputPayload(Action.CAST, 0);
    }

    public static WandInputPayload quickCast(int slot) {
        return new WandInputPayload(Action.QUICK_CAST, slot);
    }

    public static WandInputPayload cycle(int direction) {
        return new WandInputPayload(Action.CYCLE, direction);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
