package com.sepinula.sepimod.client.gui;

import com.sepinula.sepimod.SepiMod;
import com.sepinula.sepimod.spellbook.SpellbookHelper;
import com.sepinula.sepimod.spells.Spell;
import com.sepinula.sepimod.spells.SpellRegistry;
import com.sepinula.sepimod.util.ModDataAttachments;
import com.sepinula.sepimod.util.PlayerSpellData;
import com.sepinula.sepimod.util.PlayerSpellCooldownData;
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

    private static final ResourceLocation EMPTY_ICON =
            ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "textures/icon/empty_box_icon.png");

    private static final ResourceLocation FIREBALL_ICON =
            ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "textures/icon/fireball_icon.png");

    private static final ResourceLocation GUST_ICON =
            ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "textures/icon/gust_icon.png");

    private static final ResourceLocation ICE_SHARD_ICON =
            ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "textures/icon/ice_shard_icon.png");

    private static final ResourceLocation FIRE_WIND_ICON =
            ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "textures/icon/fire_wind_icon.png");

    @SubscribeEvent
    public static void render(RenderGuiLayerEvent.Post event) {
        if (!event.getName().equals(VanillaGuiLayers.HOTBAR)) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) {
            return;
        }

        boolean spellbookEquipped = SpellbookHelper.hasSpellbook(mc.player);
        boolean spellbookInMainHand = mc.player.getMainHandItem().getItem() instanceof com.sepinula.sepimod.spellbook.SpellbookItem;
        boolean spellbookInOffHand = mc.player.getOffhandItem().getItem() instanceof com.sepinula.sepimod.spellbook.SpellbookItem;

        // The HUD is available when a spellbook is equipped OR currently held.
        if (!spellbookEquipped && !spellbookInMainHand && !spellbookInOffHand) {
            return;
        }

        PlayerSpellData data = mc.player.getData(ModDataAttachments.PLAYER_SPELL_DATA);
        int capacity = Math.min(
                9,
                Math.max(0, SpellbookHelper.getCapacity(mc.player))
        );

        // A spellbook held without ever being equipped has no client attachment yet.
        // Read its tier directly so the HUD can still appear.
        if (capacity == 0) {
            if (spellbookInMainHand && mc.player.getMainHandItem().getItem() instanceof com.sepinula.sepimod.spellbook.SpellbookItem book) {
                capacity = book.getSpellSlots();
            } else if (spellbookInOffHand && mc.player.getOffhandItem().getItem() instanceof com.sepinula.sepimod.spellbook.SpellbookItem book) {
                capacity = book.getSpellSlots();
            }
        }

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

            graphics.blit(
                    EMPTY_ICON,
                    x,
                    y,
                    0,
                    0,
                    32,
                    32,
                    32,
                    32
            );

            if (selected) {
                graphics.fill(
                        x - 1,
                        y - 1,
                        x + 33,
                        y + 33,
                        0xFFE7C46A
                );

                graphics.blit(
                        EMPTY_ICON,
                        x,
                        y,
                        0,
                        0,
                        32,
                        32,
                        32,
                        32
                );
            }

            if (i < data.getActiveSpells().size()) {
                Spell spell = SpellRegistry.get(
                        ResourceLocation.parse(data.getActiveSpells().get(i))
                );

                if (spell != null) {
                    ResourceLocation icon = switch (spell.id().getPath()) {
                        case "fireball" -> FIREBALL_ICON;
                        case "gust" -> GUST_ICON;
                        case "ice_shard" -> ICE_SHARD_ICON;
                        case "fire_wind" -> FIRE_WIND_ICON;
                        default -> EMPTY_ICON;
                    };

                    graphics.blit(
                            icon,
                            x + 4,
                            y + 4,
                            0,
                            0,
                            24,
                            24,
                            24,
                            24
                    );

                    if (selected) {
                        PlayerSpellCooldownData cooldowns =
                                mc.player.getData(ModDataAttachments.PLAYER_SPELL_COOLDOWNS);
                        int remainingTicks = cooldowns.getRemainingTicks(spell.id().toString());

                        if (remainingTicks > 0 && spell.cooldownTicks() > 0) {
                            float progress = Math.min(
                                    1.0F,
                                    remainingTicks / (float) spell.cooldownTicks()
                            );

                            // The dark mask clears from top to bottom as the cooldown expires.
                            int clearHeight = Math.round(32.0F * (1.0F - progress));
                            graphics.fill(
                                    x,
                                    y + clearHeight,
                                    x + 32,
                                    y + 32,
                                    0xB8000000
                            );

                            String timer = String.format(
                                    java.util.Locale.ROOT,
                                    "%.1f",
                                    remainingTicks / 20.0F
                            );

                            graphics.drawCenteredString(
                                    mc.font,
                                    timer,
                                    x + 16,
                                    y + 12,
                                    0xFFFFFFFF
                            );
                        }
                    }

                } else {

                }
            }
        }

        // Only the spell currently selected for casting gets a name.
        String selectedId = data.getSelectedSpellId();
        if (!selectedId.isEmpty()) {
            Spell selectedSpell = SpellRegistry.get(ResourceLocation.parse(selectedId));
            if (selectedSpell != null) {
                int selectedX = startX + data.getSelectedSpellIndex() * 34 + 16;

                graphics.drawCenteredString(
                        mc.font,
                        selectedSpell.displayName(),
                        selectedX,
                        y + 34,
                        0xFFFFE6A5
                );
            }
        }

    }
}
