package com.sepinula.sepimod.client.gui;

import com.sepinula.sepimod.SepiMod;
import com.sepinula.sepimod.network.Messages;
import com.sepinula.sepimod.network.PacketUpdateStat;
import com.sepinula.sepimod.util.ModDataAttachments;
import com.sepinula.sepimod.util.PlayerStats;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;

import java.util.ArrayList;
import java.util.List;

public class StatUpgradeScreen extends Screen {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "textures/gui/stat_menu.png");

    private static final int GUI_WIDTH = 352;
    private static final int GUI_HEIGHT = 204;

    private static final int STAT_WIDTH = 155;
    private static final int STAT_HEIGHT = 38;
    private static final int LEFT_X = 20;
    private static final int RIGHT_X = 178;
    private static final int[] STAT_YS = {68, 107, 146, 185};

    private static final int POINTS_X = 20;
    private static final int POINTS_Y = 20;
    private static final int POINTS_WIDTH = 84;
    private static final int POINTS_HEIGHT = 21;

    private static final int CONFIRM_X = 249;
    private static final int CONFIRM_Y = 20;
    private static final int CONFIRM_WIDTH = 84;
    private static final int CONFIRM_HEIGHT = 21;

    private static final int TEXT_Y_OFFSET = 13;

    private String clickedStat = "";
    private int clickTimer = 0;

    public StatUpgradeScreen() {
        super(Component.literal("Status Upgrade"));
    }

    @Override
    public void tick() {
        if (clickTimer > 0) {
            clickTimer--;
        } else {
            clickedStat = "";
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);

        int left = (width - GUI_WIDTH) / 2;
        int top = (height - GUI_HEIGHT) / 2;

        graphics.blit(GUI_TEXTURE, left, top, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);

        PlayerStats stats = minecraft.player.getData(ModDataAttachments.PLAYER_STATS);

        renderStat(graphics, mouseX, mouseY, left, top, 0, "strength", stats.getStrength(), "Strength");
        renderStat(graphics, mouseX, mouseY, left, top, 1, "magic_resistance", stats.getMagicResistance(), "Magic Resist");
        renderStat(graphics, mouseX, mouseY, left, top, 2, "agility", stats.getAgility(), "Agility");
        renderStat(graphics, mouseX, mouseY, left, top, 3, "magic_power", stats.getMagicPower(), "Magic Power");

        renderStat(graphics, mouseX, mouseY, left, top, 4, "constitution", stats.getConstitution(), "Constitution");
        renderStat(graphics, mouseX, mouseY, left, top, 5, "mana", stats.getMana(), "Mana");
        renderStat(graphics, mouseX, mouseY, left, top, 6, "defense", stats.getDefense(), "Defense");
        renderStat(graphics, mouseX, mouseY, left, top, 7, "mind", stats.getMind(), "Mind");

        String points = String.valueOf(stats.getAvailablePoints());
        graphics.drawString(font, points, left + 145, top + 26, 0xFFFFFF, true);

        if (isInside(mouseX, mouseY, left + POINTS_X, top + POINTS_Y, POINTS_WIDTH, POINTS_HEIGHT)) {
            renderTooltip(graphics, List.of(
                    Component.literal("§d§lTraining Points"),
                    Component.literal("§7Available: §f" + stats.getAvailablePoints()),
                    Component.literal("§7Spend points on any stat.")
            ), mouseX, mouseY);
        }

        if (isInside(mouseX, mouseY, left + CONFIRM_X, top + CONFIRM_Y, CONFIRM_WIDTH, CONFIRM_HEIGHT)) {
            renderTooltip(graphics, List.of(
                    Component.literal("§e§lConfirm"),
                    Component.literal("§7Close the stat menu.")
            ), mouseX, mouseY);
        }

        if (stats.getTrainingPoints() < 800 && stats.getAvailablePoints() >= 0
                && isInside(mouseX, mouseY, left + 80, top + 20, 150, 24)) {
            renderXpTooltip(graphics, stats, mouseX, mouseY);
        }
    }

    private void renderStat(GuiGraphics graphics, int mouseX, int mouseY, int left, int top,
                            int index, String statKey, int value, String label) {
        int column = index % 2;
        int row = index / 2;
        int x = column == 0 ? LEFT_X : RIGHT_X;
        int y = STAT_YS[row];

        boolean hovered = isInside(mouseX, mouseY, left + x, top + y, STAT_WIDTH, STAT_HEIGHT);
        boolean clicked = clickedStat.equals(statKey);

        if (hovered) {
            graphics.fill(left + x + 2, top + y + 2,
                    left + x + STAT_WIDTH - 2, top + y + STAT_HEIGHT - 2, 0x35FFFFFF);
        }
        if (clicked) {
            graphics.fill(left + x + 2, top + y + 2,
                    left + x + STAT_WIDTH - 2, top + y + STAT_HEIGHT - 2, 0x5000FF00);
        }

        String valueText = value >= 100 ? "MAX" : String.valueOf(value);
        int valueWidth = font.width(valueText);

        // The texture already contains the stat label and trailing '-'.
        // Put the live number immediately after it.
        int valueX = left + x + 125;
        if (valueWidth > 24) valueX = left + x + STAT_WIDTH - valueWidth - 5;
        graphics.drawString(font, valueText, valueX, top + y + TEXT_Y_OFFSET, value >= 100 ? 0xB048FF : 0xFFFFFF, true);

        if (hovered) {
            renderTooltip(graphics, getStatTooltip(statsForTooltip(), statKey), mouseX, mouseY);
        }
    }

    private PlayerStats statsForTooltip() {
        return minecraft.player.getData(ModDataAttachments.PLAYER_STATS);
    }

    private List<Component> getStatTooltip(PlayerStats stats, String stat) {
        List<Component> tooltip = new ArrayList<>();
        switch (stat) {
            case "strength" -> {
                tooltip.add(Component.literal("§c§lStrength"));
                tooltip.add(Component.literal("§7Increases physical attack damage."));
                tooltip.add(Component.literal("§7Also improves mining speed at high values."));
            }
            case "magic_resistance" -> {
                tooltip.add(Component.literal("§b§lMagic Resist"));
                tooltip.add(Component.literal("§7Chance to resist harmful status effects."));
                tooltip.add(Component.literal("§7Reduces magic and curse-type damage."));
            }
            case "agility" -> {
                tooltip.add(Component.literal("§e§lAgility"));
                tooltip.add(Component.literal("§7Improves dodge chance and sprint speed."));
                tooltip.add(Component.literal("§7Also increases maximum Stamina."));
            }
            case "magic_power" -> {
                tooltip.add(Component.literal("§5§lMagic Power"));
                tooltip.add(Component.literal("§7Increases spell damage and spell effects."));
            }
            case "constitution" -> {
                tooltip.add(Component.literal("§c§lConstitution"));
                tooltip.add(Component.literal("§7Increases maximum health and regeneration."));
            }
            case "mana" -> {
                tooltip.add(Component.literal("§9§lMana"));
                tooltip.add(Component.literal("§7Increases maximum Mana and regeneration."));
            }
            case "defense" -> {
                tooltip.add(Component.literal("§6§lDefense"));
                tooltip.add(Component.literal("§7Reduces incoming physical damage."));
                tooltip.add(Component.literal("§7Also increases knockback resistance."));
            }
            case "mind" -> {
                tooltip.add(Component.literal("§d§lMind"));
                tooltip.add(Component.literal("§7Increases experience gained."));
            }
        }
        return tooltip;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int left = (width - GUI_WIDTH) / 2;
        int top = (height - GUI_HEIGHT) / 2;

        String[] stats = {
                "strength", "magic_resistance", "agility", "magic_power",
                "constitution", "mana", "defense", "mind"
        };

        for (int i = 0; i < stats.length; i++) {
            int column = i % 2;
            int row = i / 2;
            int x = column == 0 ? LEFT_X : RIGHT_X;
            int y = STAT_YS[row];

            if (isInside(mouseX, mouseY, left + x, top + y, STAT_WIDTH, STAT_HEIGHT)) {
                handleStatClick(stats[i]);
                return true;
            }
        }

        if (isInside(mouseX, mouseY, left + CONFIRM_X, top + CONFIRM_Y, CONFIRM_WIDTH, CONFIRM_HEIGHT)) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            onClose();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleStatClick(String stat) {
        PlayerStats stats = minecraft.player.getData(ModDataAttachments.PLAYER_STATS);

        int rawValue = switch (stat) {
            case "strength" -> stats.getStrengthRaw();
            case "magic_resistance" -> stats.getMagicResistanceRaw();
            case "agility" -> stats.getAgilityRaw();
            case "magic_power" -> stats.getMagicPowerRaw();
            case "constitution" -> stats.getConstitutionRaw();
            case "mana" -> stats.getManaRaw();
            case "defense" -> stats.getDefenseRaw();
            case "mind" -> stats.getMindRaw();
            default -> 100;
        };

        if (stats.getAvailablePoints() > 0 && rawValue < 100) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            Messages.sendToServer(new PacketUpdateStat(stat));
            clickedStat = stat;
            clickTimer = 5;
        } else if (rawValue >= 100) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
        }
    }

    private void renderXpTooltip(GuiGraphics graphics, PlayerStats stats, int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.literal("§d§lNext Training Point"));

        if (stats.getTrainingPoints() >= 800) {
            tooltip.add(Component.literal("§7Progress: §eMAX LEVEL"));
        } else {
            int currentXp = (int) stats.getTotalXpGained();
            int goalXp = (int) stats.getXpNeededForNextPoint();
            int percent = goalXp <= 0 ? 0 : (int) Math.min(100.0F, currentXp * 100.0F / goalXp);
            tooltip.add(Component.literal("§7Progress: §f" + currentXp + " §8/ §f" + goalXp + " XP"));
            tooltip.add(Component.literal("§a" + "█".repeat(percent / 10)
                    + "§8" + "█".repeat(10 - percent / 10) + " §7(" + percent + "%)"));
        }

        renderTooltip(graphics, tooltip, mouseX, mouseY);
    }

    private boolean isInside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
