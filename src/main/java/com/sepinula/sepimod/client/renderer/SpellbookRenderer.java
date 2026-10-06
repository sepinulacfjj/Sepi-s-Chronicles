package com.sepinula.sepimod.client.renderer;

import com.sepinula.sepimod.client.model.SpellbookModel;
import com.sepinula.sepimod.spellbook.SpellbookItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class SpellbookRenderer extends GeoItemRenderer<SpellbookItem> {
    public SpellbookRenderer() {
        super(new SpellbookModel());
    }
}
