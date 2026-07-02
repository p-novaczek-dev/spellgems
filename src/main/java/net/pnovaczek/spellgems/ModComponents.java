package net.pnovaczek.spellgems;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.pnovaczek.spellgems.item.data.SpellGemData;
import net.pnovaczek.spellgems.item.data.TomeData;
import net.pnovaczek.spellgems.item.data.WandData;

import java.util.function.UnaryOperator;

public final class ModComponents {

    public static final DataComponentType<SpellGemData> SPELL_GEM_DATA = register(
            "spell_gem_data",
            builder -> builder
                    .persistent(SpellGemData.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(SpellGemData.CODEC))
    );

    public static final DataComponentType<TomeData> TOME_DATA = register(
            "tome_data",
            builder -> builder
                    .persistent(TomeData.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(TomeData.CODEC))
    );

    public static final DataComponentType<WandData> WAND_DATA = register(
            "wand_data",
            builder -> builder
                    .persistent(WandData.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(WandData.CODEC))
    );

    private static <T> DataComponentType<T> register(
            String name,
            UnaryOperator<DataComponentType.Builder<T>> builderOperator
    ) {
        Identifier id = Identifier.fromNamespaceAndPath(Spellgems.MOD_ID, name);
        DataComponentType.Builder<T> builder = DataComponentType.builder();
        return Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                id,
                builderOperator.apply(builder).build()
        );
    }

    public static void initialize() {
        // forces static initialization
    }
}