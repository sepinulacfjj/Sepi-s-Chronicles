package com.sepinula.sepimod.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sepinula.sepimod.SepiMod;
import com.sepinula.sepimod.network.Messages;
import com.sepinula.sepimod.network.PacketUpdateStat;
import com.sepinula.sepimod.util.ModDataAttachments;
import com.sepinula.sepimod.util.PlayerStats;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

public class StatUpgradeScreen extends Screen {
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "textures/gui/stat_menu.png");
    private final int xSize = 256;
    private final int ySize = 256;

    // --- ADJUSTABLE BOX SETTINGS ---
    private final int btnW = 92;          // Changing this ONLY affects the green box width
    private final int btnH = 26;          // Changing this ONLY affects the green box height
    private final float numberScale = 1.4f;

    private String clickedStat = "";
    private int clickTimer = 0;

    public StatUpgradeScreen() {
        super(Component.literal("Stat Upgrades"));
    }

    @Override
    public void tick() {
        if (clickTimer > 0) clickTimer--;
        else clickedStat = "";
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        int leftPos = (this.width - xSize) / 2;
        int topPos = (this.height - ySize) / 2;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        graphics.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, xSize, ySize, 256, 256);

        PlayerStats stats = this.minecraft.player.getData(ModDataAttachments.PLAYER_STATS);

        // --- LEFT COLUMN ---
        // textX/textY move the NUMBER. btnX/btnY move the HOVER BOX.
        renderStatValue(graphics, mouseX, mouseY, stats.getStrength(), "strength",
                leftPos + 100, topPos + 70,
                leftPos + 30, topPos + 63);
        renderStatValue(graphics, mouseX, mouseY, stats.getAgility(), "agility",
                leftPos + 100, topPos + 106,
                leftPos + 30, topPos + 98);
        renderStatValue(graphics, mouseX, mouseY, stats.getConstitution(), "constitution",
                leftPos + 100, topPos + 140,
                leftPos + 30, topPos + 132);
        renderStatValue(graphics, mouseX, mouseY, stats.getDexterity(), "dexterity",
                leftPos + 100, topPos + 174,
                leftPos + 30, topPos + 167);

        // --- RIGHT COLUMN ---
        renderStatValue(graphics, mouseX, mouseY, stats.getWillpower(), "willpower",
                leftPos + 210, topPos + 70,
                leftPos + 134, topPos + 63);
        renderStatValue(graphics, mouseX, mouseY, stats.getCharisma(), "charisma",
                leftPos + 210, topPos + 106,
                leftPos + 134, topPos + 98);
        renderStatValue(graphics, mouseX, mouseY, stats.getMana(), "mana",
                leftPos + 210, topPos + 140,
                leftPos + 134, topPos + 132);
        renderStatValue(graphics, mouseX, mouseY, stats.getMind(), "mind",
                leftPos + 210, topPos + 174,
                leftPos + 134, topPos + 167);

        // Footer & Done Button
        String pointsText = "Points: " + stats.getAvailablePoints();
        graphics.drawString(this.font, pointsText, leftPos + (xSize / 2) - (this.font.width(pointsText) / 2), topPos + 52, 0xFFFFFF);

        if (mouseX >= leftPos + 95 && mouseX <= leftPos + 162 && mouseY >= topPos + 208 && mouseY <= topPos + 231) {
            graphics.fill(leftPos + 95, topPos + 208, leftPos + 162, topPos + 231, 0x40FFFFFF);
        }
    }

    /**
     * @param textX X position of the number
     * @param textY Y position of the number
     * @param btnX X position of the hover box
     * @param btnY Y position of the hover box
     */
    private void renderStatValue(GuiGraphics graphics, int mouseX, int mouseY, int value, String statKey, int textX, int textY, int btnX, int btnY) {
        // 1. Draw Green Hover/Click Box independently of text position
        if (clickedStat.equals(statKey)) {
            graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0x8000FF00); // Click Flash
        } else if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
            graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0x3000FF00); // Hover Green
        }

        // 2. Draw Scaled Number independently of box position
        String valStr = String.valueOf(value);
        graphics.pose().pushPose();
        graphics.pose().translate(textX, textY, 0);
        graphics.pose().scale(numberScale, numberScale, 1.0f);
        graphics.drawString(this.font, valStr, 0, 0, 0x404040, false);
        graphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int leftPos = (this.width - xSize) / 2;
        int topPos = (this.height - ySize) / 2;

        // Ensure these "y" values match the "btnY" values used in the render section above
        if (check(mouseX, mouseY, leftPos + 52, topPos + 65)) handleStatClick("strength");
        else if (check(mouseX, mouseY, leftPos + 52, topPos + 96)) handleStatClick("agility");
        else if (check(mouseX, mouseY, leftPos + 52, topPos + 130)) handleStatClick("constitution");
        else if (check(mouseX, mouseY, leftPos + 52, topPos + 164)) handleStatClick("dexterity");

        else if (check(mouseX, mouseY, leftPos + 142, topPos + 60)) handleStatClick("willpower");
        else if (check(mouseX, mouseY, leftPos + 142, topPos + 96)) handleStatClick("charisma");
        else if (check(mouseX, mouseY, leftPos + 142, topPos + 130)) handleStatClick("mana");
        else if (check(mouseX, mouseY, leftPos + 142, topPos + 164)) handleStatClick("mind");

        else if (mouseX >= leftPos + 95 && mouseX <= leftPos + 162 && mouseY >= topPos + 208 && mouseY <= topPos + 231) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            this.onClose();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleStatClick(String stat) {
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        Messages.sendToServer(new PacketUpdateStat(stat));
        this.clickedStat = stat;
        this.clickTimer = 5;
    }

    private boolean check(double mx, double my, int x, int y) {
        return mx >= x && mx <= x + btnW && my >= y && my <= y + btnH;
    }

    @Override public boolean isPauseScreen() { return false; }
}