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
import com.sepinula.sepimod.util.RpgArchetype;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

public class ClientEvents {

    public static final KeyMapping classKey = new KeyMapping("key.sepimod.class", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, "key.categories.sepimod");
    public static final KeyMapping statsKey = new KeyMapping("key.sepimod.stats", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, "key.categories.sepimod");
    public static final KeyMapping lockOnKey = new KeyMapping("key.sepimod.lock_on", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.categories.sepimod");

    private static boolean isLockedOn = false;
    private static LivingEntity target = null;

    public static void init(IEventBus modBus) {
        modBus.addListener(ClientEvents::onKeyRegister);
        modBus.addListener(ClientEvents::registerRenderers);
        modBus.addListener(ClientEvents::registerLayers);

        NeoForge.EVENT_BUS.addListener(ClientEvents::onClientTick);
        NeoForge.EVENT_BUS.addListener(ClientEvents::onComputeCameraAngles);
    }

    private static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(classKey);
        event.register(statsKey);
        event.register(lockOnKey);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BabyGOBLIN.get(), Baby_GoblinRenderer::new);
    }

    private static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.GOBLIN_LAYER, Baby_GoblinModel::createBodyLayer);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Change this section in ClientEvents.java
        while (classKey.consumeClick()) {
            PlayerStats stats = mc.player.getData(ModDataAttachments.PLAYER_STATS);
            // Check if archetype is NONE using the Enum
            if (stats.getArchetype() == RpgArchetype.NONE) {
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
                    mc.player.displayClientMessage(Component.literal("§cNo target in crosshair!"), true);
                } else {
                    mc.player.displayClientMessage(Component.literal("§aLocked on: " + target.getDisplayName().getString()), true);
                }
            } else {
                target = null;
                mc.player.displayClientMessage(Component.literal("§7Lock-on: Disabled"), true);
            }
        }

        if (isLockedOn && target != null) {
            if (!target.isAlive() || mc.player.distanceTo(target) > 20.0f || !mc.player.hasLineOfSight(target)) {
                target = null;
                isLockedOn = false;
                mc.player.displayClientMessage(Component.literal("§7Lock-on lost"), true);
            }
        }
    }

    private static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (isLockedOn && target != null && mc.player != null) {
            float partialTicks = (float) event.getPartialTick();
            Vec3 playerPos = mc.player.getEyePosition(partialTicks);

            // Focus on the middle of the entity
            Vec3 targetPos = target.getBoundingBox().getCenter();

            double diffX = targetPos.x - playerPos.x;
            double diffY = targetPos.y - playerPos.y;
            double diffZ = targetPos.z - playerPos.z;
            double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

            float targetYaw = (float) (Math.toDegrees(Math.atan2(-diffX, diffZ)));
            float targetPitch = (float) (-Math.toDegrees(Math.atan2(diffY, diffXZ)));

            float newYaw = lerpAngle(mc.player.getYRot(), targetYaw, 0.4f);
            float newPitch = lerpAngle(mc.player.getXRot(), targetPitch, 0.4f);

            mc.player.setYRot(newYaw);
            mc.player.setXRot(newPitch);

            event.setYaw(newYaw);
            event.setPitch(newPitch);
        }
    }

    private static LivingEntity findTarget(Minecraft mc) {
        Entity camera = mc.getCameraEntity();
        if (camera == null) return null;

        double distance = 20.0D;
        Vec3 eyePos = camera.getEyePosition(1.0F);
        Vec3 viewVec = camera.getViewVector(1.0F);
        Vec3 reachVec = eyePos.add(viewVec.scale(distance));
        AABB searchBox = camera.getBoundingBox().expandTowards(viewVec.scale(distance)).inflate(1.0D);

        // ProjectileUtil is the standard way to raytrace for entities in modern MC
        EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(
                camera,
                eyePos,
                reachVec,
                searchBox,
                entity -> entity instanceof LivingEntity && entity.isAlive() && !entity.isSpectator(),
                distance * distance
        );

        if (hitResult != null && hitResult.getEntity() instanceof LivingEntity living) {
            return living;
        }
        return null;
    }

    private static float lerpAngle(float start, float end, float pct) {
        float d = end - start;
        while (d < -180.0F) d += 360.0F;
        while (d >= 180.0F) d -= 360.0F;
        return start + d * pct;
    }
}