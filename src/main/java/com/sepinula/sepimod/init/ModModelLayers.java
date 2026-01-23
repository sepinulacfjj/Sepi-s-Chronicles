package com.sepinula.sepimod.init;

import com.sepinula.sepimod.SepiMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class ModModelLayers {
    public static final ModelLayerLocation GOBLIN_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "goblin"), "main");
}