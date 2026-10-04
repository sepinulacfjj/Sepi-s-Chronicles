package com.sepinula.sepimod.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {

    private static final ResourceLocation INVENTORY_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/inventory.png");

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void sepimod$renderSpellbookSlot(
            GuiGraphics graphics,
            float partialTick,
            int mouseX,
            int mouseY,
            CallbackInfo ci
    ) {
        InventoryScreen screen = (InventoryScreen) (Object) this;
        AbstractContainerScreen<?> containerScreen = (AbstractContainerScreen<?>) screen;

        graphics.blit(INVENTORY_TEXTURE,
                containerScreen.getGuiLeft() + 151,
                containerScreen.getGuiTop() + 60,
                7, 83,
                18, 18,
                256, 256);
    }

}
