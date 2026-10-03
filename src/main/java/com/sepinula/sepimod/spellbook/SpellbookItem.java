package com.sepinula.sepimod.spellbook;

import net.minecraft.world.item.Item;

/**
 * The physical spellbook.
 *
 * It only identifies its tier/capacity. Player spell knowledge and
 * configuration are intentionally stored in PlayerSpellData instead.
 */
public class SpellbookItem extends Item {

    private final SpellbookTier tier;

    public SpellbookItem(SpellbookTier tier, Properties properties) {
        super(properties.stacksTo(1));
        this.tier = tier;
    }

    public SpellbookTier getTier() {
        return tier;
    }

    public int getSpellSlots() {
        return tier.getSpellSlots();
    }
}
