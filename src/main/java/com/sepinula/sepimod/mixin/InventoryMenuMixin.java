package com.sepinula.sepimod.mixin;

import com.sepinula.sepimod.spellbook.PlayerSpellbookContainer;
import com.sepinula.sepimod.spellbook.SpellbookItem;
import com.sepinula.sepimod.spellbook.SpellbookSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin {


    private static final int SEPI_SPELLBOOK_SLOT = 46;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void sepimod$addSpellbookSlot(Inventory playerInventory, boolean active, Player owner, CallbackInfo ci) {
        ((AbstractContainerMenuAccessor) this).sepimod$addSlot(
                new SpellbookSlot(new PlayerSpellbookContainer(owner), 0, 151, 60)
        );
    }

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void sepimod$quickMoveSpellbook(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        InventoryMenu menu = (InventoryMenu) (Object) this;

        if (index == SEPI_SPELLBOOK_SLOT) {
            Slot spellbookSlot = menu.getSlot(SEPI_SPELLBOOK_SLOT);
            if (!spellbookSlot.hasItem()) {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }

            ItemStack source = spellbookSlot.getItem();
            ItemStack copy = source.copy();

            if (!((AbstractContainerMenuAccessor) this).sepimod$moveItemStackTo(source, 9, 45, true)) {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }

            spellbookSlot.setChanged();
            if (source.isEmpty()) {
                spellbookSlot.set(ItemStack.EMPTY);
            }

            cir.setReturnValue(copy);
            return;
        }

        if (index >= 9 && index < SEPI_SPELLBOOK_SLOT) {
            Slot sourceSlot = menu.getSlot(index);
            if (!sourceSlot.hasItem() || !(sourceSlot.getItem().getItem() instanceof SpellbookItem)) {
                return;
            }

            ItemStack source = sourceSlot.getItem();
            ItemStack copy = source.copy();

            if (!menu.getSlot(SEPI_SPELLBOOK_SLOT).mayPlace(source)
                    || !((AbstractContainerMenuAccessor) this).sepimod$moveItemStackTo(source, SEPI_SPELLBOOK_SLOT, SEPI_SPELLBOOK_SLOT + 1, false)) {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }

            sourceSlot.setChanged();
            if (source.isEmpty()) {
                sourceSlot.set(ItemStack.EMPTY);
            }

            cir.setReturnValue(copy);
        }
    }
}
