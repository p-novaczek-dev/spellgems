package net.pnovaczek.spellgems.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.pnovaczek.spellgems.ModBlockEntities;
import net.pnovaczek.spellgems.ModItems;
import net.pnovaczek.spellgems.recipe.ManaInfuserRecipe;
import net.pnovaczek.spellgems.recipe.ManaInfuserRecipeInput;
import net.pnovaczek.spellgems.screen.ManaInfuserMenu;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ManaInfuserBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {

    public static final int CONTAINER_SIZE = 4;
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);
    private RecipeHolder<ManaInfuserRecipe> cachedRecipe = null;
    public int maxProgress = 200; // will be overwritten by recipe
    public int manaBuffer;
    public static final int MAX_MANA = 64;
    private int progress = 0;
    private static final int MAX_PROGRESS = 200; // hard-coded ticks for one craft (will be taken from recipes later)
    private final ContainerData data;

    public ManaInfuserBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_INFUSER, pos, state);
        this.data = new SimpleContainerData(2); // 0 = progress, 1 = mana level
    }

    // ==================== INVENTORY ====================
    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    public boolean isEmpty() {
        return inventory.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ContainerHelper.removeItem(inventory, slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(inventory, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        inventory.set(slot, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        inventory.clear();
    }

    // SidedInventory (hoppers/pipes)
    private static final int[] TOP_SLOTS = {2};      // item to infuse
    private static final int[] SIDE_SLOTS = {0};     // fuel
    private static final int[] BOTTOM_SLOTS = {3};   // output

    @Override
    public int[] getSlotsForFace(Direction side) {
        return switch (side) {
            case DOWN -> BOTTOM_SLOTS;
            case UP -> TOP_SLOTS;
            default -> SIDE_SLOTS;
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return switch (slot) {
            case 0, 2 -> true;
            default -> false;
        };
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot == 3;
    }

    // ==================== MENU PROVIDER ====================
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.spellgems.mana_infuser");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new ManaInfuserMenu(syncId, playerInventory, this, data);
    }

    // ==================== DATA FOR GUI ====================
    public int getProgress() {
        return data.get(0);
    }

    public int getManaLevel() {
        return data.get(1);
    }

    // ==================== NBT ====================
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("ManaBuffer", manaBuffer);
        output.putInt("Progress", progress);
        ContainerHelper.saveAllItems(output, inventory);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.manaBuffer = input.getIntOr("ManaBuffer", 0);
        this.progress = input.getIntOr("Progress", 0);
        ContainerHelper.loadAllItems(input, inventory);
    }

    // ==================== TICKING ====================
    public static void tick(Level level, BlockPos pos, BlockState state, ManaInfuserBlockEntity blockEntity) {
        boolean wasLit = state.getValue(BlockStateProperties.LIT);

        if (level instanceof ServerLevel serverLevel) {
            // Fuel handling (server-only)
            if (!blockEntity.getItem(0).isEmpty() && blockEntity.manaBuffer < MAX_MANA) {
                ItemStack fuel = blockEntity.getItem(0);
                if (fuel.is(ModItems.MANA_ESSENCE)) {
                    int amount = Math.min(fuel.getCount(), (MAX_MANA - blockEntity.manaBuffer));
                    if (amount > 0) {
                        blockEntity.removeItem(0, amount);
                        blockEntity.manaBuffer += amount;
                        blockEntity.setChanged();
                    }
                }
            }

            ManaInfuserRecipeInput recipeInput = new ManaInfuserRecipeInput(
                    blockEntity.getItem(1),  // infusing ingredient
                    blockEntity.getItem(2)   // item to infuse
            );

            // Crafting logic
            if (blockEntity.progress > 0) {
                if (blockEntity.cachedRecipe == null ||
                        !blockEntity.cachedRecipe.value().matches(recipeInput, level)) {
                    // Inputs changed mid-craft → cancel (mana already spent)
                    blockEntity.progress = 0;
                    blockEntity.cachedRecipe = null;
                    blockEntity.setChanged();
                } else {
                    blockEntity.progress++;
                    if (blockEntity.progress >= blockEntity.maxProgress) {
                        // Finish craft
                        ItemStack result = blockEntity.cachedRecipe.value()
                                .assemble(recipeInput);

                        blockEntity.removeItem(1, 1); // infusing ingredient
                        blockEntity.removeItem(2, 1); // item to infuse

                        ItemStack output = blockEntity.getItem(3);
                        if (output.isEmpty()) {
                            blockEntity.setItem(3, result);
                        } else {
                            output.grow(result.getCount());
                        }

                        blockEntity.progress = 0;
                        blockEntity.cachedRecipe = null;
                        blockEntity.setChanged();
                    }
                }
            }

            if (blockEntity.progress == 0) {
                RecipeManager recipeManager = serverLevel.recipeAccess();
                Optional<RecipeHolder<ManaInfuserRecipe>> recipeOpt =
                        recipeManager.getRecipeFor(
                                ManaInfuserRecipe.TYPE,
                                recipeInput,
                                serverLevel
                        );

                if (recipeOpt.isPresent()) {
                    RecipeHolder<ManaInfuserRecipe> holder = recipeOpt.get();
                    ManaInfuserRecipe recipe = holder.value();

                    // Check output slot can accept result BEFORE consuming mana
                    ItemStack result = recipe.assemble(recipeInput);
                    if (canInsertItemIntoOutput(blockEntity.getItem(3), result) &&
                            blockEntity.manaBuffer >= recipe.getManaCost()) {

                        blockEntity.cachedRecipe = holder;
                        blockEntity.maxProgress = recipe.getProcessingTime();
                        blockEntity.manaBuffer -= recipe.getManaCost();
                        blockEntity.progress = 1;
                        blockEntity.setChanged();
                    }
                }
            }
        }

        // Update lit state
        boolean shouldBeLit = blockEntity.progress > 0;
        if (wasLit != shouldBeLit) {
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, shouldBeLit), 3);
        }

        // Sync to GUI
        blockEntity.data.set(0, blockEntity.progress * 100 / blockEntity.maxProgress);
        blockEntity.data.set(1, blockEntity.manaBuffer);
    }

    private static boolean canInsertItemIntoOutput(ItemStack outputSlot, ItemStack result) {
        return outputSlot.isEmpty() ||
                (ItemStack.isSameItemSameComponents(outputSlot, result) &&
                        outputSlot.getCount() + result.getCount() <= outputSlot.getMaxStackSize());
    }
}