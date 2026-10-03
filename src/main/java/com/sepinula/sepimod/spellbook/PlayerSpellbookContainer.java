package com.sepinula.sepimod.spellbook;

import com.sepinula.sepimod.util.ModDataAttachments;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.SimpleContainer;

/**
 * One-slot container that is backed by the player's spellbook attachment.
 *
 * The container is a bridge between player data and Minecraft's normal Slot
 * system. The actual item is still stored in PlayerSpellbookData.
 */
public class PlayerSpellbookContainer extends SimpleContainer {

    private final Player player;

    public PlayerSpellbookContainer(Player player) {
        super(1);
        this.player = player;

        ItemStack equipped = player.getData(ModDataAttachments.PLAYER_SPELLBOOK_DATA).getSpellbook();
        super.setItem(0, equipped.copy());

        addListener(container -> {
            if (player.level().isClientSide()) {
                return;
            }

            player.getData(ModDataAttachments.PLAYER_SPELLBOOK_DATA).setSpellbook(
                    container.getItem(0)
            );
        });
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
