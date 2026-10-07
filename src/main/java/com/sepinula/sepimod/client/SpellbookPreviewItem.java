package com.sepinula.sepimod.client;

import com.sepinula.sepimod.spellbook.SpellbookItem;
import com.sepinula.sepimod.spellbook.SpellbookTier;
import net.minecraft.world.item.Item;
import com.sepinula.sepimod.spellbook.SpellbookClientExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import java.util.function.Consumer;

public class SpellbookPreviewItem extends SpellbookItem {
    public SpellbookPreviewItem(SpellbookTier tier) {
        super(tier, new Item.Properties(), false);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new SpellbookClientExtensions());
    }
}
