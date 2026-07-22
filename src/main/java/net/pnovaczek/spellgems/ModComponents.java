package net.pnovaczek.spellgems;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.pnovaczek.spellgems.item.data.AstralBowData;
import net.pnovaczek.spellgems.item.data.SpellGemData;
import net.pnovaczek.spellgems.item.data.TomeData;
import net.pnovaczek.spellgems.item.data.WandData;
import net.pnovaczek.spellgems.registry.ModRegistry;

import java.util.function.UnaryOperator;

/**
 * Data component types. Fields are assigned in {@link #register()} (not class-init).
 */
public final class ModComponents {

    public static DataComponentType<SpellGemData> SPELL_GEM_DATA;
    public static DataComponentType<TomeData> TOME_DATA;
    public static DataComponentType<WandData> WAND_DATA;
    public static DataComponentType<AstralBowData> ASTRAL_BOW_DATA;

    private ModComponents() {
    }

    public static void register() {
        SPELL_GEM_DATA = register(
                "spell_gem_data",
                builder -> builder
                        .persistent(SpellGemData.CODEC)
                        .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(SpellGemData.CODEC))
        );
        TOME_DATA = register(
                "tome_data",
                builder -> builder
                        .persistent(TomeData.CODEC)
                        .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(TomeData.CODEC))
        );
        WAND_DATA = register(
                "wand_data",
                builder -> builder
                        .persistent(WandData.CODEC)
                        .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(WandData.CODEC))
        );
        ASTRAL_BOW_DATA = register(
                "astral_bow_data",
                builder -> builder
                        .persistent(AstralBowData.CODEC)
                        .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(AstralBowData.CODEC))
        );
    }

    private static <T> DataComponentType<T> register(
            String name,
            UnaryOperator<DataComponentType.Builder<T>> builderOperator
    ) {
        DataComponentType.Builder<T> builder = DataComponentType.builder();
        return ModRegistry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                name,
                builderOperator.apply(builder).build()
        );
    }
}
