package net.pnovaczek.spellgems.client.renderer.item.properties.numeric;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.client.renderer.item.properties.numeric.UseDuration;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.pnovaczek.spellgems.item.AstralBowItem;
import org.jspecify.annotations.Nullable;

public class AstralBowPull implements RangeSelectItemModelProperty {
    public static final MapCodec<AstralBowPull> MAP_CODEC = MapCodec.unit(new AstralBowPull());

    @Override
    public float get(final ItemStack itemStack, @Nullable final ClientLevel level, @Nullable final ItemOwner owner, final int seed) {
        LivingEntity entity = owner == null ? null : owner.asLivingEntity();
        if (entity == null) {
            return 0.0F;
        }

        int drawDuration = AstralBowItem.getDrawDurationTicks(itemStack, entity);
        if (drawDuration <= 0) {
            return 1.0F;
        }

        return (float) UseDuration.useDuration(itemStack, entity) / drawDuration;
    }

    @Override
    public MapCodec<AstralBowPull> type() {
        return MAP_CODEC;
    }
}