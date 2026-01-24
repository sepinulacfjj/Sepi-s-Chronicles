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

    private final int xSize = 512;
    private final int ySize = 512;
    private final int btnW = 162;
    private final int btnH = 40;
    private final float numberScale = 1.2f;
    private final float pointsScale = 2.0f;

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
        graphics.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, xSize, ySize, 512, 512);

        PlayerStats stats = this.minecraft.player.getData(ModDataAttachments.PLAYER_STATS);

        // --- STAT COLUMNS ---
        renderStatValue(graphics, mouseX, mouseY, stats.getStrength(), "strength", leftPos + 228, topPos + 199, leftPos + 94, topPos + 184);
        renderStatValue(graphics, mouseX, mouseY, stats.getAgility(), "agility", leftPos + 228, topPos + 245, leftPos + 94, topPos + 229);
        renderStatValue(graphics, mouseX, mouseY, stats.getConstitution(), "constitution", leftPos + 232, topPos + 289, leftPos + 94, topPos + 274);
        renderStatValue(graphics, mouseX, mouseY, stats.getDexterity(), "dexterity", leftPos + 228, topPos + 335, leftPos + 94, topPos + 319);
        renderStatValue(graphics, mouseX, mouseY, stats.getWillpower(), "willpower", leftPos + 392, topPos + 199, leftPos + 260, topPos + 184);
        renderStatValue(graphics, mouseX, mouseY, stats.getCharisma(), "charisma", leftPos + 397, topPos + 244, leftPos + 260, topPos + 229);
        renderStatValue(graphics, mouseX, mouseY, stats.getMana(), "mana", leftPos + 397, topPos + 289, leftPos + 260, topPos + 274);
        renderStatValue(graphics, mouseX, mouseY, stats.getMind(), "mind", leftPos + 392, topPos + 334, leftPos + 260, topPos + 319);

        // --- PURPLE TRAINING POINTS (Supports 999) ---
        String pointsVal = String.valueOf(stats.getAvailablePoints());
        graphics.pose().pushPose();
        // Adjusted X position (422) to ensure 3-digit numbers like 999 stay centered
        graphics.pose().translate(leftPos + 430, topPos + 162, 0);
        graphics.pose().scale(pointsScale, pointsScale, 1.0f);

        // Color changed to God Tier Purple (0xB048FF)
        graphics.drawString(this.font, pointsVal, 0, 0, 0xB048FF, true);
        graphics.pose().popPose();

        // Exit buttons highlights
        if (mouseX >= leftPos + 201 && mouseX <= leftPos + 314 && mouseY >= topPos + 362 && mouseY <= topPos + 385)
            graphics.fill(leftPos + 201, topPos + 362, leftPos + 314, topPos + 385, 0x40FFFFFF);
        if (mouseX >= leftPos + 471 && mouseX <= leftPos + 497 && mouseY >= topPos + 126 && mouseY <= topPos + 145)
            graphics.fill(leftPos + 471, topPos + 126, leftPos + 497, topPos + 145, 0x40FFFFFF);
    }

    private void renderStatValue(GuiGraphics graphics, int mouseX, int mouseY, int value, String statKey, int textX, int textY, int btnX, int btnY) {
        if (clickedStat.equals(statKey)) graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0x6000FF00);
        else if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH)
            graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0x30FFFFFF);

        // Reverted: Numbers are White, "MAX" is Purple
        boolean isMax = value >= 100;
        String valStr = isMax ? "MAX" : String.valueOf(value);
        int color = isMax ? 0xB048FF : 0xFFFFFF;

        graphics.pose().pushPose();
        graphics.pose().translate(textX, textY, 0);
        graphics.pose().scale(numberScale, numberScale, 1.0f);
        graphics.drawString(this.font, valStr, 0, 0, color, true);
        graphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int leftPos = (this.width - xSize) / 2;
        int topPos = (this.height - ySize) / 2;
        if (check(mouseX, mouseY, leftPos + 94, topPos + 184)) handleStatClick("strength");
        else if (check(mouseX, mouseY, leftPos + 94, topPos + 229)) handleStatClick("agility");
        else if (check(mouseX, mouseY, leftPos + 94, topPos + 274)) handleStatClick("constitution");
        else if (check(mouseX, mouseY, leftPos + 94, topPos + 319)) handleStatClick("dexterity");
        else if (check(mouseX, mouseY, leftPos + 260, topPos + 184)) handleStatClick("willpower");
        else if (check(mouseX, mouseY, leftPos + 260, topPos + 229)) handleStatClick("charisma");
        else if (check(mouseX, mouseY, leftPos + 260, topPos + 274)) handleStatClick("mana");
        else if (check(mouseX, mouseY, leftPos + 260, topPos + 319)) handleStatClick("mind");
        else if (mouseX >= leftPos + 201 && mouseX <= leftPos + 314 && mouseY >= topPos + 362 && mouseY <= topPos + 385) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            this.onClose();
        } else if (mouseX >= leftPos + 471 && mouseX <= leftPos + 497 && mouseY >= topPos + 126 && mouseY <= topPos + 145) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            this.onClose();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleStatClick(String stat) {
        PlayerStats stats = this.minecraft.player.getData(ModDataAttachments.PLAYER_STATS);
        int val = switch(stat) {
            case "strength" -> stats.getStrength();
            case "agility" -> stats.getAgility();
            case "constitution" -> stats.getConstitution();
            case "willpower" -> stats.getWillpower();
            case "mind" -> stats.getMind();
            case "mana" -> stats.getMana();
            case "dexterity" -> stats.getDexterity();
            case "charisma" -> stats.getCharisma();
            default -> 100;
        };
        if (stats.getAvailablePoints() > 0 && val < 100) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            Messages.sendToServer(new PacketUpdateStat(stat));
            this.clickedStat = stat;
            this.clickTimer = 5;
        } else if (val >= 100) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
        }
    }

    private boolean check(double mx, double my, int x, int y) { return mx >= x && mx <= x + btnW && my >= y && my <= y + btnH; }
    @Override public boolean isPauseScreen() { return false; }
}