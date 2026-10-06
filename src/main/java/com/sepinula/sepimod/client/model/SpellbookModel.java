package com.sepinula.sepimod.client.model;

import com.sepinula.sepimod.spellbook.SpellbookItem;
import com.sepinula.sepimod.spellbook.SpellbookTier;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SpellbookModel extends GeoModel<SpellbookItem> {
    @Override
    public ResourceLocation getModelResource(SpellbookItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("sepimod", "geo/spellbook.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SpellbookItem animatable) {
        // Keep the existing iron texture unchanged; use each tier's existing item texture for the others.
        String texture = switch (animatable.getTier()) {
            case ORIGINAL -> "spellbook";
            case COPPER -> "copper_spellbook";
            case IRON -> "iron_spellbook";
            case GOLD -> "gold_spellbook";
            case DIAMOND -> "diamond_spellbook";
            case NETHERITE -> "netherite_spellbook";
        };
        return ResourceLocation.fromNamespaceAndPath("sepimod", "textures/item/" + texture + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(SpellbookItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("sepimod", "animations/spellbook.animation.json");
    }
}
