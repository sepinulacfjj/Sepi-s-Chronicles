package com.sepinula.sepimod.client.renderer;

import com.sepinula.sepimod.client.model.BasicStaffModel;
import com.sepinula.sepimod.item.BasicStaffItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class BasicStaffRenderer extends GeoItemRenderer<BasicStaffItem> {
    public BasicStaffRenderer() {
        super(new BasicStaffModel());
    }
}
