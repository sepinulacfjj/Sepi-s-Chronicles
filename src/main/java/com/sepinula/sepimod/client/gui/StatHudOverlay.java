package com.sepinula.sepimod.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sepinula.sepimod.SepiMod;
import com.sepinula.sepimod.util.ModDataAttachments;
import com.sepinula.sepimod.util.PlayerStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = SepiMod.MODID, value = Dist.CLIENT)
public class StatHudOverlay {
    private static final ResourceLocation HUD_TEXTURE = ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "textures/gui/status_bars.png");

    @SubscribeEvent
    public static void onRenderGui(RenderGuiLayerEvent.Pre event) {
        if (event.getName().equals(VanillaGuiLayers.PLAYER_HEALTH)) {
            event.setCanceled(true);
        }
        if (event.getName().equals(VanillaGuiLayers.HOTBAR)) {
            renderDBCStyleBars(event.getGuiGraphics());
        }
    }

    private static void renderDBCStyleBars(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui) return;

        PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);

        int x = 10;
        int y = 10;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, HUD_TEXTURE);
        RenderSystem.enableBlend();

        // --- 1. DRAW FRAME FIRST (The Background) ---
        // Draws the main HUD frame from your texture
        graphics.blit(HUD_TEXTURE, x, y, 0, 0, 168, 45, 256, 256);

        // --- 2. DRAW BARS SECOND (The Overlay) ---

        // HEALTH (Red)
        float healthPct = player.getHealth() / player.getMaxHealth();
        int healthWidth = (int) (healthPct * 121);
        if (healthWidth > 0) {
            // Draws the red bar at your adjusted X+26 position
            graphics.blit(HUD_TEXTURE, x + 26, y + 21, 26, 45, healthWidth, 7, 256, 256);
        }

        // STAMINA (Yellow)
        float staminaPct = player.getFoodData().getFoodLevel() / 20f;
        int staminaWidth = (int) (staminaPct * 128);
        if (staminaWidth > 0) {
            graphics.blit(HUD_TEXTURE, x + 21, y + 32, 21, 56, staminaWidth, 4, 256, 256);
        }

        // MANA (Blue)
        float manaPct = stats.getCurrentMana() / stats.getMaxMana();
        int manaWidth = (int) (manaPct * 128);
        if (manaWidth > 0) {
            graphics.blit(HUD_TEXTURE, x + 21, y + 37, 21, 61, manaWidth, 4, 256, 256);
        }

        RenderSystem.disableBlend();
    }
}