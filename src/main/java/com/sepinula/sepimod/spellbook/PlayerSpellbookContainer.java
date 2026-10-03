package com.sepinula.sepimod.spellbook;

import com.sepinula.sepimod.network.PacketSyncSpellbookData;
import com.sepinula.sepimod.util.ModDataAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.SimpleContainer;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * One-slot container backed by the player's spellbook attachment.
 *
 * The actual item remains player data on the server. The container is the
 * bridge used by menu slots and vanilla inventory interactions.
 */
public class PlayerSpellbookContainer extends SimpleContainer {

    private final Player player;

    public PlayerSpellbookContainer(Player player) {
        super(1);
        this.player = player;

        ItemStack equipped = player.getData(ModDataAttachments.PLAYER_SPELLBOOK_DATA).getSpellbook();
        super.setItem(0, equipped.copy());

        addListener(container -> syncSpellbookData());
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        super.setItem(index, stack);
        syncSpellbookData();
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack result = super.removeItem(index, count);
        syncSpellbookData();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack result = super.removeItemNoUpdate(index);
        syncSpellbookData();
        return result;
    }

    private void syncSpellbookData() {
        if (player.level().isClientSide()) {
            return;
        }

        var data = player.getData(ModDataAttachments.PLAYER_SPELLBOOK_DATA);
        ItemStack current = getItem(0);
        data.setSpellbook(current);
        SpellbookHelper.enforceCapacity(player);

        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, PacketSyncSpellbookData.from(data));
        }
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
