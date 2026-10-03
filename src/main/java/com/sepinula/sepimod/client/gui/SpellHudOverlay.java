package com.sepinula.sepimod.client.gui;

import com.sepinula.sepimod.SepiMod;
import com.sepinula.sepimod.spellbook.SpellbookHelper;
import com.sepinula.sepimod.spells.Spell;
import com.sepinula.sepimod.spells.SpellRegistry;
import com.sepinula.sepimod.util.ModDataAttachments;
import com.sepinula.sepimod.util.PlayerSpellData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = SepiMod.MODID, value = Dist.CLIENT)
public class SpellHudOverlay {

    @SubscribeEvent
    public static void render(RenderGuiLayerEvent.Post event) {
        if (!event.getName().equals(VanillaGuiLayers.HOTBAR)) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) {
            return;
        }

        boolean spellbookInMainHand = mc.player.getMainHandItem().getItem() instanceof com.sepinula.sepimod.spellbook.SpellbookItem;
        boolean spellbookInOffHand = mc.player.getOffhandItem().getItem() instanceof com.sepinula.sepimod.spellbook.SpellbookItem;

        if (!spellbookInMainHand && !spellbookInOffHand) {
            return;
        }

        PlayerSpellData data = mc.player.getData(ModDataAttachments.PLAYER_SPELL_DATA);
        int capacity = Math.min(
                9,
                Math.max(0, SpellbookHelper.getCapacity(mc.player))
        );

        if (capacity == 0) {
            return;
        }

        int screenWidth = event.getGuiGraphics().guiWidth();
        int screenHeight = event.getGuiGraphics().guiHeight();
        int y = screenHeight - 55;
        int total = capacity * 34;
        int startX = 12;

        GuiGraphics graphics = event.getGuiGraphics();

        for (int i = 0; i < capacity; i++) {
            int x = startX + i * 34;
            boolean selected = i == data.getSelectedSpellIndex();

            graphics.fill(
                    x, y, x + 32, y + 32,
                    selected ? 0xFFD8B85A : 0xAA111016
            );
            graphics.fill(x + 2, y + 2, x + 30, y + 30, 0xFF2A2230);

            if (i < data.getActiveSpells().size()) {
                Spell spell = SpellRegistry.get(
                        ResourceLocation.parse(data.getActiveSpells().get(i))
                );

                if (spell != null) {
                    graphics.drawCenteredString(
                            mc.font,
                            spell.displayName(),
                            x + 16,
                            y + 34,
                            selected ? 0xFFFFE6A5 : 0xFFE8DCC7
                    );
                }
            }
        }

    }
}
