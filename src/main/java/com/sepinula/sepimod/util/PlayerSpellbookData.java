package com.sepinula.sepimod.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import com.sepinula.sepimod.spellbook.SpellbookItem;

/**
 * Stores the physical spellbook equipped in the player's dedicated spellbook slot.
 *
 * The server keeps the actual ItemStack. The client only needs the equipped
 * state and capacity for rendering the spell UI.
 */
public class PlayerSpellbookData {

    public static final Codec<PlayerSpellbookData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ItemStack.CODEC.fieldOf("spellbook")
                            .forGetter(PlayerSpellbookData::getSpellbook)
            ).apply(instance, PlayerSpellbookData::new)
    );

    private ItemStack spellbook;
    private boolean clientEquipped;
    private int clientCapacity;

    public PlayerSpellbookData() {
        this(ItemStack.EMPTY);
    }

    public PlayerSpellbookData(ItemStack spellbook) {
        this.spellbook = spellbook.copy();
        this.clientEquipped = hasSpellbook();
        this.clientCapacity = getSpellSlotCapacity();
    }

    private PlayerSpellbookData(boolean equipped, int capacity) {
        this.spellbook = ItemStack.EMPTY;
        this.clientEquipped = equipped;
        this.clientCapacity = Math.max(0, capacity);
    }

    public static PlayerSpellbookData clientState(boolean equipped, int capacity) {
        return new PlayerSpellbookData(equipped, capacity);
    }

    public ItemStack getSpellbook() {
        return spellbook;
    }

    public void setSpellbook(ItemStack spellbook) {
        this.spellbook = spellbook.copy();
        this.clientEquipped = !this.spellbook.isEmpty()
                && this.spellbook.getItem() instanceof SpellbookItem;
        this.clientCapacity = getSpellSlotCapacity();
    }

    public boolean hasSpellbook() {
        if (!spellbook.isEmpty()) {
            return spellbook.getItem() instanceof SpellbookItem;
        }
        return clientEquipped;
    }

    public int getSpellSlotCapacity() {
        if (!spellbook.isEmpty() && spellbook.getItem() instanceof SpellbookItem spellbookItem) {
            return spellbookItem.getSpellSlots();
        }
        return clientCapacity;
    }
}
