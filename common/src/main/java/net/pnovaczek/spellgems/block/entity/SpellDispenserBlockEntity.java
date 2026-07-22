package net.pnovaczek.spellgems.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.pnovaczek.spellgems.ModBlockEntities;
import net.pnovaczek.spellgems.ModTags;
import net.pnovaczek.spellgems.Spellgems;
import net.pnovaczek.spellgems.block.SpellDispenserBlock;
import net.pnovaczek.spellgems.item.SpellGemItem;
import net.pnovaczek.spellgems.item.data.SpellGemData;
import net.pnovaczek.spellgems.screen.SpellDispenserMenu;
import net.pnovaczek.spellgems.spell.Spell;
import net.pnovaczek.spellgems.spell.SpellContext;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class SpellDispenserBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {

    public static final int SLOT_SPELL_GEM = 0;
    public static final int MATERIAL_SLOTS = 9;
    public static final int CONTAINER_SIZE = 1 + MATERIAL_SLOTS;
    public static final int FIRST_MATERIAL_SLOT = 1;
    public static final int LAST_MATERIAL_SLOT = CONTAINER_SIZE - 1;

    private static final int[] MATERIAL_SLOT_ARRAY = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    private static final int[] GEM_SLOT_ARRAY = {0};
    private static final double ABSORB_RADIUS = 0.55;
    private static final double ABSORB_RADIUS_SQR = ABSORB_RADIUS * ABSORB_RADIUS;

    private final NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    private final Container materialView = new MaterialSlotView();

    /** 0=cooldownRemaining, 1=cooldownMax, 2=burnoutRemaining, 3=burnoutMax */
    private final ContainerData data = new SimpleContainerData(4);

    private int cooldownRemaining;
    private int cooldownMax;
    private int burnoutRemaining;
    private int burnoutMax;

    public SpellDispenserBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPELL_DISPENSER, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SpellDispenserBlockEntity be) {
        be.tickServer(level, pos, state);
    }

    private void tickServer(Level level, BlockPos pos, BlockState state) {
        boolean changed = false;
        if (cooldownRemaining > 0) {
            cooldownRemaining--;
            changed = true;
        }
        if (burnoutRemaining > 0) {
            burnoutRemaining--;
            changed = true;
        }
        if (changed) {
            syncData();
            setChanged();
        }
        absorbItemsAtFront(level, pos, state);
    }

    public void tryCastFromRedstone() {
        if (level == null || level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (burnoutRemaining > 0) {
            playFailSound(serverLevel);
            return;
        }

        ItemStack gemStack = getItem(SLOT_SPELL_GEM);
        if (gemStack.isEmpty() || !isSpellGem(gemStack)) {
            playFailSound(serverLevel);
            return;
        }

        SpellGemData gemData = SpellGemItem.getSpellData(gemStack);
        Spell spell = SpellGemItem.getSpell(gemData);
        if (spell == null || gemData == null) {
            playFailSound(serverLevel);
            return;
        }

        Direction facing = getFacing();
        Vec3 origin = Vec3.atCenterOf(worldPosition.relative(facing));
        Vec3 look = Vec3.atLowerCornerOf(facing.getUnitVec3i());

        SpellContext context = SpellContext.forDispenser(
                serverLevel,
                null,
                gemStack,
                gemData,
                origin,
                look,
                materialView
        );

        if (!spell.canCast(context)) {
            playFailSound(serverLevel);
            return;
        }

        boolean wasOnCooldown = cooldownRemaining > 0;
        spell.cast(context);

        int cd = Spellgems.CONFIG.getDispenserCooldownTicks(spell.id());
        cooldownMax = Math.max(0, cd);
        cooldownRemaining = cooldownMax;

        if (wasOnCooldown) {
            burnoutMax = Math.max(0, Spellgems.CONFIG.spellDispenser.burnoutTicks);
            burnoutRemaining = burnoutMax;
            playBurnoutSound(serverLevel);
        }

        syncData();
        setChanged();
    }

    private void absorbItemsAtFront(Level level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        Direction facing = state.getValue(SpellDispenserBlock.FACING);
        Vec3 center = Vec3.atCenterOf(pos.relative(facing));
        AABB box = new AABB(center, center).inflate(ABSORB_RADIUS);
        List<ItemEntity> items = serverLevel.getEntitiesOfClass(
                ItemEntity.class,
                box,
                e -> e.isAlive() && !e.getItem().isEmpty() && e.position().distanceToSqr(center) <= ABSORB_RADIUS_SQR
        );
        if (items.isEmpty()) {
            return;
        }

        boolean changed = false;
        for (ItemEntity entity : items) {
            ItemStack stack = entity.getItem();
            ItemStack remaining = insertIntoMaterials(stack);
            if (remaining.getCount() != stack.getCount()) {
                changed = true;
                if (remaining.isEmpty()) {
                    entity.discard();
                } else {
                    entity.setItem(remaining);
                }
            }
        }
        if (changed) {
            setChanged();
        }
    }

    private ItemStack insertIntoMaterials(ItemStack stack) {
        ItemStack remaining = stack.copy();
        // Merge into existing stacks first, then empty slots.
        for (int slot = FIRST_MATERIAL_SLOT; slot <= LAST_MATERIAL_SLOT && !remaining.isEmpty(); slot++) {
            ItemStack existing = getItem(slot);
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, remaining)) {
                int space = existing.getMaxStackSize() - existing.getCount();
                if (space > 0) {
                    int move = Math.min(space, remaining.getCount());
                    existing.grow(move);
                    remaining.shrink(move);
                    setItem(slot, existing);
                }
            }
        }
        for (int slot = FIRST_MATERIAL_SLOT; slot <= LAST_MATERIAL_SLOT && !remaining.isEmpty(); slot++) {
            if (getItem(slot).isEmpty()) {
                int move = Math.min(remaining.getMaxStackSize(), remaining.getCount());
                setItem(slot, remaining.split(move));
            }
        }
        return remaining;
    }

    private void playFailSound(ServerLevel level) {
        level.playSound(
                null,
                worldPosition,
                SoundEvents.DISPENSER_FAIL,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );
        level.levelEvent(1001, worldPosition, 0);
    }

    private void playBurnoutSound(ServerLevel level) {
        level.playSound(
                null,
                worldPosition,
                SoundEvents.REDSTONE_TORCH_BURNOUT,
                SoundSource.BLOCKS,
                0.5F,
                2.0F
        );
        // Smoke puffs like a burned-out redstone torch
        level.levelEvent(1502, worldPosition, 0);
    }

    public Direction getFacing() {
        return getBlockState().getValue(SpellDispenserBlock.FACING);
    }

    public static boolean isSpellGem(ItemStack stack) {
        return !stack.isEmpty()
                && (stack.is(ModTags.COMBAT_SPELL_GEMS) || stack.is(ModTags.UTILITY_SPELL_GEMS));
    }

    private void syncData() {
        data.set(0, cooldownRemaining);
        data.set(1, cooldownMax);
        data.set(2, burnoutRemaining);
        data.set(3, burnoutMax);
    }

    public ContainerData getContainerData() {
        return data;
    }

    public Container getMaterialView() {
        return materialView;
    }

    // ==================== MENU ====================

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.spellgems.spell_dispenser");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new SpellDispenserMenu(syncId, playerInventory, this, data);
    }

    // ==================== INVENTORY ====================

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (!stack.isEmpty() && stack.getCount() > stack.getMaxStackSize()) {
            stack.setCount(stack.getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == SLOT_SPELL_GEM) {
            return isSpellGem(stack);
        }
        return slot >= FIRST_MATERIAL_SLOT && slot <= LAST_MATERIAL_SLOT;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        // Top: gem only; other faces: materials
        return side == Direction.UP ? GEM_SLOT_ARRAY : MATERIAL_SLOT_ARRAY;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        // Do not hopper-extract the spell gem
        return slot != SLOT_SPELL_GEM;
    }

    // ==================== NBT ====================

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        output.putInt("CooldownRemaining", cooldownRemaining);
        output.putInt("CooldownMax", cooldownMax);
        output.putInt("BurnoutRemaining", burnoutRemaining);
        output.putInt("BurnoutMax", burnoutMax);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items.clear();
        ContainerHelper.loadAllItems(input, items);
        cooldownRemaining = input.getIntOr("CooldownRemaining", 0);
        cooldownMax = input.getIntOr("CooldownMax", 0);
        burnoutRemaining = input.getIntOr("BurnoutRemaining", 0);
        burnoutMax = input.getIntOr("BurnoutMax", 0);
        syncData();
    }

    /**
     * Exposes material slots 1–9 as a contiguous 9-slot container for spell item consumption.
     */
    private final class MaterialSlotView implements Container {
        @Override
        public int getContainerSize() {
            return MATERIAL_SLOTS;
        }

        @Override
        public boolean isEmpty() {
            for (int i = FIRST_MATERIAL_SLOT; i <= LAST_MATERIAL_SLOT; i++) {
                if (!SpellDispenserBlockEntity.this.getItem(i).isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getItem(int slot) {
            return SpellDispenserBlockEntity.this.getItem(FIRST_MATERIAL_SLOT + slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            return SpellDispenserBlockEntity.this.removeItem(FIRST_MATERIAL_SLOT + slot, amount);
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return SpellDispenserBlockEntity.this.removeItemNoUpdate(FIRST_MATERIAL_SLOT + slot);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            SpellDispenserBlockEntity.this.setItem(FIRST_MATERIAL_SLOT + slot, stack);
        }

        @Override
        public void setChanged() {
            SpellDispenserBlockEntity.this.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return SpellDispenserBlockEntity.this.stillValid(player);
        }

        @Override
        public void clearContent() {
            for (int i = FIRST_MATERIAL_SLOT; i <= LAST_MATERIAL_SLOT; i++) {
                SpellDispenserBlockEntity.this.setItem(i, ItemStack.EMPTY);
            }
        }
    }
}
