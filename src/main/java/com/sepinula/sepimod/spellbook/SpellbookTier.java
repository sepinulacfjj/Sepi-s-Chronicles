package com.sepinula.sepimod.spellbook;

/**
 * Defines the capacity and cooldown efficiency of a physical spellbook.
 *
 * The capacity and cooldown bonus belong to the book item, while the actual
 * spells belong to PlayerSpellData.
 */
public enum SpellbookTier {
    ORIGINAL(3, 0.00F),
    COPPER(4, 0.02F),
    IRON(5, 0.05F),
    GOLD(6, 0.10F),
    DIAMOND(7, 0.15F),
    NETHERITE(9, 0.20F);

    private final int spellSlots;
    private final float cooldownReduction;

    SpellbookTier(int spellSlots, float cooldownReduction) {
        this.spellSlots = spellSlots;
        this.cooldownReduction = cooldownReduction;
    }

    public int getSpellSlots() {
        return spellSlots;
    }

    public float getCooldownReduction() {
        return cooldownReduction;
    }
}
