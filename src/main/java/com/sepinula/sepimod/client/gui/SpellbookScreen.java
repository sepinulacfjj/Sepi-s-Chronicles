package com.sepinula.sepimod.client.gui;

import com.sepinula.sepimod.network.Messages;
import com.sepinula.sepimod.network.PacketSpellbookAction;
import com.sepinula.sepimod.spells.Spell;
import com.sepinula.sepimod.spells.SpellRegistry;
import com.sepinula.sepimod.util.ModDataAttachments;
import com.sepinula.sepimod.util.PlayerSpellData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class SpellbookScreen extends AbstractContainerScreen<com.sepinula.sepimod.spellbook.SpellbookMenu> {

    private int combineFirst = -1;
    private int combineSecond = -1;

    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath("sepimod", "textures/gui/spellbook_background.png");

    private static final ResourceLocation COMBINE_BUTTON =
            ResourceLocation.fromNamespaceAndPath("sepimod", "textures/button/combine.png");

    private static final ResourceLocation EMPTY_ICON =
            ResourceLocation.fromNamespaceAndPath("sepimod", "textures/icon/empty_box_icon.png");

    private static final ResourceLocation FIREBALL_ICON =
            ResourceLocation.fromNamespaceAndPath("sepimod", "textures/icon/fireball_icon.png");

    private static final ResourceLocation GUST_ICON =
            ResourceLocation.fromNamespaceAndPath("sepimod", "textures/icon/gust_icon.png");

    private static final ResourceLocation ICE_SHARD_ICON =
            ResourceLocation.fromNamespaceAndPath("sepimod", "textures/icon/ice_shard_icon.png");

    private static final ResourceLocation FIRE_WIND_ICON =
            ResourceLocation.fromNamespaceAndPath("sepimod", "textures/icon/fire_wind_icon.png");

    public SpellbookScreen(
            com.sepinula.sepimod.spellbook.SpellbookMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.titleLabelX = 72;
        this.titleLabelY = 7;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 74;
    }

    @Override
    protected void init() {
        super.init();

        int x = leftPos;
        int y = topPos;

        for (int i = 0; i < 5; i++) {
            final int knownIndex = i;

            addRenderableWidget(
                    Button.builder(
                                    Component.literal("+"),
                                    b -> Messages.sendToServer(
                                            new PacketSpellbookAction(
                                                    PacketSpellbookAction.ADD_KNOWN,
                                                    knownIndex,
                                                    0
                                            )
                                    )
                            )
                            .bounds(x + 146, y + 16 + i * 10, 14, 10)
                            .build()
            );
        }

        addRenderableWidget(
                Button.builder(Component.literal("Remove"), b -> {
                    if (combineFirst >= 0) {
                        Messages.sendToServer(
                                new PacketSpellbookAction(
                                        PacketSpellbookAction.REMOVE_ACTIVE,
                                        combineFirst,
                                        0
                                )
                        );
                        combineFirst = -1;
                    }
                }).bounds(x + 130, y + 67, 42, 13).build()
        );

        addRenderableWidget(
                Button.builder(Component.empty(), b -> {
                    if (combineFirst >= 0
                            && combineSecond >= 0
                            && combineFirst != combineSecond) {

                        Messages.sendToServer(
                                new PacketSpellbookAction(
                                        PacketSpellbookAction.COMBINE,
                                        combineFirst,
                                        combineSecond
                                )
                        );

                        combineFirst = -1;
                        combineSecond = -1;
                    }
                }).bounds(x + 47, y + 67, 81, 13).build()
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int i = 0; i < 9; i++) {
            int sx = leftPos + 62 + i * 21;
            int sy = topPos + 28;

            if (mouseX >= sx
                    && mouseX < sx + 18
                    && mouseY >= sy
                    && mouseY < sy + 18) {

                if (combineFirst < 0) {
                    combineFirst = i;
                } else if (combineSecond < 0 && combineFirst != i) {
                    combineSecond = i;
                } else {
                    combineFirst = i;
                    combineSecond = -1;
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

        graphics.fill(
                x,
                y,
                x + imageWidth,
                y + imageHeight,
                0xFF17131A
        );

        if (minecraft.getResourceManager().getResource(BACKGROUND).isPresent()) {
            graphics.blit(
                    BACKGROUND,
                    x,
                    y,
                    0,
                    0,
                    imageWidth,
                    imageHeight,
                    imageWidth,
                    imageHeight
            );
        } else {
            graphics.fill(x + 4, y + 4, x + imageWidth - 4, y + 70, 0xFF241D29);
            graphics.fill(x + 7, y + 7, x + imageWidth - 7, y + 69, 0xFF302533);
        }

        PlayerSpellData data =
                minecraft.player.getData(ModDataAttachments.PLAYER_SPELL_DATA);

        int capacity = Math.min(
                9,
                Math.max(
                        0,
                        com.sepinula.sepimod.spellbook.SpellbookHelper
                                .getCapacity(minecraft.player)
                )
        );

        graphics.drawString(
                font,
                "ACTIVE SPELLS",
                x + 62,
                y + 18,
                0xFFD8C8A8
        );

        for (int i = 0; i < capacity; i++) {
            int slotX = x + 62 + i * 21;
            int border =
                    i == data.getSelectedSpellIndex()
                            ? 0xFFE7C46A
                            : 0xFF111016;

            graphics.fill(
                    slotX - 1,
                    y + 27,
                    slotX + 19,
                    y + 47,
                    border
            );

            graphics.fill(
                    slotX,
                    y + 28,
                    slotX + 18,
                    y + 46,
                    0xFF3B2E3B
            );

            if (i < data.getActiveSpells().size()) {
                Spell spell = SpellRegistry.get(
                        ResourceLocation.parse(data.getActiveSpells().get(i))
                );

                if (spell != null) {
                    drawSpellIcon(graphics, spell, slotX + 1, y + 29, 16, 16);
                }
            } else {
                graphics.blit(
                        EMPTY_ICON,
                        slotX + 1,
                        y + 29,
                        0,
                        0,
                        16,
                        16,
                        16,
                        16
                );
            }
        }

        graphics.drawString(font, "KNOWN SPELLS", x + 62, y + 7, 0xFFD8C8A8);

        List<String> known = data.getKnownSpells();

        for (int row = 0; row < Math.min(5, known.size()); row++) {
            Spell spell = SpellRegistry.get(
                    ResourceLocation.parse(known.get(row))
            );

            if (spell != null) {
                drawSpellIcon(graphics, spell, x + 62, y + 27 + row * 10, 10, 10);

                graphics.drawString(
                        font,
                        spell.displayName(),
                        x + 74,
                        y + 28 + row * 10,
                        0xFFE8DCC7
                );
            }
        }

        graphics.drawString(
                font,
                "BOOK",
                x + 20,
                y + 14,
                0xFFD8C8A8
        );

    }

    @Override
    protected void renderLabels(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        // The background artwork contains the page/header layout. Dynamic
        // spell names and icons are rendered separately in renderBg().
    }

    private void drawSpellIcon(
            GuiGraphics graphics,
            Spell spell,
            int x,
            int y,
            int width,
            int height
    ) {
        ResourceLocation icon = switch (spell.id().getPath()) {
            case "fireball" -> FIREBALL_ICON;
            case "gust" -> GUST_ICON;
            case "ice_shard" -> ICE_SHARD_ICON;
            case "fire_wind" -> FIRE_WIND_ICON;
            default -> EMPTY_ICON;
        };

        graphics.blit(
                icon,
                x,
                y,
                0,
                0,
                width,
                height,
                16,
                16
        );
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        // The actual button is still a clickable widget, but the visual comes
        // from the custom texture you made rather than the vanilla button style.
        graphics.blit(
                COMBINE_BUTTON,
                leftPos + 47,
                topPos + 67,
                0,
                0,
                81,
                13,
                81,
                13
        );

        renderTooltip(graphics, mouseX, mouseY);
    }
}
