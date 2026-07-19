package net.pnovaczek.spellgems.spell;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.pnovaczek.spellgems.item.data.SpellGemData;
import org.jspecify.annotations.Nullable;

/**
 * Immutable cast parameters for a single spell invocation.
 * Geometry ({@link #origin}, {@link #lookDirection}) and optional {@link #itemSource}
 * are explicit so non-player casters (e.g. spell dispenser) can reuse the same pipeline.
 */
public record SpellContext(
        Level level,
        @Nullable LivingEntity caster,
        ItemStack castingItem,
        SpellGemData data,
        CastSource source,
        Vec3 origin,
        Vec3 lookDirection,
        @Nullable Container itemSource
) {
    public SpellContext {
        if (origin == null) {
            throw new IllegalArgumentException("origin must not be null");
        }
        if (lookDirection == null) {
            throw new IllegalArgumentException("lookDirection must not be null");
        }
        lookDirection = lookDirection.lengthSqr() < 1.0E-8
                ? new Vec3(0.0, 0.0, 1.0)
                : lookDirection.normalize();
        if (source == null) {
            source = CastSource.HAND;
        }
        if (castingItem == null) {
            castingItem = ItemStack.EMPTY;
        }
    }

    /** Player hand-cast of a spell gem. Origin at feet; look from player. */
    public static SpellContext forHand(Level level, Player player, ItemStack castingItem, SpellGemData data) {
        return forPlayer(level, player, castingItem, data, CastSource.HAND, null);
    }

    /** Wand cast. Origin at feet; look from player. */
    public static SpellContext forWand(Level level, Player player, ItemStack wand, SpellGemData data) {
        return forPlayer(level, player, wand, data, CastSource.WAND, null);
    }

    /**
     * Dispenser (or other machine) cast. {@code caster} may be null.
     * {@code itemSource} is the machine inventory used by feed/grow/plant/place.
     */
    public static SpellContext forDispenser(
            Level level,
            @Nullable LivingEntity caster,
            ItemStack castingItem,
            SpellGemData data,
            Vec3 origin,
            Vec3 lookDirection,
            @Nullable Container itemSource
    ) {
        return new SpellContext(
                level,
                caster,
                castingItem,
                data,
                CastSource.DISPENSER,
                origin,
                lookDirection,
                itemSource
        );
    }

    private static SpellContext forPlayer(
            Level level,
            Player player,
            ItemStack castingItem,
            SpellGemData data,
            CastSource source,
            @Nullable Container itemSource
    ) {
        return new SpellContext(
                level,
                player,
                castingItem,
                data,
                source,
                player.position(),
                player.getLookAngle(),
                itemSource
        );
    }

    public Vec3 lookAngle() {
        return lookDirection;
    }

    /** Eye-height origin when a living caster is present; otherwise {@link #origin}. */
    public Vec3 eyeOrigin() {
        if (caster != null) {
            return new Vec3(caster.getX(), caster.getEyeY(), caster.getZ());
        }
        return origin;
    }

    public BlockPos originBlockPos() {
        return BlockPos.containing(origin);
    }

    public boolean isWandCast() {
        return source.isWand();
    }

    public boolean isDispenserCast() {
        return source.isDispenser();
    }

    public boolean appliesPlayerItemCooldown() {
        return source.appliesPlayerItemCooldown();
    }

    public boolean hasLivingCaster() {
        return caster != null && caster.isAlive();
    }

    /**
     * Inventory used by spells that consume items.
     * Prefer an explicit {@link #itemSource}; otherwise the player's hotbar when caster is a player.
     */
    public @Nullable Container resolveItemSource() {
        if (itemSource != null) {
            return itemSource;
        }
        if (caster instanceof Player player) {
            return player.getInventory();
        }
        return null;
    }

    /**
     * Whether item picking should only consider the hotbar (first 9 slots).
     * True for player hand/wand casts without an explicit container; false for machine inventories.
     */
    public boolean useHotbarOnly() {
        return itemSource == null && caster instanceof Player;
    }
}
