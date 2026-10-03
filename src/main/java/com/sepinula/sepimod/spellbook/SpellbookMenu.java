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

    }

    @Override
    public boolean stillValid(Player player) {
        return player.isAlive();
    }
}
