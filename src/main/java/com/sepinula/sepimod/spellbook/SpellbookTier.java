package com.sepinula.sepimod.spellbook;

/**
 * Defines the capacity of a physical spellbook.
 *
 * The capacity belongs to the book item, while the actual spells belong to
 * PlayerSpellData.
 */
public enum SpellbookTier {
    WOOD(3),
    COPPER(4),
    IRON(5),
    GOLD(6),
    DIAMOND(7),
    NETHERITE(9);

    private final int spellSlots;

    SpellbookTier(int spellSlots) {
        this.spellSlots = spellSlots;
    }

    public int getSpellSlots() {
        return spellSlots;
    }
}
