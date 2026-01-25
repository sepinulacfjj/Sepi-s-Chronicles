package com.sepinula.sepimod.client.model;

import com.sepinula.sepimod.item.BasicStaffItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BasicStaffModel extends GeoModel<BasicStaffItem> {
    @Override
    public ResourceLocation getModelResource(BasicStaffItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("sepimod", "geo/basic_staff.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BasicStaffItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("sepimod", "textures/item/basic_staff.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BasicStaffItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("sepimod", "animations/basic_staff.animation.json");
    }
}
