package com.sepinula.sepimod.client.model;

import com.sepinula.sepimod.spellbook.SpellbookItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SpellbookModel extends GeoModel<SpellbookItem> {
    @Override
    public ResourceLocation getModelResource(SpellbookItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("sepimod", "geo/spellbook.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SpellbookItem animatable) {
        // The current custom UV layout and texture were authored for the iron spellbook.
        return ResourceLocation.fromNamespaceAndPath("sepimod", "textures/item/iron_spellbook.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SpellbookItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("sepimod", "animations/spellbook.animation.json");
    }
}
