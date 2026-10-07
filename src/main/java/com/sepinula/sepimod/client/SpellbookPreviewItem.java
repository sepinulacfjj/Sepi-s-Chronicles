package com.sepinula.sepimod.client;

import com.sepinula.sepimod.spellbook.SpellbookItem;
import com.sepinula.sepimod.spellbook.SpellbookTier;
import net.minecraft.world.item.Item;

public class SpellbookPreviewItem extends SpellbookItem {
    public SpellbookPreviewItem(SpellbookTier tier) {
        super(tier, new Item.Properties(), false);
    }
}
