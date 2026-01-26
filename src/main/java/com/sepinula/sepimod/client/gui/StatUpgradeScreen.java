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

import java.util.ArrayList;
import java.util.List;

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

        // --- STAT COLUMNS (Using Total Stat for Display, but keeping keys unique) ---
        // Strength, Defense, and Mana now use getStrength(), getDefense(), and getMana() to show Archetype bonuses.
        renderStatValue(graphics, mouseX, mouseY, stats.getStrength(), "strength", leftPos + 228, topPos + 199, leftPos + 94, topPos + 184);
        renderStatValue(graphics, mouseX, mouseY, stats.getAgility(), "agility", leftPos + 228, topPos + 245, leftPos + 94, topPos + 229);
        renderStatValue(graphics, mouseX, mouseY, stats.getConstitution(), "constitution", leftPos + 232, topPos + 289, leftPos + 94, topPos + 274);
        renderStatValue(graphics, mouseX, mouseY, stats.getDefense(), "defense", leftPos + 228, topPos + 335, leftPos + 94, topPos + 319);
        renderStatValue(graphics, mouseX, mouseY, stats.getWillpower(), "willpower", leftPos + 392, topPos + 199, leftPos + 260, topPos + 184);
        renderStatValue(graphics, mouseX, mouseY, stats.getCharisma(), "charisma", leftPos + 397, topPos + 244, leftPos + 260, topPos + 229);
        renderStatValue(graphics, mouseX, mouseY, stats.getMana(), "mana", leftPos + 397, topPos + 289, leftPos + 260, topPos + 274);
        renderStatValue(graphics, mouseX, mouseY, stats.getMind(), "mind", leftPos + 392, topPos + 334, leftPos + 260, topPos + 319);

        // --- PURPLE TRAINING POINTS ---
        String pointsVal = String.valueOf(stats.getAvailablePoints());
        int pX = leftPos + 430;
        int pY = topPos + 162;

        graphics.pose().pushPose();
        graphics.pose().translate(pX, pY, 0);
        graphics.pose().scale(pointsScale, pointsScale, 1.0f);
        graphics.drawString(this.font, pointsVal, 0, 0, 0xB048FF, true);
        graphics.pose().popPose();

        // --- XP PROGRESS HOVER POP-UP (800 CAP) ---
        if (mouseX >= pX - 10 && mouseX <= pX + 50 && mouseY >= pY && mouseY <= pY + 25) {
            renderXpTooltip(graphics, stats, mouseX, mouseY);
        }

        // --- EXIT BUTTON HIGHLIGHTS ---
        renderButtonHighlights(graphics, mouseX, mouseY, leftPos, topPos);
    }

    private void renderStatValue(GuiGraphics graphics, int mouseX, int mouseY, int value, String statKey, int textX, int textY, int btnX, int btnY) {
        if (clickedStat.equals(statKey)) graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0x6000FF00);
        else if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH)
            graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0x30FFFFFF);

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
        else if (check(mouseX, mouseY, leftPos + 94, topPos + 319)) handleStatClick("defense");
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

        // We CHECK against the RAW value so archetype bonuses don't "eat" your 100-point limit.
        int rawVal = switch(stat) {
            case "strength" -> stats.getStrengthRaw();
            case "agility" -> stats.getAgility();
            case "constitution" -> stats.getConstitution();
            case "willpower" -> stats.getWillpower();
            case "mind" -> stats.getMind();
            case "mana" -> stats.getManaRaw();
            case "defense" -> stats.getDefenseRaw();
            case "charisma" -> stats.getCharisma();
            default -> 100;
        };

        if (stats.getAvailablePoints() > 0 && rawVal < 100) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            Messages.sendToServer(new PacketUpdateStat(stat));
            this.clickedStat = stat;
            this.clickTimer = 5;
        } else if (rawVal >= 100) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
        }
    }

    private void renderXpTooltip(GuiGraphics graphics, PlayerStats stats, int mx, int my) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.literal("§d§lNext Training Point"));
        if (stats.getTrainingPoints() >= 800) {
            tooltip.add(Component.literal("§7Progress: §e0 §8/ §eMAX XP"));
            tooltip.add(Component.literal("§a██████████ §7(100%)"));
        } else {
            int currentXp = (int) stats.getTotalXpGained();
            int goalXp = (int) stats.getXpNeededForNextPoint();
            float percent = Math.min(1.0f, (float)currentXp / goalXp);
            tooltip.add(Component.literal("§7Progress: §f" + currentXp + " §8/ §f" + goalXp + " XP"));
            String bar = "§a" + "█".repeat((int)(percent * 10)) + "§8" + "█".repeat(10 - (int)(percent * 10));
            tooltip.add(Component.literal(bar + " §7(" + (int)(percent * 100) + "%)"));
        }
        graphics.renderComponentTooltip(this.font, tooltip, mx, my);
    }

    private void renderButtonHighlights(GuiGraphics graphics, int mx, int my, int left, int top) {
        if (mx >= left + 201 && mx <= left + 314 && my >= top + 362 && my <= top + 385)
            graphics.fill(left + 201, top + 362, left + 314, top + 385, 0x40FFFFFF);
        if (mx >= left + 471 && mx <= left + 497 && my >= top + 126 && my <= top + 145)
            graphics.fill(left + 471, top + 126, left + 497, top + 145, 0x40FFFFFF);
    }

    private boolean check(double mx, double my, int x, int y) { return mx >= x && mx <= x + btnW && my >= y && my <= y + btnH; }
    @Override public boolean isPauseScreen() { return false; }
}