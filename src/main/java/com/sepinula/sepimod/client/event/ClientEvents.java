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
import com.sepinula.sepimod.init.ModItems;
import com.sepinula.sepimod.init.ModModelLayers;
import com.sepinula.sepimod.network.Messages;
import com.sepinula.sepimod.network.PacketSpellbookAction;
import com.sepinula.sepimod.spellbook.SpellbookHelper;
import com.sepinula.sepimod.spellbook.SpellbookItem;
import com.sepinula.sepimod.spellbook.SpellbookTier;
import software.bernie.geckolib.animatable.GeoItem;
import com.sepinula.sepimod.util.ModDataAttachments;
import com.sepinula.sepimod.util.PlayerStats;
import com.sepinula.sepimod.util.PlayerSpellCooldownData;
import com.sepinula.sepimod.util.RpgArchetype;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
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

    // Client-only preview of the equipped spellbook for keybind activation.
    // The real spellbook remains in the dedicated attachment slot.
    private static InteractionHand previewHand = null;
    private static ItemStack previewOriginalStack = ItemStack.EMPTY;
    private static boolean previewGuiOpened = false;
    private static int previewRestoreTicks = -1;
    private static ItemStack previewStack = ItemStack.EMPTY;
    private static SpellbookItem previewItem;

    public static void init(IEventBus modBus) {
        modBus.addListener(ClientEvents::onKeyRegister);
        modBus.addListener(ClientEvents::registerRenderers);
        modBus.addListener(ClientEvents::registerLayers);
        modBus.addListener(ClientEvents::registerScreens);
        NeoForge.EVENT_BUS.addListener(ClientEvents::onClientTick);
        NeoForge.EVENT_BUS.addListener(ClientEvents::onComputeCameraAngles);
        NeoForge.EVENT_BUS.addListener(ClientEvents::onRenderPlayer);

    }

    private static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(classKey);
        event.register(statsKey);
        event.register(spellbookKey);
        event.register(lockOnKey);
        event.register(spellPreviousKey);
        event.register(spellNextKey);
        event.register(spellCastKey);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.SPELLBOOK.get(), SpellbookScreen::new);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BabyGOBLIN.get(), Baby_GoblinRenderer::new);
    }

    private static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.GOBLIN_LAYER, Baby_GoblinModel::createBodyLayer);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (!vanillaCreativeHotbarKeysCleared) {
            mc.options.keySaveHotbarActivator.setKey(InputConstants.UNKNOWN);
            mc.options.keyLoadHotbarActivator.setKey(InputConstants.UNKNOWN);
            mc.options.save();
            vanillaCreativeHotbarKeysCleared = true;
        }
        if (mc.player == null || mc.level == null) return;

        PlayerSpellCooldownData cooldowns = mc.player.getData(ModDataAttachments.PLAYER_SPELL_COOLDOWNS);
        cooldowns.tick();

        if (mc.screen == null) {
            while (classKey.consumeClick()) {
                PlayerStats stats = mc.player.getData(ModDataAttachments.PLAYER_STATS);
                if (stats.getArchetype() == RpgArchetype.NONE) mc.setScreen(new ClassSelectionScreen());
                else mc.player.displayClientMessage(Component.literal("§cYou already have a class selected!"), true);
            }

            while (spellbookKey.consumeClick()) {
                if (SpellbookHelper.hasSpellbook(mc.player)) {
                    if (startSpellbookKeybindPreview(mc)) {
                        Messages.sendToServer(new PacketSpellbookAction(PacketSpellbookAction.OPEN, 0, 0));
                    }
                } else {
                    mc.player.displayClientMessage(Component.literal("§cEquip a spellbook first."), true);
                }
            }

            while (statsKey.consumeClick()) {
                PlayerStats stats = mc.player.getData(ModDataAttachments.PLAYER_STATS);
                if (stats.getArchetype() == RpgArchetype.NONE) mc.player.displayClientMessage(Component.literal("§6You must pick a class (Press O) before upgrading stats!"), true);
                else mc.setScreen(new StatUpgradeScreen());
            }

            while (spellPreviousKey.consumeClick()) {
                if (SpellbookHelper.hasSpellbook(mc.player)) Messages.sendToServer(new PacketSpellbookAction(PacketSpellbookAction.PREVIOUS, 0, 0));
            }

            while (spellNextKey.consumeClick()) {
                if (SpellbookHelper.hasSpellbook(mc.player)) Messages.sendToServer(new PacketSpellbookAction(PacketSpellbookAction.NEXT, 0, 0));
            }

            while (spellCastKey.consumeClick()) {
                if (SpellbookHelper.hasSpellbook(mc.player)) Messages.sendToServer(new PacketSpellbookAction(PacketSpellbookAction.CAST, 0, 0));
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
        }

        if (previewHand != null) {
            if (mc.screen != null) {
                previewGuiOpened = true;
            } else if (previewGuiOpened && previewRestoreTicks < 0) {
                // SpellbookMenu.removed triggers the close animation while the
                // preview stack is still in the hand. Give that animation time
                // to finish before restoring the real hand contents.
                previewRestoreTicks = 11;
            }

            if (previewRestoreTicks >= 0) {
                if (previewRestoreTicks == 0) {
                    restoreSpellbookKeybindPreview(mc);
                } else {
                    previewRestoreTicks--;
                }
            }
        }

        if (isLockedOn && target != null && (!target.isAlive() || mc.player.distanceTo(target) > 20.0f || !mc.player.hasLineOfSight(target))) {
            target = null;
            isLockedOn = false;
            mc.player.displayClientMessage(Component.literal("§7Lock-on lost"), true);
        }
    }


    private static boolean startSpellbookKeybindPreview(Minecraft mc) {
        if (previewHand != null || mc.player == null || mc.level == null) {
            return false;
        }

        ItemStack equippedBook = SpellbookHelper.getSpellbook(mc.player);
        if (!(equippedBook.getItem() instanceof SpellbookItem spellbook)) {
            return false;
        }

        previewHand = InteractionHand.MAIN_HAND;
        previewOriginalStack = ItemStack.EMPTY;
        previewGuiOpened = false;
        previewRestoreTicks = -1;

        // This is a separately registered visual item. It is never placed
        // into the player's inventory, hotbar, or dedicated spellbook slot.
        previewItem = getPreviewItem(spellbook.getTier());
        previewStack = new ItemStack(previewItem);

        previewItem.triggerAnim(mc.player, GeoItem.getId(previewStack), "controller", "open");
        return true;
    }

    private static SpellbookItem getPreviewItem(SpellbookTier tier) {
        return switch (tier) {
            case ORIGINAL -> ModItems.SPELLBOOK_PREVIEW.get();
            case COPPER -> ModItems.COPPER_SPELLBOOK_PREVIEW.get();
            case IRON -> ModItems.IRON_SPELLBOOK_PREVIEW.get();
            case GOLD -> ModItems.GOLD_SPELLBOOK_PREVIEW.get();
            case DIAMOND -> ModItems.DIAMOND_SPELLBOOK_PREVIEW.get();
            case NETHERITE -> ModItems.NETHERITE_SPELLBOOK_PREVIEW.get();
        };
    }

    private static void restoreSpellbookKeybindPreview(Minecraft mc) {
        previewHand = null;
        previewOriginalStack = ItemStack.EMPTY;
        previewStack = ItemStack.EMPTY;
        previewItem = null;
        previewGuiOpened = false;
        previewRestoreTicks = -1;
    }

    public static ItemStack getSpellbookPreviewStack() {
        return previewStack;
    }

    public static void startSpellbookClosePreview() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || previewStack.isEmpty()) {
            return;
        }

        if (previewItem != null) {
            previewItem.triggerAnim(
                    mc.player,
                    GeoItem.getId(previewStack),
                    "controller",
                    "close"
            );
            previewRestoreTicks = 11;
        }
    }

    private static void onRenderPlayer(RenderPlayerEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        if (previewStack.isEmpty()
                || mc.player == null
                || event.getEntity() != mc.player
                || mc.options.getCameraType().isFirstPerson()) {
            return;
        }

        event.getPoseStack().pushPose();

        // Position exactly at the player's right hand. The item renderer then
        // applies the spellbook.json THIRD_PERSON_RIGHT_HAND transform.
        event.getRenderer().getModel().rightArm.translateAndRotate(event.getPoseStack());
        event.getPoseStack().translate(0.0F, -0.1F, 0.0F);

        mc.getItemRenderer().renderStatic(
                mc.player,
                previewStack,
                ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                false,
                event.getPoseStack(),
                event.getMultiBufferSource(),
                mc.level,
                event.getPackedLight(),
                OverlayTexture.NO_OVERLAY,
                0
        );

        event.getPoseStack().popPose();
    }

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