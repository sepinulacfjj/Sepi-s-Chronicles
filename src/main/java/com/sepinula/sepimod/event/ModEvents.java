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
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = SepiMod.MODID) // Tells NeoForge to listen for events in this class
public class ModEvents {

    // Unique ID for the speed buff so it doesn't conflict with other mods
    private static final ResourceLocation AGILITY_SPRINT_ID = ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "agility_sprint_bonus");
    // Cooldown used to keep the FOV "zoomed in" for a split second after stopping a sprint
    private static int speedCooldown = 2;

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS); // Fetch player's RPG stats
            StatLogicHandler.applyStatModifiers(player, stats); // Apply permanent buffs like Max Health
            ModDataAttachments.sync(player); // Send stat data from server to client (for the UI)
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
            StatLogicHandler.applyStatModifiers(player, stats); // Re-apply buffs after death
            ModDataAttachments.sync(player);
        }
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        // Essential for custom mobs: Gives Baby Goblin its base health/speed/attack
        event.put(ModEntities.BabyGOBLIN.get(), Baby_GoblinEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        // Arm swinging and vanilla hits are always allowed
    }

    @SubscribeEvent
    public static void onPlayerTakeDamage(LivingIncomingDamageEvent event) {
        // --- 1. DEFENDER LOGIC (When a player is being hit) ---
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
            int dex = stats.getDexterity();
            int str = stats.getStrength();

            // Heavier strength makes stamina drain faster when taking hits (simulates bulkiness)
            float weightPenalty = 1.0f + (str * 0.015f);

            if (player.isBlocking()) {
                stats.subStamina(2.5f * weightPenalty); // Shield block stamina cost
            } else {
                stats.subStamina(1.5f * weightPenalty); // Getting hit flat stamina cost
            }

            // Dexterity-based Dodge system
            if (dex > 0) {
                float dodgeCost = stats.getMaxStamina() * 0.10f; // Dodging costs 10% max stamina
                if (stats.getCurrentStamina() >= dodgeCost) {
                    double dodgeChance = dex * 0.004; // 0.4% dodge chance per level of Dex
                    if (player.getRandom().nextDouble() < dodgeChance) {
                        event.setCanceled(true); // Stop the damage entirely
                        player.displayClientMessage(Component.literal("§b* Dodged! *"), true);
                        stats.subStamina(dodgeCost);
                        ModDataAttachments.sync(player);
                        return;
                    }
                }
                // If player doesn't dodge, Dex still provides minor damage reduction (1% per level, max 80%)
                float reduction = Math.min(0.8f, dex * 0.01f);
                event.setAmount(event.getAmount() * (1.0f - reduction));
            }
            ModDataAttachments.sync(player);
        }

        // --- 2. ATTACKER LOGIC (When a player hits something else) ---
        if (event.getSource().getEntity() instanceof ServerPlayer attacker) {
            PlayerStats attackerStats = attacker.getData(ModDataAttachments.PLAYER_STATS);
            int str = attackerStats.getStrength();

            // Scaling cost: Higher Strength = massive damage but massive stamina use
            float attackCost = 3.0f * (1.0f + (str * 0.08f));

            // Only apply Strength bonus if the player has enough stamina for a 'Heavy Hit'
            if (attackerStats.getCurrentStamina() >= attackCost) {
                float strengthBonus = str * 0.5f; // +0.5 damage per Strength level
                event.setAmount(event.getAmount() + strengthBonus);
                attackerStats.subStamina(attackCost);
            }
            // If they are out of stamina, they just deal Vanilla damage (no bonus added)

            ModDataAttachments.sync(attacker);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        // Only run logic on the server-side every "tick" (20 times per second)
        if (event.getEntity() instanceof ServerPlayer player && !player.level().isClientSide) {
            PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
            int con = stats.getConstitution();
            int agi = stats.getAgility();

            float staminaRegen = 0.03f + (con * 0.005f); // Constitution boosts stamina recovery speed
            boolean needsSync = false;

            AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttr != null) {
                if (player.isSprinting()) {
                    if (stats.getCurrentStamina() <= 0.1f) {
                        player.setSprinting(false); // Force stop if exhausted
                        speedCooldown = 20; // Start FOV delay timer
                        needsSync = true;
                    } else {
                        // Apply Agility speed bonus during sprint only
                        if (!speedAttr.hasModifier(AGILITY_SPRINT_ID) && agi > 0) {
                            speedAttr.addTransientModifier(new AttributeModifier(AGILITY_SPRINT_ID,
                                    agi * 0.01D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        }
                        stats.subStamina(0.65f); // Constant stamina drain while sprinting
                        needsSync = true;
                    }
                } else {
                    // FOV Delay: Keeps the speed modifier for 1 second after sprint stops for smoothness
                    if (speedCooldown > 0) {
                        speedCooldown--;
                    } else if (speedAttr.hasModifier(AGILITY_SPRINT_ID)) {
                        speedAttr.removeModifier(AGILITY_SPRINT_ID);
                        needsSync = true;
                    }

                    // Regenerate stamina if not blocking and not sprinting
                    if (!player.isBlocking() && stats.getCurrentStamina() < stats.getMaxStamina()) {
                        stats.addStamina(staminaRegen);
                        if (player.tickCount % 5 == 0) needsSync = true;
                    }
                }
            }

            // Passive stamina drain for holding up a shield
            if (player.isBlocking()) {
                stats.subStamina(0.15f);
                needsSync = true;
            }

            // Passive Health Regen based on Constitution (Happens every 1 second)
            if (player.tickCount % 20 == 0) {
                if (player.getHealth() < player.getMaxHealth() && con > 0) {
                    player.heal(con * 0.05f);
                }
                ModDataAttachments.sync(player);
            } else if (needsSync) {
                ModDataAttachments.sync(player);
            }

            // Every 2 minutes (2400 ticks), try to award a Training Point
            if (player.tickCount % 2400 == 0) {
                applyXpStyleProgress(player, stats);
            }
        }
    }

    private static void applyXpStyleProgress(ServerPlayer player, PlayerStats stats) {
        // Sum of all stats to determine "Total Level"
        int totalLevel = stats.getStrength() + stats.getAgility() + stats.getConstitution() +
                stats.getDexterity() + stats.getWillpower() + stats.getMind() +
                stats.getMana() + stats.getCharisma();

        // Higher total level makes it harder to get the next free Training Point
        int difficultyThreshold = 1 + (totalLevel / 50);
        if (player.getRandom().nextInt(difficultyThreshold) == 0) {
            stats.addTrainingPoints(1);
            checkAndNotify(player, stats);
        }
    }

    @SubscribeEvent
    public static void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
            // Jumping cost scales with Strength (Heavier legs)
            float jumpCost = 4.0f * (1.0f + (stats.getStrength() * 0.02f));
            stats.subStamina(jumpCost);
            applyXpStyleProgress(player, stats); // Jumping can occasionally grant training progress
            ModDataAttachments.sync(player);
        }
    }

    @SubscribeEvent
    public static void onVillagerInteract(PlayerInteractEvent.EntityInteract event) {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof ServerPlayer player) {
            if (event.getTarget() instanceof Villager villager) {
                PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
                int charisma = stats.getCharisma();
                if (charisma > 0) {
                    // Charisma provides a discount on all villager trades (max 90% off)
                    float discountFactor = Math.max(0.1F, 1.0F - (charisma * 0.01F));
                    for (MerchantOffer offer : villager.getOffers()) {
                        int baseCost = offer.getBaseCostA().getCount();
                        offer.setSpecialPriceDiff(Math.max(1, Math.round(baseCost * discountFactor)) - baseCost);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onFinishEating(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (event.getItem().has(DataComponents.FOOD)) {
                PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
                float bonus = stats.getConstitution() * 0.5f; // Constitution makes food more effective
                player.heal(2.0f + bonus); // Restore Health
                stats.addMana(15.0f + bonus); // Restore Mana
                stats.addStamina(30.0f + bonus); // Restore Stamina
                ModDataAttachments.sync(player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerUseItem(PlayerInteractEvent.RightClickItem event) {
        // Allows the player to eat food even if their hunger bar is 100% full
        if (event.getItemStack().has(DataComponents.FOOD)) {
            event.getEntity().startUsingItem(event.getHand());
        }
    }

    private static void checkAndNotify(ServerPlayer player, PlayerStats stats) {
        ModDataAttachments.sync(player);
        if (stats.getTrainingPoints() >= 100) {
            player.displayClientMessage(Component.literal("§6[SepiMod] §fTraining complete!"), true);
        }
    }
}