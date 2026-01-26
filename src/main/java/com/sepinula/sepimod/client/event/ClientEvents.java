package com.sepinula.sepimod.client.event;

import com.mojang.blaze3d.platform.InputConstants;
import com.sepinula.sepimod.client.gui.ClassSelectionScreen;
import com.sepinula.sepimod.client.gui.StatUpgradeScreen;
import com.sepinula.sepimod.client.model.Baby_GoblinModel;
import com.sepinula.sepimod.client.renderer.Baby_GoblinRenderer;
import com.sepinula.sepimod.init.ModEntities;
import com.sepinula.sepimod.init.ModModelLayers;
import com.sepinula.sepimod.util.ModDataAttachments;
import com.sepinula.sepimod.util.PlayerStats;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;
import java.util.List;

// --- GAME BUS: Handles Ticks and Input ---
@EventBusSubscriber(value = Dist.CLIENT)
public class ClientEvents {

    public static final KeyMapping classKey = new KeyMapping("key.sepimod.class", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, "key.categories.sepimod");
    public static final KeyMapping statsKey = new KeyMapping("key.sepimod.stats", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, "key.categories.sepimod");
    public static final KeyMapping lockOnKey = new KeyMapping("key.sepimod.lock_on", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.categories.sepimod");

    private static boolean isLockedOn = false;
    private static LivingEntity target = null;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // 1. Handle Keys
        while (classKey.consumeClick()) {
            PlayerStats stats = mc.player.getData(ModDataAttachments.PLAYER_STATS);
            if (stats.getPlayerClass().equals("NONE")) {
                mc.setScreen(new ClassSelectionScreen());
            } else {
                mc.player.displayClientMessage(Component.literal("§cYou already have a class!"), true);
            }
        }

        while (statsKey.consumeClick()) {
            mc.setScreen(new StatUpgradeScreen());
        }

        while (lockOnKey.consumeClick()) {
            isLockedOn = !isLockedOn;
            if (isLockedOn) {
                target = findTarget(mc);
                if (target == null) {
                    isLockedOn = false;
                    mc.player.displayClientMessage(Component.literal("§cNo target found!"), true);
                } else {
                    mc.player.displayClientMessage(Component.literal("§aLocked on: " + target.getDisplayName().getString()), true);
                }
            } else {
                target = null;
                mc.player.displayClientMessage(Component.literal("§7Lock-on: Disabled"), true);
            }
        }

        // 2. Logic Check (Range/Life)
        if (isLockedOn && target != null) {
            if (!target.isAlive() || mc.player.distanceTo(target) > 15.0f || !mc.player.hasLineOfSight(target)) {
                target = null;
                isLockedOn = false;
                mc.player.displayClientMessage(Component.literal("§7Lock-on lost"), true);
            }
        }
    }

    // --- LOCK ON ROTATION: Handles smooth movement and mouse override ---
    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (isLockedOn && target != null && mc.player != null) {
            // Use Delta Ticks for maximum smoothness (Frame-rate independent)
            float partialTicks = (float) event.getPartialTick();

            Vec3 playerPos = mc.player.getEyePosition(partialTicks);
            Vec3 targetPos = target.getBoundingBox().getCenter();

            double diffX = targetPos.x - playerPos.x;
            double diffY = targetPos.y - playerPos.y;
            double diffZ = targetPos.z - playerPos.z;
            double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

            float targetYaw = (float) (Math.toDegrees(Math.atan2(-diffX, diffZ)));
            float targetPitch = (float) (-Math.toDegrees(Math.atan2(diffY, diffXZ)));

            // High lerp (0.6f) to keep up with fast entities like chickens
            float newYaw = lerpAngle(mc.player.getYRot(), targetYaw, 0.6f);
            float newPitch = lerpAngle(mc.player.getXRot(), targetPitch, 0.6f);

            // Set player rotation
            mc.player.setYRot(newYaw);
            mc.player.setXRot(newPitch);

            // IMPORTANT: Setting event angles overrides the mouse input, stopping the shake
            event.setYaw(newYaw);
            event.setPitch(newPitch);
        }
    }

    private static LivingEntity findTarget(Minecraft mc) {
        AABB area = mc.player.getBoundingBox().inflate(12.0D);
        List<LivingEntity> entities = mc.level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != mc.player && e.isAlive() && mc.player.hasLineOfSight(e));

        return entities.stream()
                .min(Comparator.comparingDouble(mc.player::distanceTo))
                .orElse(null);
    }

    private static float lerpAngle(float start, float end, float pct) {
        float d = end - start;
        while (d < -180.0F) d += 360.0F;
        while (d >= 180.0F) d -= 360.0F;
        return start + d * pct;
    }

    // --- MOD BUS: Handles Registration ---
    @EventBusSubscriber(value = Dist.CLIENT)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(classKey);
            event.register(statsKey);
            event.register(lockOnKey);
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.BabyGOBLIN.get(), Baby_GoblinRenderer::new);
        }

        @SubscribeEvent
        public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(ModModelLayers.GOBLIN_LAYER, Baby_GoblinModel::createBodyLayer);
        }
    }
}