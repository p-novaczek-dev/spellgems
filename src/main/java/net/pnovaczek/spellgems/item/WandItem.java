package net.pnovaczek.spellgems.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.pnovaczek.spellgems.screen.WandMenu;
import net.pnovaczek.spellgems.wand.WandDepletion;
import org.jspecify.annotations.NonNull;

public class WandItem extends Item {

    public WandItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull Component getName(@NonNull ItemStack stack) {
        if (WandDepletion.isDepleted(stack)) {
            return Component.translatable("item.spellgems.wand.depleted");
        }
        return super.getName(stack);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        InteractionHand openHand = hand;
        player.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return stack.getHoverName();
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, net.minecraft.world.entity.player.Inventory inventory, Player p) {
                return new WandMenu(containerId, inventory, openHand);
            }
        });

        return InteractionResult.SUCCESS;
    }
}