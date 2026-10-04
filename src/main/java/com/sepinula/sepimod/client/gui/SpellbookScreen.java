package com.sepinula.sepimod.client.gui;

import com.sepinula.sepimod.network.Messages;
import com.sepinula.sepimod.network.PacketSpellbookAction;
import com.sepinula.sepimod.spells.Spell;
import com.sepinula.sepimod.spells.SpellRegistry;
import com.sepinula.sepimod.util.ModDataAttachments;
import com.sepinula.sepimod.util.PlayerSpellData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class SpellbookScreen extends AbstractContainerScreen<com.sepinula.sepimod.spellbook.SpellbookMenu> {

    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath("sepimod", "textures/gui/spellbook_background.png");

    private static final ResourceLocation EMPTY_ICON =
            ResourceLocation.fromNamespaceAndPath("sepimod", "textures/icon/empty_box_icon.png");

    private static final ResourceLocation LOCK_ICON =
            ResourceLocation.fromNamespaceAndPath("sepimod", "textures/icon/lock_icon.png");

    private static final ResourceLocation FIREBALL_ICON =
            ResourceLocation.fromNamespaceAndPath("sepimod", "textures/icon/fireball_icon.png");

    private static final ResourceLocation GUST_ICON =
            ResourceLocation.fromNamespaceAndPath("sepimod", "textures/icon/gust_icon.png");

    private static final ResourceLocation ICE_SHARD_ICON =
            ResourceLocation.fromNamespaceAndPath("sepimod", "textures/icon/ice_shard_icon.png");

    private static final ResourceLocation FIRE_WIND_ICON =
            ResourceLocation.fromNamespaceAndPath("sepimod", "textures/icon/fire_wind_icon.png");

    private static final int TILE_SIZE = 32;
    private static final int ICON_SIZE = 24;
    private static final int TILE_SPACING = 4;
    private static final int START_X = 18;
    private static final int START_Y = 45;
    private static final int MAX_COLUMNS = 4;
    private int selectedSpellInfo = -1;

    public SpellbookScreen(
            com.sepinula.sepimod.spellbook.SpellbookMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);

        // spellbook_background.png is now 352x204. Render it at native size.
        this.imageWidth = 352;
        this.imageHeight = 204;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (minecraft.player == null) {
            return false;
        }

        PlayerSpellData data =
                minecraft.player.getData(ModDataAttachments.PLAYER_SPELL_DATA);

        List<Spell> spells = new ArrayList<>(SpellRegistry.all());

        for (int i = 0; i < spells.size(); i++) {
            int column = i % MAX_COLUMNS;
            int row = i / MAX_COLUMNS;

            int tileX = leftPos + START_X + column * (TILE_SIZE + TILE_SPACING);
            int tileY = topPos + START_Y + row * (TILE_SIZE + TILE_SPACING);

            if (mouseX < tileX || mouseX >= tileX + TILE_SIZE
                    || mouseY < tileY || mouseY >= tileY + TILE_SIZE) {
                continue;
            }

            Spell spell = spells.get(i);
            String spellId = spell.id().toString();

            if (button == 1) {
                // Inspecting a spell is independent from equipping it.
                // Right-clicking the same spell again simply keeps its
                // information page open.
                if (selectedSpellInfo != i) {
                    selectedSpellInfo = i;
                }
                return true;
            }

            if (button == 0) {
                if (!data.knowsSpell(spellId)) {
                    return true;
                }

                int activeIndex = data.getActiveSpells().indexOf(spellId);

                if (activeIndex >= 0) {
                    Messages.sendToServer(
                            new PacketSpellbookAction(
                                    PacketSpellbookAction.REMOVE_ACTIVE,
                                    activeIndex,
                                    0
                            )
                    );
                } else {
                    int knownIndex = data.getKnownSpells().indexOf(spellId);
                    if (knownIndex >= 0) {
                        Messages.sendToServer(
                                new PacketSpellbookAction(
                                        PacketSpellbookAction.ADD_KNOWN,
                                        knownIndex,
                                        0
                                )
                        );
                    }
                }

                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderBg(
            GuiGraphics graphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        int x = leftPos;
        int y = topPos;

        graphics.blit(
                BACKGROUND,
                x,
                y,
                0,
                0,
                352,
                204,
                352,
                204
        );

        if (minecraft.player == null) {
            return;
        }

        PlayerSpellData data =
                minecraft.player.getData(ModDataAttachments.PLAYER_SPELL_DATA);

        List<Spell> spells = new ArrayList<>(SpellRegistry.all());

        for (int i = 0; i < spells.size(); i++) {
            int column = i % MAX_COLUMNS;
            int row = i / MAX_COLUMNS;

            int tileX = x + START_X + column * (TILE_SIZE + TILE_SPACING);
            int tileY = y + START_Y + row * (TILE_SIZE + TILE_SPACING);

            Spell spell = spells.get(i);
            String spellId = spell.id().toString();

            boolean learned = data.knowsSpell(spellId);
            boolean active = data.getActiveSpells().contains(spellId);

            // Every spell gets the 32x32 box artwork.
            graphics.blit(
                    EMPTY_ICON,
                    tileX,
                    tileY,
                    0,
                    0,
                    TILE_SIZE,
                    TILE_SIZE,
                    TILE_SIZE,
                    TILE_SIZE
            );

            ResourceLocation icon = getSpellIcon(spell);

            graphics.blit(
                    icon,
                    tileX + (TILE_SIZE - ICON_SIZE) / 2,
                    tileY + (TILE_SIZE - ICON_SIZE) / 2,
                    0,
                    0,
                    ICON_SIZE,
                    ICON_SIZE,
                    ICON_SIZE,
                    ICON_SIZE
            );

            if (active) {
                // Fill the whole 32x32 tile with a subtle gold highlight,
                // then redraw the pixel-art border so the selection is obvious.
                graphics.fill(
                        tileX,
                        tileY,
                        tileX + TILE_SIZE,
                        tileY + TILE_SIZE,
                        0x66E7C46A
                );

                int highlight = 0xFFE7C46A;
                graphics.fill(tileX, tileY, tileX + TILE_SIZE, tileY + 2, highlight);
                graphics.fill(tileX, tileY + TILE_SIZE - 2, tileX + TILE_SIZE, tileY + TILE_SIZE, highlight);
                graphics.fill(tileX, tileY, tileX + 2, tileY + TILE_SIZE, highlight);
                graphics.fill(tileX + TILE_SIZE - 2, tileY, tileX + TILE_SIZE, tileY + TILE_SIZE, highlight);
            }

            if (!learned) {
                boolean hovered =
                        mouseX >= tileX
                                && mouseX < tileX + TILE_SIZE
                                && mouseY >= tileY
                                && mouseY < tileY + TILE_SIZE;

                graphics.setColor(1.0F, 1.0F, 1.0F, hovered ? 0.45F : 1.0F);

                graphics.blit(
                        LOCK_ICON,
                        tileX + (TILE_SIZE - 26) / 2,
                        tileY + (TILE_SIZE - 28) / 2,
                        0,
                        0,
                        26,
                        28,
                        26,
                        28
                );

                graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }

        // Right page: instructions until a spell is inspected.
        int infoX = x + 190;
        int infoY = y + 38;

        if (selectedSpellInfo < 0 || selectedSpellInfo >= spells.size()) {
            graphics.drawString(font, "SPELL INFORMATION", infoX, infoY, 0xFF2B241D);
            graphics.drawString(font, "Left-click", infoX, infoY + 24, 0xFF3F6B35);
            graphics.drawString(font, "Equip / unequip", infoX, infoY + 35, 0xFF2B241D);
            graphics.drawString(font, "a learned spell", infoX, infoY + 46, 0xFF2B241D);
            graphics.drawString(font, "Right-click", infoX, infoY + 65, 0xFF3F6B35);
            graphics.drawString(font, "View spell information", infoX, infoY + 76, 0xFF2B241D);
            graphics.drawString(font, "V / X", infoX, infoY + 95, 0xFF3F6B35);
            graphics.drawString(font, "Change combat spell", infoX, infoY + 106, 0xFF2B241D);
            graphics.drawString(font, "R", infoX, infoY + 125, 0xFF3F6B35);
        } else {
            Spell selected = spells.get(selectedSpellInfo);
            boolean learned = data.knowsSpell(selected.id().toString());

            graphics.blit(getSpellIcon(selected), infoX, infoY, 0, 0,
                    ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);

            graphics.drawString(font, selected.displayName(), infoX + 34, infoY + 5, 0xFF2B241D);
            graphics.drawString(font, learned ? "Learned" : "Locked",
                    infoX + 34, infoY + 18, learned ? 0xFF3F6B35 : 0xFF8B3333);
            graphics.drawString(font, "Mana: " + selected.manaCost(),
                    infoX, infoY + 32, 0xFF2B241D);

            int descriptionY = infoY + 47;
            List<net.minecraft.util.FormattedCharSequence> wrapped =
                    font.split(Component.literal(selected.description()), 135);

            for (int line = 0; line < wrapped.size(); line++) {
                graphics.drawString(font, wrapped.get(line),
                        infoX, descriptionY + line * 10, 0xFF2B241D);
            }

            if (!learned) {
                graphics.drawString(font, "Locked - learn this spell to equip it.",
                        infoX, descriptionY + wrapped.size() * 10 + 12, 0xFF8B3333);
            }
        }
    }

    private ResourceLocation getSpellIcon(Spell spell) {
        return switch (spell.id().getPath()) {
            case "fireball" -> FIREBALL_ICON;
            case "gust" -> GUST_ICON;
            case "ice_shard" -> ICE_SHARD_ICON;
            case "fire_wind" -> FIRE_WIND_ICON;
            default -> EMPTY_ICON;
        };
    }

    @Override
    protected void renderLabels(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        // The background artwork owns all static text.
    }
}
