package com.sepinula.sepimod.spellbook;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * The dedicated spellbook slot.
 *
 * It rejects every item that is not a SpellbookItem.
 */
public class SpellbookSlot extends Slot {

    public SpellbookSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.isEmpty() || stack.getItem() instanceof SpellbookItem;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }
}
