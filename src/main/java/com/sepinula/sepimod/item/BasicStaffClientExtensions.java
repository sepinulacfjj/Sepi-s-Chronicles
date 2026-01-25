package com.sepinula.sepimod.item;

import com.sepinula.sepimod.client.renderer.BasicStaffRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class BasicStaffClientExtensions implements IClientItemExtensions {
    private BasicStaffRenderer renderer;

    @Override
    public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
        if (this.renderer == null) {
            this.renderer = new BasicStaffRenderer();
        }
        return this.renderer;
    }
}