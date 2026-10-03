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

import java.util.ArrayList;
import java.util.List;

public class SpellbookScreen extends AbstractContainerScreen<com.sepinula.sepimod.spellbook.SpellbookMenu> {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath("sepimod", "textures/gui/spellbook.png");

    public SpellbookScreen(com.sepinula.sepimod.spellbook.SpellbookMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
        this.titleLabelX = 72;
        this.inventoryLabelX = 8;
        this.titleLabelY = 7;
        this.inventoryLabelY = 74;
    }

    @Override
    protected void init() {
        super.init();
        int x = leftPos;
        int y = topPos;

        addRenderableWidget(Button.builder(Component.literal("Add"), b -> {
            PlayerSpellData data = minecraft.player.getData(ModDataAttachments.PLAYER_SPELL_DATA);
            if (!data.getKnownSpells().isEmpty()) {
                int index = Math.min(knownScrollIndex(), data.getKnownSpells().size() - 1);
                Messages.sendToServer(new PacketSpellbookAction(PacketSpellbookAction.ADD_KNOWN, index, 0));
            }
        }).bounds(x + 104, y + 52, 30, 16).build());

        addRenderableWidget(Button.builder(Component.literal("Remove"), b -> {
            PlayerSpellData data = minecraft.player.getData(ModDataAttachments.PLAYER_SPELL_DATA);
            int index = data.getSelectedSpellIndex();
            if (index >= 0 && index < data.getActiveSpells().size()) {
                Messages.sendToServer(new PacketSpellbookAction(PacketSpellbookAction.REMOVE_ACTIVE, index, 0));
            }
        }).bounds(x + 137, y + 52, 32, 16).build());

        addRenderableWidget(Button.builder(Component.literal("Combine"), b -> {
            PlayerSpellData data = minecraft.player.getData(ModDataAttachments.PLAYER_SPELL_DATA);
            if (data.getActiveSpells().size() >= 2) {
                Messages.sendToServer(new PacketSpellbookAction(PacketSpellbookAction.COMBINE, 0, 1));
            }
        }).bounds(x + 62, y + 68, 50, 16).build());
    }

    private int knownScrollIndex() {
        return 0;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF17131A);
        graphics.fill(x + 4, y + 4, x + imageWidth - 4, y + 70, 0xFF241D29);
        graphics.fill(x + 7, y + 7, x + imageWidth - 7, y + 69, 0xFF302533);

        // Replace this single texture later with your finished artwork.
        // Expected path: assets/sepimod/textures/gui/spellbook.png
        if (minecraft.getResourceManager().getResource(BACKGROUND).isPresent()) {
            graphics.blit(BACKGROUND, x, y, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        }

        graphics.fill(x + 12, y + 22, x + 52, y + 66, 0xFF17131A);

        PlayerSpellData data = minecraft.player.getData(ModDataAttachments.PLAYER_SPELL_DATA);
        int capacity = Math.max(0, Math.min(9, com.sepinula.sepimod.spellbook.SpellbookHelper.getCapacity(minecraft.player)));

        for (int i = 0; i < capacity; i++) {
            int slotX = x + 62 + i * 21;
            int border = i == data.getSelectedSpellIndex() ? 0xFFE7C46A : 0xFF111016;
            graphics.fill(slotX - 1, y + 27, slotX + 19, y + 47, border);
            graphics.fill(slotX, y + 28, slotX + 18, y + 46, 0xFF3B2E3B);

            if (i < data.getActiveSpells().size()) {
                Spell spell = SpellRegistry.get(ResourceLocation.parse(data.getActiveSpells().get(i)));
                if (spell != null) {
                    graphics.drawCenteredString(font, spell.displayName(), slotX + 9, y + 48, 0xFFE8DCC7);
                }
            }
        }

        graphics.drawString(font, "Known", x + 62, y + 7, 0xFFD8C8A8);

        List<String> known = data.getKnownSpells();
        int row = 0;
        for (String id : known) {
            if (row >= 2) break;
            Spell spell = SpellRegistry.get(ResourceLocation.parse(id));
            if (spell != null) {
                graphics.drawString(font, spell.displayName(), x + 62, y + 18 + row * 12, 0xFFE8DCC7);
            }
            row++;
        }

        graphics.drawString(font, "Book", x + 20, y + 14, 0xFFD8C8A8);
        graphics.drawString(font, "Active Spells", x + 62, y + 18, 0xFFD8C8A8);

        // Vanilla inventory background.
        graphics.blit(net.minecraft.resources.ResourceLocation.withDefaultNamespace("textures/gui/container/inventory.png"),
                x, y + 72, 0, 0, 176, 94, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, "Spellbook", titleLabelX, titleLabelY, 0xFFE8DCC7);
        graphics.drawString(font, "Inventory", inventoryLabelX, inventoryLabelY, 0xFFD8C8A8);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}