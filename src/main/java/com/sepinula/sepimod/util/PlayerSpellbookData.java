package com.sepinula.sepimod.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

/**
 * Stores the physical spellbook equipped in the player's dedicated spellbook slot.
 *
 * The book itself contains no learned-spell configuration. It only supplies
 * the tier and therefore the number of spell slots available to the player.
 */
public class PlayerSpellbookData {

    public static final Codec<PlayerSpellbookData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ItemStack.CODEC.fieldOf("spellbook")
                            .forGetter(PlayerSpellbookData::getSpellbook)
            ).apply(instance, PlayerSpellbookData::new)
    );

    private ItemStack spellbook;

    public PlayerSpellbookData() {
        this(ItemStack.EMPTY);
    }

    public PlayerSpellbookData(ItemStack spellbook) {
        this.spellbook = spellbook.copy();
    }

    public ItemStack getSpellbook() {
        return spellbook;
    }

    public void setSpellbook(ItemStack spellbook) {
        this.spellbook = spellbook.copy();
    }

    public boolean hasSpellbook() {
        return !spellbook.isEmpty() && spellbook.getItem() instanceof com.sepinula.sepimod.spellbook.SpellbookItem;
    }

    public int getSpellSlotCapacity() {
        if (!hasSpellbook()) {
            return 0;
        }

        return ((com.sepinula.sepimod.spellbook.SpellbookItem) spellbook.getItem()).getSpellSlots();
    }
}
