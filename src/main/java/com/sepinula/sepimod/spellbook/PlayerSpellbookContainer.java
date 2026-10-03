package com.sepinula.sepimod.spellbook;

import com.sepinula.sepimod.network.PacketSyncSpellbookData;
import com.sepinula.sepimod.util.ModDataAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.SimpleContainer;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * One-slot view backed directly by the player's spellbook attachment.
 *
 * Unlike a normal SimpleContainer, this container does not keep a second
 * independent copy of the item. That is important because the player's
 * attachment is the persistent source of truth for the dedicated spellbook
 * slot, including when a new InventoryMenu is created after reconnecting.
 */
public class PlayerSpellbookContainer extends SimpleContainer {

    private final Player player;

    public PlayerSpellbookContainer(Player player) {
        super(1);
        this.player = player;
    }

    @Override
    public ItemStack getItem(int index) {
        if (index != 0) {
            return ItemStack.EMPTY;
        }

        return player.getData(ModDataAttachments.PLAYER_SPELLBOOK_DATA)
                .getSpellbook()
                .copy();
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (index != 0) {
            return;
        }

        var data = player.getData(ModDataAttachments.PLAYER_SPELLBOOK_DATA);
        data.setSpellbook(stack);
        SpellbookHelper.enforceCapacity(player);

        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(
                    serverPlayer,
                    PacketSyncSpellbookData.from(data)
            );
        }
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        if (index != 0) {
            return ItemStack.EMPTY;
        }

        ItemStack current = getItem(0);
        if (current.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack removed = current.split(count);
        setItem(0, current);
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        if (index != 0) {
            return ItemStack.EMPTY;
        }

        ItemStack current = getItem(0);
        if (current.isEmpty()) {
            return ItemStack.EMPTY;
        }

        setItem(0, ItemStack.EMPTY);
        return current;
    }

    @Override
    public boolean isEmpty() {
        return getItem(0).isEmpty();
    }

    @Override
    public void clearContent() {
        setItem(0, ItemStack.EMPTY);
    }

    @Override
    public void setChanged() {
        // The attachment is updated immediately by setItem/removeItem.
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return index == 0 && (stack.isEmpty() || stack.getItem() instanceof SpellbookItem);
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
