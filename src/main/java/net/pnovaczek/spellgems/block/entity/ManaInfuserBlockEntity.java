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

    // Hoppers/pipes: all faces see all slots; placement/extraction filtered by item role.
    // Order prefers fuel → infusing → infused → output so auto-insert picks the right role.
    private static final int[] AUTOMATION_SLOTS = {0, 1, 2, 3};
    public static final int SLOT_FUEL = 0;
    public static final int SLOT_INFUSING = 1;
    public static final int SLOT_INFUSED = 2;
    public static final int SLOT_OUTPUT = 3;

    @Override
    public int[] getSlotsForFace(Direction side) {
        return AUTOMATION_SLOTS;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (stack.isEmpty() || slot == SLOT_OUTPUT) {
            return false;
        }

        ItemStack existing = getItem(slot);
        if (!existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing, stack)) {
            return false;
        }

        return switch (slot) {
            case SLOT_FUEL -> stack.is(ModItems.MANA_ESSENCE);
            case SLOT_INFUSING -> isValidInfusingIngredient(stack);
            case SLOT_INFUSED -> isValidInfusedItem(stack);
            default -> false;
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot == SLOT_OUTPUT;
    }

    /**
     * True if {@code stack} is the infusing ingredient of any mana-infuser recipe.
     */
    private boolean isValidInfusingIngredient(ItemStack stack) {
        return matchesAnyRecipeIngredient(stack, true);
    }

    /**
     * True if {@code stack} is the item-to-infuse of any mana-infuser recipe.
     */
    private boolean isValidInfusedItem(ItemStack stack) {
        return matchesAnyRecipeIngredient(stack, false);
    }

    private boolean matchesAnyRecipeIngredient(ItemStack stack, boolean infusing) {
        if (!(level instanceof ServerLevel serverLevel)) {
            // Pipes/hoppers run server-side; fail closed without a level.
            return false;
        }
        if (!(serverLevel.recipeAccess() instanceof RecipeManager recipeManager)) {
            return false;
        }
        for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
            if (holder.value().getType() != ManaInfuserRecipe.TYPE) {
                continue;
            }
            ManaInfuserRecipe recipe = (ManaInfuserRecipe) holder.value();
            if (infusing ? recipe.getInfusingItem().test(stack) : recipe.getInfusedItem().test(stack)) {
                return true;
            }
        }
        return false;
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