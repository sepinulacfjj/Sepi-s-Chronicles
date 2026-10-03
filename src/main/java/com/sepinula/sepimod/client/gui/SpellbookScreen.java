package com.sepinula.sepimod.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sepinula.sepimod.spellbook.SpellbookMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * First functional version of the spellbook GUI.
 *
 * The final artwork can replace the procedural panels without changing the
 * menu/data architecture.
 */
public class SpellbookScreen extends AbstractContainerScreen<SpellbookMenu> {

    private static final ResourceLocation VANILLA_SLOT =
            ResourceLocation.withDefaultNamespace("textures/gui/container/inventory.png");

    public SpellbookScreen(SpellbookMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
        this.titleLabelX = 72;
        this.inventoryLabelX = 8;
        this.titleLabelY = 7;
        this.inventoryLabelY = 74;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF17131A);
        graphics.fill(x + 4, y + 4, x + imageWidth - 4, y + 70, 0xFF241D29);
        graphics.fill(x + 7, y + 7, x + imageWidth - 7, y + 69, 0xFF302533);

        // Dedicated spellbook panel.
        graphics.fill(x + 12, y + 22, x + 52, y + 66, 0xFF17131A);

        // Active spell slots.
        for (int i = 0; i < 5; i++) {
            int slotX = x + 62 + i * 21;
            graphics.fill(slotX, y + 28, slotX + 18, y + 46, 0xFF111016);
            graphics.fill(slotX + 1, y + 29, slotX + 17, y + 45, 0xFF3B2E3B);
        }

        // Player inventory background.
        RenderSystem.setShaderTexture(0, VANILLA_SLOT);
        graphics.blit(VANILLA_SLOT, x, y + 72, 0, 0, 176, 94, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, "Spellbook", this.titleLabelX, this.titleLabelY, 0xFFE8DCC7);
        graphics.drawString(this.font, "Active Spells", 62, 18, 0xFFD8C8A8);
        graphics.drawString(this.font, "Known Spells", 62, 51, 0xFFD8C8A8);
        graphics.drawString(this.font, "Book", 20, 14, 0xFFD8C8A8);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
