package net.pnovaczek.spellgems.spell;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.pnovaczek.spellgems.ModComponents;
import net.pnovaczek.spellgems.ModItems;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.inventory.WandContainer;
import net.pnovaczek.spellgems.item.SpellGemItem;
import net.pnovaczek.spellgems.item.data.InventoryBinding;
import net.pnovaczek.spellgems.item.data.SpellGemData;
import net.pnovaczek.spellgems.item.data.WandData;
import net.pnovaczek.spellgems.spell.enchantment.UtilityEnchantments;
import org.jspecify.annotations.Nullable;

/**
 * Moves stacks from the targeted inventory into an inventory bound to the gem.
 * Inventories are the same ones a hopper can reach, including double chests and mod containers.
 */
public class ItemTransport extends AbstractSpell {

    private static final float TELEPORT_VOLUME = 0.75F;
    private static final float SUCCESS_PITCH = 1.0F;
    private static final float FAIL_PITCH = 0.5F;
    private static final float PICKUP_VOLUME = 0.5F;
    private static final float PICKUP_PITCH = 1.0F;
    private static final float PICKUP_LOW_PITCH = 0.25F;

    @Override
    public Identifier id() {
        return SpellIds.ITEM_TRANSPORT;
    }

    @Override
    public boolean repeatWhileHeld() {
        return false;
    }

    /**
     * Sneak right-click on an inventory binds the gem. Other clicks are left to the block.
     */
    public static InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player == null || !player.isShiftKeyDown() || !isTransportGem(stack)) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (HopperBlockEntity.getContainerAt(level, pos) == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            stack.set(ModComponents.INVENTORY_BINDING, new InventoryBinding(level.dimension(), pos.immutable()));
            playPickup(level, player.getX(), player.getY(), player.getZ(), PICKUP_PITCH);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Sneak right-click in the air clears the binding and does not cast.
     *
     * @return {@code true} when this click was the clear action
     */
    public static boolean clearBinding(Player player, ItemStack stack) {
        if (!player.isShiftKeyDown() || !isTransportGem(stack)) {
            return false;
        }
        if (!player.level().isClientSide() && stack.has(ModComponents.INVENTORY_BINDING)) {
            stack.remove(ModComponents.INVENTORY_BINDING);
            playPickup(player.level(), player.getX(), player.getY(), player.getZ(), PICKUP_LOW_PITCH);
        }
        return true;
    }

    @Override
    protected boolean performCast(SpellContext context) {
        if (context.level().isClientSide() || !(context.level() instanceof ServerLevel level)) {
            return false;
        }

        BlockPos sourcePos;
        if (context.isDispenserCast()) {
            sourcePos = context.originBlockPos();
        } else if (context.caster() instanceof Player player) {
            BlockHitResult hit = SpellTargeting.resolveBlockHit(context, player.blockInteractionRange());
            if (hit == null) {
                return false;
            }
            sourcePos = hit.getBlockPos();
        } else {
            return false;
        }

        Container source = HopperBlockEntity.getContainerAt(level, sourcePos);
        if (source == null) {
            return false;
        }

        ItemStack gem = gemStack(context);
        InventoryBinding binding = gem.get(ModComponents.INVENTORY_BINDING);
        Vec3 soundPos = Vec3.atCenterOf(sourcePos);
        if (binding == null) {
            playPickup(context, soundPos, PICKUP_LOW_PITCH);
            return false;
        }

        ServerLevel destinationLevel = level.getServer().getLevel(binding.dimension());
        BlockPos destinationPos = binding.pos();
        Container destination = destinationLevel == null || !destinationLevel.isLoaded(destinationPos)
                ? null
                : HopperBlockEntity.getContainerAt(destinationLevel, destinationPos);
        if (destination == null) {
            playTeleport(context, soundPos, FAIL_PITCH);
            return false;
        }
        if (sameInventory(level, sourcePos, destinationLevel, destinationPos)) {
            return false;
        }

        int maxStacks = Math.max(1, Spellgems.CONFIG.spells.itemTransport.maxStacks);
        boolean voidLeftovers = hasVoid(context.data());
        boolean acted = false;
        int attempted = 0;

        for (int slot = 0; slot < source.getContainerSize() && attempted < maxStacks; slot++) {
            ItemStack stack = source.getItem(slot);
            if (stack.isEmpty() || !canExtract(source, destination, slot, stack)) {
                continue;
            }
            attempted++;

            ItemStack moving = source.removeItem(slot, stack.getCount());
            ItemStack leftover = insertInto(destination, moving);
            if (voidLeftovers && !leftover.isEmpty()) {
                leftover = ItemStack.EMPTY;
            }
            if (leftover.getCount() != moving.getCount()) {
                acted = true;
            }
            if (!leftover.isEmpty()) {
                source.setItem(slot, leftover);
            }
        }

        if (!acted) {
            return false;
        }

        source.setChanged();
        destination.setChanged();
        playTeleport(context, soundPos, SUCCESS_PITCH);
        return true;
    }

    private static void playTeleport(SpellContext context, Vec3 pos, float pitch) {
        SpellSounds.play(
                context,
                pos,
                SoundEvents.PLAYER_TELEPORT,
                SoundSource.PLAYERS,
                TELEPORT_VOLUME,
                pitch
        );
    }

    private static void playPickup(Level level, double x, double y, double z, float pitch) {
        level.playSound(null, x, y, z, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, PICKUP_VOLUME, pitch);
    }

    private static void playPickup(SpellContext context, Vec3 pos, float pitch) {
        SpellSounds.play(
                context,
                pos,
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS,
                PICKUP_VOLUME,
                pitch
        );
    }

    private static ItemStack gemStack(SpellContext context) {
        ItemStack casting = context.castingItem();
        if (!casting.is(ModItems.WAND)) {
            return casting;
        }
        SimpleContainer slots = new SimpleContainer(WandContainer.SIZE);
        WandContainer.loadInto(slots, casting);
        int slot = Mth.clamp(
                casting.getOrDefault(ModComponents.WAND_DATA, WandData.DEFAULT).selectedSlot(),
                0,
                WandContainer.SIZE - 1
        );
        return slots.getItem(slot);
    }

    private static boolean isTransportGem(ItemStack stack) {
        SpellGemData data = SpellGemItem.getSpellData(stack);
        return data != null && SpellIds.ITEM_TRANSPORT.equals(data.spellId());
    }

    private static boolean hasVoid(@Nullable SpellGemData data) {
        return data != null && data.utilityEffects().stream().anyMatch(effect -> effect.is(UtilityEnchantments.VOID));
    }

    private static boolean sameInventory(Level sourceLevel, BlockPos sourcePos, Level destinationLevel, BlockPos destinationPos) {
        if (sourceLevel != destinationLevel) {
            return false;
        }
        if (sourcePos.equals(destinationPos)) {
            return true;
        }
        BlockState state = sourceLevel.getBlockState(sourcePos);
        if (state.getBlock() instanceof ChestBlock && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            return ChestBlock.getConnectedBlockPos(sourcePos, state).equals(destinationPos);
        }
        return false;
    }

    private static boolean canExtract(Container source, Container destination, int slot, ItemStack stack) {
        if (!source.canTakeItem(destination, slot, stack)) {
            return false;
        }
        if (!(source instanceof WorldlyContainer worldly)) {
            return true;
        }
        for (Direction face : Direction.values()) {
            if (worldly.canTakeItemThroughFace(slot, stack, face)) {
                return true;
            }
        }
        return false;
    }

    private static boolean canInsert(Container destination, int slot, ItemStack stack) {
        if (!destination.canPlaceItem(slot, stack)) {
            return false;
        }
        if (!(destination instanceof WorldlyContainer worldly)) {
            return true;
        }
        for (Direction face : Direction.values()) {
            if (worldly.canPlaceItemThroughFace(slot, stack, face)) {
                return true;
            }
        }
        return false;
    }

    private static ItemStack insertInto(Container destination, ItemStack stack) {
        ItemStack remaining = stack.copy();
        if (remaining.isEmpty()) {
            return remaining;
        }

        for (int slot = 0; slot < destination.getContainerSize() && !remaining.isEmpty(); slot++) {
            ItemStack current = destination.getItem(slot);
            if (current.isEmpty() || !ItemStack.isSameItemSameComponents(current, remaining)) {
                continue;
            }
            if (!canInsert(destination, slot, remaining)) {
                continue;
            }
            int space = destination.getMaxStackSize(remaining) - current.getCount();
            if (space <= 0) {
                continue;
            }
            int move = Math.min(space, remaining.getCount());
            current.grow(move);
            remaining.shrink(move);
            destination.setItem(slot, current);
        }

        for (int slot = 0; slot < destination.getContainerSize() && !remaining.isEmpty(); slot++) {
            if (!destination.getItem(slot).isEmpty() || !canInsert(destination, slot, remaining)) {
                continue;
            }
            int move = Math.min(destination.getMaxStackSize(remaining), remaining.getCount());
            destination.setItem(slot, remaining.split(move));
        }
        return remaining;
    }
}
