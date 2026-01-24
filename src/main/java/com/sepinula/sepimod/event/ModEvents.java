package com.sepinula.sepimod.event;

import com.sepinula.sepimod.SepiMod;
import com.sepinula.sepimod.entity.Baby_GoblinEntity;
import com.sepinula.sepimod.init.ModEntities;
import com.sepinula.sepimod.util.ModDataAttachments;
import com.sepinula.sepimod.util.PlayerStats;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = SepiMod.MODID)
public class ModEvents {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        // This connects the Goblin Entity to the attributes we defined in its class
        event.put(ModEntities.BabyGOBLIN.get(), Baby_GoblinEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        // NeoForge 1.21.1 uses PlayerTickEvent.Post instead of TickEvent
        if (event.getEntity() instanceof ServerPlayer player && !player.level().isClientSide) {

            // Accessing data via the new Attachment system
            PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);

            // Passive training every 2 minutes (2400 ticks)
            if (player.tickCount % 2400 == 0) {
                stats.addTrainingPoints(1);
                checkAndNotify(player, stats);
            }

            // Training while sprinting
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

    // --- CHARISMA TRADING LOGIC ---
    @SubscribeEvent
    public static void onVillagerInteract(PlayerInteractEvent.EntityInteract event) {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof ServerPlayer player) {
            if (event.getTarget() instanceof Villager villager) {

                PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
                int charisma = stats.getCharisma();

                if (charisma > 0) {
                    // Calculate discount factor
                    float discountFactor = 1.0F - (charisma / 200.0F); // Adjusted for better scaling

                    for (MerchantOffer offer : villager.getOffers()) {
                        int baseCost = offer.getBaseCostA().getCount();
                        int discountedCount = Math.max(1, (int)(baseCost * discountFactor));

                        // In 1.21.1, we set the special price difference directly
                        offer.setSpecialPriceDiff(discountedCount - baseCost);
                    }
                }
            }
        }
    }

    private static void checkAndNotify(ServerPlayer player, PlayerStats stats) {
        // We sync the data to the client whenever training points change
        ModDataAttachments.sync(player);

        // Notify the player if they hit the threshold for a new Stat Point
        if (stats.getTrainingPoints() == 100 && stats.getAvailablePoints() > 0) {
            player.displayClientMessage(
                    Component.literal("§6[SepiMod] §fYou've earned a §eStat Point§f through training!"),
                    true
            );
        }
    }
}