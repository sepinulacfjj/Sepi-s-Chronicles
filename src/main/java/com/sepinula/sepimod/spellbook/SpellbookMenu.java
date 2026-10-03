package com.sepinula.sepimod.spellbook;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import com.sepinula.sepimod.init.ModMenus;

/**
 * Server-authoritative menu for the spellbook screen.
 *
 * The dedicated spellbook slot is the only custom item slot. Spell selection
 * and spell combinations will be handled as player-data actions rather than
 * by putting spells into physical item slots.
 */
public class SpellbookMenu extends AbstractContainerMenu {

    private final Player player;

    public SpellbookMenu(int containerId, Inventory playerInventory) {
        super(ModMenus.SPELLBOOK.get(), containerId);
        this.player = playerInventory.player;

        PlayerSpellbookContainer bookContainer = new PlayerSpellbookContainer(player);
        this.addSlot(new SpellbookSlot(bookContainer, 0, 20, 30));

        addPlayerInventory(playerInventory);
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(inventory, column + row * 9 + 9,
                        8 + column * 18, 84 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(inventory, column,
                    8 + column * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack source = slot.getItem();
        ItemStack copy = source.copy();

        if (index == 0) {
            if (!moveItemStackTo(source, 1, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!(source.getItem() instanceof SpellbookItem)
                    || !this.slots.get(0).mayPlace(source)
                    || !moveItemStackTo(source, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (source.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isAlive();
    }
}
