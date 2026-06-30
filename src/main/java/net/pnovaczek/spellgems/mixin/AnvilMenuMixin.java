package net.pnovaczek.spellgems.mixin;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.pnovaczek.spellgems.anvil.SpellTomeAnvilHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {

    @Shadow
    @Final
    private DataSlot cost;

    @Shadow
    private int repairItemCountCost;

    @Shadow
    private boolean onlyRenaming;

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void spellgems$tryTomeCombine(CallbackInfo ci) {
        AnvilMenu menu = (AnvilMenu) (Object) this;
        ItemStack gemStack = menu.getSlot(AnvilMenu.INPUT_SLOT).getItem();
        ItemStack tomeStack = menu.getSlot(AnvilMenu.ADDITIONAL_SLOT).getItem();

        SpellTomeAnvilHandler.tryCombine(gemStack, tomeStack).ifPresent(combineResult -> {
            this.onlyRenaming = false;
            this.repairItemCountCost = 0;
            this.cost.set(combineResult.xpCost());
            menu.getSlot(AnvilMenu.RESULT_SLOT).set(combineResult.result());
            ((AbstractContainerMenu) (Object) this).broadcastChanges();
            ci.cancel();
        });
    }
}