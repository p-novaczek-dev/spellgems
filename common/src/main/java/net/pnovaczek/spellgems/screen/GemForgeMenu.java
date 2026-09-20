package net.pnovaczek.spellgems.screen;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.pnovaczek.spellgems.ModBlocks;
import net.pnovaczek.spellgems.ModMenuTypes;
import net.pnovaczek.spellgems.recipe.GemForgeRecipe;
import net.pnovaczek.spellgems.recipe.GemForgeRecipeInput;

import java.util.List;
import java.util.Optional;

public class GemForgeMenu extends ItemCombinerMenu {
    public static final int INPUT_SLOT_1 = 0;
    public static final int INPUT_SLOT_2 = 1;
    public static final int RESULT_SLOT = 2;

    private static final int INPUT_1_X = 8;
    private static final int INPUT_2_X = 44;
    private static final int RESULT_X = 98;
    private static final int SLOT_Y = 48;

    private final Level level;
    private final DataSlot xpCost = DataSlot.standalone();
    private final DataSlot hasRecipeError = DataSlot.standalone();

    public GemForgeMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, ContainerLevelAccess.NULL);
    }

    @SuppressWarnings("this-escape")
    public GemForgeMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
        super(ModMenuTypes.GEM_FORGE, containerId, inventory, access, createInputSlotDefinitions());
        this.level = inventory.player.level();
        this.addDataSlot(this.xpCost);
        this.addDataSlot(this.hasRecipeError);
    }

    private static ItemCombinerMenuSlotDefinition createInputSlotDefinitions() {
        return ItemCombinerMenuSlotDefinition.create()
                .withSlot(INPUT_SLOT_1, INPUT_1_X, SLOT_Y, stack -> true)
                .withSlot(INPUT_SLOT_2, INPUT_2_X, SLOT_Y, stack -> true)
                .withResultSlot(RESULT_SLOT, RESULT_X, SLOT_Y)
                .build();
    }

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.is(ModBlocks.GEM_FORGE);
    }

    @Override
    protected boolean mayPickup(Player player, boolean hasItem) {
        if (!hasItem) {
            return false;
        }
        return player.hasInfiniteMaterials() || player.totalExperience >= this.xpCost.get();
    }

    @Override
    protected void onTake(Player player, ItemStack carried) {
        carried.onCraftedBy(player, carried.getCount());
        this.resultSlots.awardUsedRecipes(player, List.of(
                this.inputSlots.getItem(INPUT_SLOT_1),
                this.inputSlots.getItem(INPUT_SLOT_2)
        ));

        int cost = this.xpCost.get();
        if (!player.hasInfiniteMaterials() && cost > 0) {
            player.giveExperiencePoints(-cost);
        }

        shrinkStackInSlot(INPUT_SLOT_1);
        shrinkStackInSlot(INPUT_SLOT_2);
        this.access.execute((level, pos) -> level.levelEvent(1030, pos, 0));
    }

    private void shrinkStackInSlot(int slot) {
        ItemStack stack = this.inputSlots.getItem(slot);
        if (!stack.isEmpty()) {
            stack.shrink(1);
            this.inputSlots.setItem(slot, stack);
        }
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (this.level instanceof ServerLevel) {
            boolean error = this.getSlot(INPUT_SLOT_1).hasItem()
                    && this.getSlot(INPUT_SLOT_2).hasItem()
                    && !this.getSlot(this.getResultSlot()).hasItem();
            this.hasRecipeError.set(error ? 1 : 0);
        }
    }

    @Override
    public void createResult() {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!(serverLevel.recipeAccess() instanceof RecipeManager recipeManager)) {
            clearResult();
            return;
        }

        GemForgeRecipeInput input = new GemForgeRecipeInput(
                this.inputSlots.getItem(INPUT_SLOT_1),
                this.inputSlots.getItem(INPUT_SLOT_2)
        );
        Optional<RecipeHolder<GemForgeRecipe>> found = recipeManager.getRecipeFor(
                GemForgeRecipe.TYPE, input, serverLevel);

        found.ifPresentOrElse(holder -> {
            ItemStack result = holder.value().assemble(input);
            if (result.isEmpty()) {
                clearResult();
                return;
            }
            this.resultSlots.setRecipeUsed(holder);
            this.resultSlots.setItem(0, result);
            this.xpCost.set(holder.value().getXpCost());
        }, this::clearResult);
    }

    private void clearResult() {
        this.resultSlots.setRecipeUsed(null);
        this.resultSlots.setItem(0, ItemStack.EMPTY);
        this.xpCost.set(0);
    }

    public int getXpCost() {
        return this.xpCost.get();
    }

    public boolean hasRecipeError() {
        return this.hasRecipeError.get() > 0;
    }
}
