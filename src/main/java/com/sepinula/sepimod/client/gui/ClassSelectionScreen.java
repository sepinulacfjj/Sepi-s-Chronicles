package com.sepinula.sepimod.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sepinula.sepimod.network.Messages;
import com.sepinula.sepimod.network.PacketSelectClass;
import com.sepinula.sepimod.SepiMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

public class ClassSelectionScreen extends Screen {
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "textures/gui/archetype_selection.png");

    private final int xSize = 512;
    private final int ySize = 512;

    private String selectedClass = "";

    public ClassSelectionScreen() {
        super(Component.literal("Select Your Archetype"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);

        int leftPos = (this.width - xSize) / 2;
        int topPos = (this.height - ySize) / 2;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);

        // Render the texture scaled to 512x512
        graphics.blit(GUI_TEXTURE, leftPos, topPos, xSize, ySize, 0, 0, 256, 256, 256, 256);

        // --- DYNAMIC BUTTON SCALING ---
        int boxW = (int)(xSize * 0.196);
        int boxH = (int)(ySize * 0.321);
        int boxY = topPos + (int)(ySize * 0.337);

        // Rogue, Warrior, Mage Highlights
        renderClassVisuals(graphics, mouseX, mouseY, "Rogue", leftPos + (int)(xSize * 0.205), boxY, boxW, boxH);
        renderClassVisuals(graphics, mouseX, mouseY, "Warrior", leftPos + (int)(xSize * 0.411), boxY, boxW, boxH);
        renderClassVisuals(graphics, mouseX, mouseY, "Mage", leftPos + (int)(xSize * 0.619), boxY, boxW, boxH);

        // Choose Button Highlight
        if (checkClick(mouseX, mouseY, leftPos + (int)(xSize * 0.290), topPos + (int)(ySize * 0.665), (int)(xSize * 0.44), (int)(ySize * 0.08))) {
            graphics.fill(leftPos + (int)(xSize * 0.290), topPos + (int)(ySize * 0.665), leftPos + (int)(xSize * 0.732), topPos + (int)(ySize * 0.745), 0x40FFFFFF);
        }

        // --- X BUTTON HOVER FIX ---
        // These coordinates are calculated to hit the red 'X' in the top right of the scroll
        int xBtnX = leftPos + (int)(xSize * 0.923);
        int xBtnY = topPos + (int)(ySize * 0.245);
        int xBtnWidth = (int)(xSize * 0.047);
        int xBtnHeight = (int)(ySize * 0.038);

        if (checkClick(mouseX, mouseY, xBtnX, xBtnY, xBtnWidth, xBtnHeight)) {
            // Draw the hover box exactly over the sensor area
            graphics.fill(xBtnX, xBtnY, xBtnX + xBtnWidth, xBtnY + xBtnHeight, 0x40FFFFFF);
        }
    }

    private void renderClassVisuals(GuiGraphics graphics, int mouseX, int mouseY, String className, int x, int y, int w, int h) {
        if (selectedClass.equals(className)) {
            graphics.fill(x, y, x + w, y + h, 0x6000FF00);
        } else if (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h) {
            graphics.fill(x, y, x + w, y + h, 0x40FFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int leftPos = (this.width - xSize) / 2;
        int topPos = (this.height - ySize) / 2;

        int boxW = (int)(xSize * 0.196);
        int boxH = (int)(ySize * 0.321);
        int boxY = topPos + (int)(ySize * 0.337);

        // Class Selection Clicks
        if (checkClick(mouseX, mouseY, leftPos + (int)(xSize * 0.205), boxY, boxW, boxH)) {
            selectVisualOnly("Rogue");
            return true;
        }
        if (checkClick(mouseX, mouseY, leftPos + (int)(xSize * 0.411), boxY, boxW, boxH)) {
            selectVisualOnly("Warrior");
            return true;
        }
        if (checkClick(mouseX, mouseY, leftPos + (int)(xSize * 0.619), boxY, boxW, boxH)) {
            selectVisualOnly("Mage");
            return true;
        }

        // Choose Button Click
        if (checkClick(mouseX, mouseY, leftPos + (int)(xSize * 0.290), topPos + (int)(ySize * 0.665), (int)(xSize * 0.44), (int)(ySize * 0.08))) {
            if (!selectedClass.isEmpty()) {
                confirmAndSend();
            }
            return true;
        }

        // --- X BUTTON CLICK FIX ---
        // Must use the EXACT same coordinates as the render() hover
        int xBtnX = leftPos + (int)(xSize * 0.924);
        int xBtnY = topPos + (int)(ySize * 0.245);
        int xBtnWidth = (int)(xSize * 0.047);
        int xBtnHeight = (int)(ySize * 0.038);

        if (checkClick(mouseX, mouseY, xBtnX, xBtnY, xBtnWidth, xBtnHeight)) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            this.onClose();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void selectVisualOnly(String className) {
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        this.selectedClass = className;
    }

    private void confirmAndSend() {
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        Messages.sendToServer(new PacketSelectClass(this.selectedClass));
        this.onClose();
    }

    private boolean checkClick(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}