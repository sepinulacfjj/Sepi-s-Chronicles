package com.sepinula.sepimod.client.gui;

import com.sepinula.sepimod.SepiMod;
import com.sepinula.sepimod.network.Messages;
import com.sepinula.sepimod.network.PacketSelectClass;
import com.sepinula.sepimod.util.RpgArchetype;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

import java.util.List;

public class ClassSelectionScreen extends Screen {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "textures/gui/archetype_selection.png");

    // The redesigned texture is authored at its native resolution.
    private static final int GUI_WIDTH = 352;
    private static final int GUI_HEIGHT = 204;

    // Main player preview frame in archetype_selection.png.
    private static final int PREVIEW_X = 137;
    private static final int PREVIEW_Y = 30;
    private static final int PREVIEW_WIDTH = 84;
    private static final int PREVIEW_HEIGHT = 116;

    // Arrow controls beside the preview.
    private static final int PREVIOUS_X = 113;
    private static final int NEXT_X = 222;
    private static final int ARROW_Y = 78;
    private static final int ARROW_WIDTH = 17;
    private static final int ARROW_HEIGHT = 24;

    // Confirm button remains the same size as in the texture.
    private static final int CONFIRM_X = 137;
    private static final int CONFIRM_Y = 175;
    private static final int CONFIRM_WIDTH = 84;
    private static final int CONFIRM_HEIGHT = 20;

    private static final RpgArchetype[] CHOICES = {
            RpgArchetype.ROGUE,
            RpgArchetype.WARRIOR,
            RpgArchetype.MAGE
    };

    private int selectedIndex = 0;

    public ClassSelectionScreen() {
        super(Component.literal("Archetype Selection"));
    }

    private RpgArchetype selectedClass() {
        return CHOICES[selectedIndex];
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);

        int left = (width - GUI_WIDTH) / 2;
        int top = (height - GUI_HEIGHT) / 2;

        graphics.blit(GUI_TEXTURE, left, top, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);

        RpgArchetype archetype = selectedClass();

        // Render the actual local player model, including their current skin.
        if (minecraft.player != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    graphics,
                    left + PREVIEW_X,
                    top + PREVIEW_Y,
                    left + PREVIEW_X + PREVIEW_WIDTH,
                    top + PREVIEW_Y + PREVIEW_HEIGHT,
                    42,
                    0.0F,
                    mouseX,
                    mouseY,
                    minecraft.player
            );
        }

        // The chosen class sits in the strip beneath the player preview.
        graphics.drawCenteredString(font, Component.literal(archetype.getName()),
                left + PREVIEW_X + PREVIEW_WIDTH / 2, top + 151, 0xFFFF00);

        renderArrow(graphics, mouseX, mouseY, left + PREVIOUS_X, top + ARROW_Y, "<");
        renderArrow(graphics, mouseX, mouseY, left + NEXT_X, top + ARROW_Y, ">");

        // Class information uses the open space on either side of the preview.
        renderClassInformation(graphics, archetype, left, top);

        if (isInside(mouseX, mouseY, left + CONFIRM_X, top + CONFIRM_Y,
                CONFIRM_WIDTH, CONFIRM_HEIGHT)) {
            graphics.fill(left + CONFIRM_X + 1, top + CONFIRM_Y + 1,
                    left + CONFIRM_X + CONFIRM_WIDTH - 1,
                    top + CONFIRM_Y + CONFIRM_HEIGHT - 1, 0x45FFFFFF);
        }
    }

    private void renderArrow(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, String label) {
        boolean hovered = isInside(mouseX, mouseY, x, y, ARROW_WIDTH, ARROW_HEIGHT);
        if (hovered) {
            graphics.fill(x, y, x + ARROW_WIDTH, y + ARROW_HEIGHT, 0x45FFFFFF);
        }
        graphics.drawCenteredString(font, Component.literal(label),
                x + ARROW_WIDTH / 2, y + 7, 0xFFFF00);
    }

    private void renderClassInformation(GuiGraphics graphics, RpgArchetype archetype, int left, int top) {
        int leftTextX = left + 8;
        int rightTextX = left + 240;
        int infoTop = top + 42;
        int infoWidth = 102;

        graphics.drawString(font, Component.literal("§e§l" + archetype.getName()),
                leftTextX, infoTop, 0xFFFF00, false);

        List<FormattedCharSequence> description = font.split(
                Component.literal(stripFormatting(archetype.getDescription())), infoWidth);
        int y = infoTop + 13;
        for (FormattedCharSequence line : description) {
            graphics.drawString(font, line, leftTextX, y, 0xFFFFFF, false);
            y += 10;
        }

        graphics.drawString(font, Component.literal("§eBonuses"), leftTextX, y + 5, 0xFFFF00, false);
        y += 17;
        for (String bonus : getBonuses(archetype)) {
            if (y > top + 137) break;
            graphics.drawString(font, Component.literal(bonus), leftTextX, y, 0xFFFFFF, false);
            y += 11;
        }

        graphics.drawString(font, Component.literal("§ePlaystyle"), rightTextX, infoTop, 0xFFFF00, false);
        int rightY = infoTop + 14;
        for (FormattedCharSequence line : font.split(Component.literal(getPlaystyle(archetype)), infoWidth)) {
            graphics.drawString(font, line, rightTextX, rightY, 0xFFFFFF, false);
            rightY += 10;
        }

        graphics.drawString(font, Component.literal("§eBest for"), rightTextX, rightY + 6, 0xFFFF00, false);
        rightY += 18;
        for (FormattedCharSequence line : font.split(Component.literal(getBestFor(archetype)), infoWidth)) {
            if (rightY > top + 137) break;
            graphics.drawString(font, line, rightTextX, rightY, 0xFFFFFF, false);
            rightY += 10;
        }
    }

    private List<String> getBonuses(RpgArchetype archetype) {
        return switch (archetype) {
            case ROGUE -> List.of("+1 Strength", "+3 Agility", "+1 Health", "+1 Defense", "+1 Mind");
            case WARRIOR -> List.of("+2 Strength", "+1 Agility", "+2 Health", "+2 Defense");
            case MAGE -> List.of("+1 Agility", "+1 Magic Resist", "+2 Magic Power", "+2 Mana", "+2 Mind");
            default -> List.of();
        };
    }

    private String getPlaystyle(RpgArchetype archetype) {
        return switch (archetype) {
            case ROGUE -> "Fast and agile. Avoid attacks and move around enemies.";
            case WARRIOR -> "A sturdy front-line fighter built for close combat.";
            case MAGE -> "A spell-focused class with stronger magic and more mana.";
            default -> "";
        };
    }

    private String getBestFor(RpgArchetype archetype) {
        return switch (archetype) {
            case ROGUE -> "Mobility, dodging and quick attacks.";
            case WARRIOR -> "Surviving hits and dealing physical damage.";
            case MAGE -> "Casting spells and building magical power.";
            default -> "";
        };
    }

    private String stripFormatting(String text) {
        return text.replaceAll("§.", "");
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int left = (width - GUI_WIDTH) / 2;
        int top = (height - GUI_HEIGHT) / 2;

        if (isInside(mouseX, mouseY, left + PREVIOUS_X, top + ARROW_Y, ARROW_WIDTH, ARROW_HEIGHT)) {
            selectedIndex = (selectedIndex + CHOICES.length - 1) % CHOICES.length;
            playClick();
            return true;
        }

        if (isInside(mouseX, mouseY, left + NEXT_X, top + ARROW_Y, ARROW_WIDTH, ARROW_HEIGHT)) {
            selectedIndex = (selectedIndex + 1) % CHOICES.length;
            playClick();
            return true;
        }

        if (isInside(mouseX, mouseY, left + CONFIRM_X, top + CONFIRM_Y,
                CONFIRM_WIDTH, CONFIRM_HEIGHT)) {
            Messages.sendToServer(new PacketSelectClass(selectedClass().name()));
            playClick();
            onClose();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void playClick() {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private boolean isInside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
