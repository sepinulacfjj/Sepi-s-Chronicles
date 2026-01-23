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

    private final int xSize = 256;
    private final int ySize = 256;

    // Tracks what the user has clicked on before pressing confirm
    private String selectedClass = "";

    public ClassSelectionScreen() {
        super(Component.literal("Select Your Class"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);

        int leftPos = (this.width - xSize) / 2;
        int topPos = (this.height - ySize) / 2;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);

        // Render main texture. Using 512, 512 for the source size to fix tiling.
        graphics.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, xSize, ySize, 256, 256);

        // --- BUTTON DIMENSIONS (Synced with your provided values) ---
        int buttonX = leftPos + 55;
        int buttonWidth = 145;
        int buttonHeight = 35;

        // Render Class Visuals (Hover = White, Selected = Green)
        renderClassVisuals(graphics, mouseX, mouseY, "Rogue", buttonX, topPos + 56, buttonWidth, buttonHeight);
        renderClassVisuals(graphics, mouseX, mouseY, "Warrior", buttonX, topPos + 95, buttonWidth, buttonHeight);
        renderClassVisuals(graphics, mouseX, mouseY, "Mage", buttonX, topPos + 133, buttonWidth, buttonHeight);

        // --- SYSTEM BUTTON HOVERS (Confirm & X) ---
        // Confirm Hover
        if (checkClick(mouseX, mouseY, leftPos + 94, topPos + 181, 55, 18)) {
            graphics.fill(leftPos + 94, topPos + 181, leftPos + 107 + 55, topPos + 191 + 18, 0x40FFFFFF);
        }
        // X Hover
        if (checkClick(mouseX, mouseY, leftPos + 177, topPos + 183, 18, 18)) {
            graphics.fill(leftPos + 177, topPos + 183, leftPos + 185 + 18, topPos + 193 + 18, 0x40FFFFFF);
        }
    }

    private void renderClassVisuals(GuiGraphics graphics, int mouseX, int mouseY, String className, int x, int y, int w, int h) {
        if (selectedClass.equals(className)) {
            // SELECTED: Green highlight (0x6000FF00)
            graphics.fill(x, y, x + w, y + h, 0x6000FF00);
        } else if (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h) {
            // HOVER: White highlight (0x40FFFFFF)
            graphics.fill(x, y, x + w, y + h, 0x40FFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int leftPos = (this.width - xSize) / 2;
        int topPos = (this.height - ySize) / 2;

        int buttonX = leftPos + 55;
        int buttonWidth = 145;
        int buttonHeight = 32;

        // 1. Rogue Selection
        if (checkClick(mouseX, mouseY, buttonX, topPos + 57, buttonWidth, buttonHeight)) {
            selectVisualOnly("Rogue");
            return true;
        }
        // 2. Warrior Selection
        if (checkClick(mouseX, mouseY, buttonX, topPos + 97, buttonWidth, buttonHeight)) {
            selectVisualOnly("Warrior");
            return true;
        }
        // 3. Mage Selection
        if (checkClick(mouseX, mouseY, buttonX, topPos + 135, buttonWidth, buttonHeight)) {
            selectVisualOnly("Mage");
            return true;
        }

        // 4. CONFIRM BUTTON CLICK
        if (checkClick(mouseX, mouseY, leftPos + 94, topPos + 181, 55, 18)) {
            if (!selectedClass.isEmpty()) {
                confirmAndSend();
            }
            return true;
        }

        // 5. X BUTTON CLICK
        if (checkClick(mouseX, mouseY, leftPos + 177, topPos + 183, 18, 18)) {
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
        // Only sends the packet when the player clicks the actual CONFIRM button
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