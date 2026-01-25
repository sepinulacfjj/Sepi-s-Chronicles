package com.sepinula.sepimod.event;

import com.sepinula.sepimod.SepiMod;
import com.sepinula.sepimod.entity.Baby_GoblinEntity;
import com.sepinula.sepimod.init.ModEntities;
import com.sepinula.sepimod.util.ModDataAttachments;
import com.sepinula.sepimod.util.PlayerStats;
import com.sepinula.sepimod.util.StatLogicHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = SepiMod.MODID)
public class ModEvents {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
            StatLogicHandler.applyStatModifiers(player, stats);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
            StatLogicHandler.applyStatModifiers(player, stats);
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

            if (dex > 0) {
                // Dodge logic
                double dodgeChance = dex * 0.005;
                if (player.getRandom().nextDouble() < dodgeChance) {
                    event.setCanceled(true);
                    player.displayClientMessage(Component.literal("§b* Dodged! *"), true);
                    return;
                }

                // Invisible damage reduction
                float reduction = Math.min(0.8f, dex * 0.01f);
                float newDamage = event.getAmount() * (1.0f - reduction);
                event.setAmount(newDamage);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && !player.level().isClientSide) {
            PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);

            // --- MANA REGENERATION LOGIC ---
            // Triggers every 20 ticks (1 second)
            if (player.tickCount % 20 == 0) {
                float current = stats.getCurrentMana();
                float max = stats.getMaxMana();

                if (current < max) {
                    // Regenerate 1 + 5% of total Max Mana per second
                    float regenAmount = 1.0f + (max * 0.05f);
                    stats.setCurrentMana(current + regenAmount);

                    // Always sync to the client so the HUD bar updates
                    ModDataAttachments.sync(player);
                }
            }

            // Training logic
            if (player.tickCount % 2400 == 0) {
                stats.addTrainingPoints(1);
                checkAndNotify(player, stats);
            }

            if (player.isSprinting() && player.tickCount % 100 == 0) {
                stats.addTrainingPoints(2);
                checkAndNotify(player, stats);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
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