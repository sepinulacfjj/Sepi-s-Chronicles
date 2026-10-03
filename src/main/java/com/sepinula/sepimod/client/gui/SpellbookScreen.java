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
    private boolean showSelectedSpellName = false;

    private static final float GUI_SCALE = 1.5f;

    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath("sepimod", "textures/gui/spellbook_background.png");

    private static final ResourceLocation COMBINE_BUTTON =
            ResourceLocation.fromNamespaceAndPath("sepimod", "textures/button/combine.png");

    private static final ResourceLocation REMOVE_BUTTON =
            ResourceLocation.fromNamespaceAndPath("sepimod", "textures/button/remove.png");

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
        this.imageWidth = 264;
        this.imageHeight = 249;
        this.titleLabelX = 72;
        this.titleLabelY = 7;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 74;
    }

    private static int s(int value) {
        return Math.round(value * GUI_SCALE);
    }

    @Override
    protected void init() {
        super.init();

        int x = leftPos;
        int y = topPos;

        for (int i = 0; i < 5; i++) {
            final int knownIndex = i;

            Button addButton = Button.builder(
                            Component.empty(),
                            b -> Messages.sendToServer(
                                    new PacketSpellbookAction(
                                            PacketSpellbookAction.ADD_KNOWN,
                                            knownIndex,
                                            0
                                    )
                            )
                    )
                    .bounds(x + s(146), y + s(16 + i * 10), s(14), s(10))
                    .build();

            addButton.setAlpha(0.0F);
            addRenderableWidget(addButton);
        }

        Button removeButton = Button.builder(Component.empty(), b -> {
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
                }).bounds(x + s(130), y + s(67), s(42), s(13)).build();

        removeButton.setAlpha(0.0F);
        addRenderableWidget(removeButton);

        Button combineButton = Button.builder(Component.empty(), b -> {
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
                }).bounds(x + s(47), y + s(67), s(81), s(13)).build();

        combineButton.setAlpha(0.0F);
        addRenderableWidget(combineButton);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        PlayerSpellData data =
                minecraft.player.getData(ModDataAttachments.PLAYER_SPELL_DATA);

        for (int i = 0; i < 9; i++) {
            int sx = leftPos + s(62 + i * 21);
            int sy = topPos + s(93);

            if (mouseX >= sx
                    && mouseX < sx + s(18)
                    && mouseY >= sy
                    && mouseY < sy + s(18)) {

                if (i < data.getActiveSpells().size()) {
                    showSelectedSpellName = true;
                } else {
                    showSelectedSpellName = false;
                }

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
            graphics.pose().pushPose();
            graphics.pose().translate(x, y, 0);
            graphics.pose().scale(GUI_SCALE, GUI_SCALE, 1.0f);

            graphics.blit(
                    BACKGROUND,
                    0,
                    0,
                    0,
                    0,
                    176,
                    166,
                    176,
                    166
            );

            graphics.pose().popPose();
        } else {
            graphics.fill(x + 4, y + 4, x + imageWidth - 4, y + 70, 0xFF241D29);
            graphics.fill(x + 7, y + 7, x + imageWidth - 7, y + 69, 0xFF302533);
        }

        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(GUI_SCALE, GUI_SCALE, 1.0f);

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
                s(62),
                s(82),
                0xFFD8C8A8
        );

        for (int i = 0; i < capacity; i++) {
            int slotX = s(62 + i * 21);
            int slotY = s(93);
            // The background already contains the slot frames.
            // Only the selected slot receives a dynamic highlight.
            if (i == data.getSelectedSpellIndex()) {
                graphics.fill(
                        slotX - s(1),
                        slotY - s(1),
                        slotX + s(19),
                        slotY + s(19),
                        0xFFE7C46A
                );
            }

            if (i < data.getActiveSpells().size()) {
                Spell spell = SpellRegistry.get(
                        ResourceLocation.parse(data.getActiveSpells().get(i))
                );

                if (spell != null) {
                    drawSpellIcon(graphics, spell, slotX + s(1), slotY + s(1), s(16), s(16));
                }
            }
        }

        if (showSelectedSpellName) {
            String selectedId = data.getSelectedSpellId();
            if (!selectedId.isEmpty()) {
                Spell selectedSpell = SpellRegistry.get(ResourceLocation.parse(selectedId));
                if (selectedSpell != null) {
                    graphics.drawCenteredString(
                            font,
                            selectedSpell.displayName(),
                            s(62 + data.getSelectedSpellIndex() * 21 + 8),
                            s(116),
                            0xFF2B241D
                    );
                }
            }
        }

        graphics.pose().popPose();
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
    protected void renderLabels(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        // The background artwork owns the title and page labels.
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

        graphics.pose().pushPose();
        graphics.pose().translate(leftPos, topPos, 0);
        graphics.pose().scale(GUI_SCALE, GUI_SCALE, 1.0f);

        graphics.blit(
                COMBINE_BUTTON,
                47,
                67,
                0,
                0,
                81,
                13,
                81,
                13
        );

        graphics.blit(
                REMOVE_BUTTON,
                130,
                67,
                0,
                0,
                42,
                13,
                42,
                13
        );

        graphics.pose().popPose();

        renderTooltip(graphics, mouseX, mouseY);
    }
}
