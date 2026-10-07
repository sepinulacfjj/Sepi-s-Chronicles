package com.sepinula.sepimod.spellbook;

import com.sepinula.sepimod.init.ModMenus;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;

public class SpellbookMenu extends AbstractContainerMenu {
    private final Player player;

    public SpellbookMenu(int containerId, Inventory playerInventory) {
        super(ModMenus.SPELLBOOK.get(), containerId);
        this.player = playerInventory.player;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isAlive();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        if (player.level().isClientSide()) {
            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack stack = player.getItemInHand(hand);

                if (stack.getItem() instanceof SpellbookItem spellbook) {
                    spellbook.triggerAnim(player, GeoItem.getId(stack), "controller", "close");
                    break;
                }
            }
        }
    }
}
