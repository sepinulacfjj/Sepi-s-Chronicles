package com.sepinula.sepimod.event;

import com.mojang.blaze3d.platform.InputConstants;
import com.sepinula.sepimod.SepiMod;
import com.sepinula.sepimod.client.gui.ClassSelectionScreen;
import com.sepinula.sepimod.client.gui.StatUpgradeScreen;
import com.sepinula.sepimod.client.model.GoblinModel;
import com.sepinula.sepimod.client.renderer.GoblinRenderer;
import com.sepinula.sepimod.init.ModEntities;
import com.sepinula.sepimod.init.ModModelLayers;
import com.sepinula.sepimod.util.ModDataAttachments;
import com.sepinula.sepimod.util.PlayerStats;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = SepiMod.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class ClientEvents {

    public static final KeyMapping classKey = new KeyMapping("key.sepimod.class", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, "key.categories.sepimod");
    public static final KeyMapping statsKey = new KeyMapping("key.sepimod.stats", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, "key.categories.sepimod");

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // This connects the Entity Type to the Renderer class you just made
        event.registerEntityRenderer(ModEntities.GOBLIN.get(), GoblinRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // This points to BabyGoblin_Model.createBodyLayer() which is the method from your export
        event.registerLayerDefinition(ModModelLayers.GOBLIN_LAYER, GoblinModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        // NeoForge 1.21.1 uses ClientTickEvent.Post instead of TickEvent.ClientTickEvent
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {

            // Handle Class Selection Key (O)
            while (classKey.consumeClick()) {
                // Accessing data via the NeoForge Attachment system
                PlayerStats stats = mc.player.getData(ModDataAttachments.PLAYER_STATS);

                if (stats.getPlayerClass().equals("NONE")) {
                    mc.setScreen(new ClassSelectionScreen());
                } else {
                    mc.player.displayClientMessage(
                            Component.literal("§cYou have already chosen a path!"),
                            true
                    );
                }
            }

            // Handle Stat Upgrade Key (J)
            while (statsKey.consumeClick()) {
                mc.setScreen(new StatUpgradeScreen());
            }
        }
    }

    @EventBusSubscriber(modid = SepiMod.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(classKey);
            event.register(statsKey);
        }
    }
}