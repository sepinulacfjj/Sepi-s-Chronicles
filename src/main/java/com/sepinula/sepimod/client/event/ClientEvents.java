package com.sepinula.sepimod.client.event;

import com.mojang.blaze3d.platform.InputConstants;
import com.sepinula.sepimod.SepiMod;
import com.sepinula.sepimod.client.gui.ClassSelectionScreen;
import com.sepinula.sepimod.client.gui.SpellbookScreen;
import com.sepinula.sepimod.client.gui.StatUpgradeScreen;
import com.sepinula.sepimod.client.model.Baby_GoblinModel;
import com.sepinula.sepimod.client.renderer.Baby_GoblinRenderer;
import com.sepinula.sepimod.init.ModEntities;
import com.sepinula.sepimod.init.ModMenus;
import com.sepinula.sepimod.init.ModModelLayers;
import com.sepinula.sepimod.network.Messages;
import com.sepinula.sepimod.network.PacketSpellbookAction;
import com.sepinula.sepimod.spellbook.SpellbookHelper;
import com.sepinula.sepimod.spellbook.SpellbookItem;
import com.sepinula.sepimod.util.ModDataAttachments;
import com.sepinula.sepimod.util.PlayerStats;
import com.sepinula.sepimod.util.PlayerSpellCooldownData;
import com.sepinula.sepimod.util.RpgArchetype;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

public class ClientEvents {
    private static boolean vanillaCreativeHotbarKeysCleared = false;
    public static final KeyMapping classKey = new KeyMapping("key.sepimod.open_class", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, "key.categories.sepimod");
    public static final KeyMapping statsKey = new KeyMapping("key.sepimod.open_stats", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, "key.categories.sepimod");
    public static final KeyMapping spellbookKey = new KeyMapping("key.sepimod.open_spellbook", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, "key.categories.sepimod");
    public static final KeyMapping lockOnKey = new KeyMapping("key.sepimod.lock_on", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.categories.sepimod");
    public static final KeyMapping spellPreviousKey = new KeyMapping("key.sepimod.spell_previous", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.categories.sepimod");
    public static final KeyMapping spellNextKey = new KeyMapping("key.sepimod.spell_next", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, "key.categories.sepimod");
    public static final KeyMapping spellCastKey = new KeyMapping("key.sepimod.spell_cast", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.sepimod");

    private static boolean isLockedOn = false;
    private static LivingEntity target = null;

    private static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (isLockedOn && target != null && mc.player != null) {
            float partialTicks = (float) event.getPartialTick();
            Vec3 playerPos = mc.player.getEyePosition(partialTicks);
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
        EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(camera, eyePos, reachVec, searchBox,
                entity -> entity instanceof LivingEntity && entity.isAlive() && !entity.isSpectator(), distance * distance);
        if (hitResult != null && hitResult.getEntity() instanceof LivingEntity living) return living;
        return null;
    }

    private static float lerpAngle(float start, float end, float pct) {
        float d = end - start;
        while (d < -180.0F) d += 360.0F;
        while (d >= 180.0F) d -= 360.0F;
        return start + d * pct;
    }
}