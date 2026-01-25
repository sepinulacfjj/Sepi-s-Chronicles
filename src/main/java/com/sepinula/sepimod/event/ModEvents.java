package com.sepinula.sepimod.event;

import com.sepinula.sepimod.SepiMod;
import com.sepinula.sepimod.entity.Baby_GoblinEntity;
import com.sepinula.sepimod.init.ModEntities;
import com.sepinula.sepimod.util.ModDataAttachments;
import com.sepinula.sepimod.util.PlayerStats;
import com.sepinula.sepimod.util.StatLogicHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = SepiMod.MODID)
public class ModEvents {

    private static final ResourceLocation AGILITY_SPEED_ID = ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "agility_speed_bonus");

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
            StatLogicHandler.applyStatModifiers(player, stats);
            ModDataAttachments.sync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
            StatLogicHandler.applyStatModifiers(player, stats);
            ModDataAttachments.sync(player);
        }
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.BabyGOBLIN.get(), Baby_GoblinEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void onPlayerTakeDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
            int dex = stats.getDexterity();

            if (player.isBlocking()) {
                stats.subStamina(4.0f);
            } else {
                stats.subStamina(1.5f);
            }

            if (dex > 0) {
                float dodgeCost = stats.getMaxStamina() * 0.10f;
                if (stats.getCurrentStamina() >= dodgeCost) {
                    double dodgeChance = dex * 0.005;
                    if (player.getRandom().nextDouble() < dodgeChance) {
                        event.setCanceled(true);
                        player.displayClientMessage(Component.literal("§b* Dodged! *"), true);
                        stats.subStamina(dodgeCost);
                        ModDataAttachments.sync(player);
                        return;
                    }
                }

                float reduction = Math.min(0.8f, dex * 0.01f);
                float newDamage = event.getAmount() * (1.0f - reduction);
                event.setAmount(newDamage);
            }
            ModDataAttachments.sync(player);
        }

        if (event.getSource().getEntity() instanceof ServerPlayer attacker) {
            PlayerStats attackerStats = attacker.getData(ModDataAttachments.PLAYER_STATS);
            attackerStats.subStamina(1.0f);
            ModDataAttachments.sync(attacker);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && !player.level().isClientSide) {
            PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
            int con = stats.getConstitution();
            int agi = stats.getAgility();

            // --- BUFFED STAMINA REGEN ---
            // Formula: Base 0.1 + (0.05 per point of Constitution)
            // At 100 Con, this is 5.1 per tick (approx 102 stamina per second)
            float staminaRegen = 0.1f + (con * 0.05f);
            boolean needsSync = false;

            // --- STAMINA LOGIC ---
            if (player.isSprinting() && agi > 0) {
                stats.subStamina(0.55f);
                needsSync = true;
            } else if (player.isBlocking()) {
                stats.subStamina(0.05f);
                needsSync = true;
            } else if (stats.getCurrentStamina() < stats.getMaxStamina()) {
                stats.addStamina(staminaRegen);
                // Sync more often during regen so the bar looks smooth
                if (player.tickCount % 2 == 0) needsSync = true;
            }

            // --- SPEED LOGIC ---
            AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttr != null) {
                speedAttr.removeModifier(AGILITY_SPEED_ID);
                if (player.isSprinting() && stats.getCurrentStamina() > 0 && agi > 0) {
                    double agilityBonus = agi * 0.0002;
                    agilityBonus = Math.min(agilityBonus, 0.04);
                    speedAttr.addTransientModifier(new AttributeModifier(AGILITY_SPEED_ID, agilityBonus, AttributeModifier.Operation.ADD_VALUE));
                }
            }

            // --- REGENERATION LOOP ---
            if (player.tickCount % 20 == 0) {
                float currentMana = stats.getCurrentMana();
                float maxMana = stats.getMaxMana();
                if (currentMana < maxMana) {
                    stats.setCurrentMana(currentMana + (1.0f + (maxMana * 0.05f)));
                }

                if (player.getHealth() < player.getMaxHealth() && con > 0) {
                    player.heal(con * 0.1f);
                }

                ModDataAttachments.sync(player);
            } else if (needsSync) {
                ModDataAttachments.sync(player);
            }

            if (player.tickCount % 2400 == 0) {
                stats.addTrainingPoints(1);
                checkAndNotify(player, stats);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickItem event) {
        if (event.getItemStack().has(DataComponents.FOOD)) {
            event.getEntity().startUsingItem(event.getHand());
        }
    }

    @SubscribeEvent
    public static void onFinishEating(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (event.getItem().has(DataComponents.FOOD)) {
                PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
                float bonus = stats.getConstitution() * 0.5f;
                player.heal(2.0f + bonus);
                stats.addMana(15.0f + bonus);
                stats.addStamina(30.0f + bonus);
                ModDataAttachments.sync(player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
            if (stats.getAgility() > 0 && stats.getCurrentStamina() > 0) {
                stats.subStamina(3.5f);
                ModDataAttachments.sync(player);
            }
            stats.addTrainingPoints(1);
            checkAndNotify(player, stats);
        }
    }

    @SubscribeEvent
    public static void onVillagerInteract(PlayerInteractEvent.EntityInteract event) {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof ServerPlayer player) {
            if (event.getTarget() instanceof Villager villager) {
                PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
                int charisma = stats.getCharisma();

                if (charisma > 0) {
                    float discountFactor = Math.max(0.1F, 1.0F - (charisma * 0.01F));
                    for (MerchantOffer offer : villager.getOffers()) {
                        int baseCost = offer.getBaseCostA().getCount();
                        int discountedCount = Math.max(1, Math.round(baseCost * discountFactor));
                        offer.setSpecialPriceDiff(discountedCount - baseCost);
                    }
                }
            }
        }
    }

    private static void checkAndNotify(ServerPlayer player, PlayerStats stats) {
        ModDataAttachments.sync(player);
        if (stats.getTrainingPoints() >= 100) {
            player.displayClientMessage(
                    Component.literal("§6[SepiMod] §fTraining complete! Check your §eStat Menu§f."),
                    true
            );
        }
    }
}