package net.pnovaczek.spellgems.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.pnovaczek.spellgems.astralbow.AstralBowCaster;
import net.pnovaczek.spellgems.screen.AstralBowMenu;

public class AstralBowItem extends BowItem {

    public AstralBowItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
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
                    return new AstralBowMenu(containerId, inventory, openHand);
                }
            });

            return InteractionResult.SUCCESS;
        }

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof Player player)) {
            return false;
        }

        int useTime = this.getUseDuration(stack, user) - remainingUseTicks;
        float power = getPowerForTime(useTime, stack, user);

        if (power < 0.1F) {
            return false;
        }

        if (level instanceof ServerLevel serverLevel) {
            InteractionHand hand = player.getMainHandItem() == stack
                    ? InteractionHand.MAIN_HAND
                    : InteractionHand.OFF_HAND;
            AstralBowCaster.fireVolley(serverLevel, player, stack, hand, power);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F);

        player.awardStat(Stats.ITEM_USED.get(this));
        return true;
    }

    public static int getDrawDurationTicks(ItemStack bow, LivingEntity user) {
        float seconds = EnchantmentHelper.modifyCrossbowChargingTime(bow, user, 1.0F);
        return Mth.floor(seconds * 20.0F);
    }

    private static float getPowerForTime(int timeHeld, ItemStack bow, LivingEntity user) {
        float drawTicks = getDrawDurationTicks(bow, user);
        if (drawTicks <= 0.0F) {
            return 1.0F;
        }

        float pow = timeHeld / drawTicks;
        pow = (pow * pow + pow * 2.0F) / 3.0F;
        return Math.min(pow, 1.0F);
    }
}