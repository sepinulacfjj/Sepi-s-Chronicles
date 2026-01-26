package com.sepinula.sepimod.event;

import com.sepinula.sepimod.SepiMod;
import com.sepinula.sepimod.entity.Baby_GoblinEntity;
import com.sepinula.sepimod.init.ModEntities;
import com.sepinula.sepimod.util.ModDataAttachments;
import com.sepinula.sepimod.util.PlayerStats;
import com.sepinula.sepimod.util.StatLogicHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = SepiMod.MODID)
public class ModEvents {

    private static final ResourceLocation AGILITY_SPRINT_ID = ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "agility_sprint_bonus");
    private static int speedCooldown = 2;

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

            // FULL RECOVERY ON RESPAWN
            player.setHealth(player.getMaxHealth());
            stats.setCurrentStamina(stats.getMaxStamina());
            stats.setCurrentMana(stats.getMaxMana());

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

            // --- 1. FATAL DAMAGE CHECK ---
            // Void, /kill, and basic magic/poison cannot be dodged or mitigated
            if (event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD) ||
                    event.getSource().is(DamageTypes.GENERIC_KILL) ||
                    event.getSource().is(DamageTypes.MAGIC) ||
                    event.getSource().is(DamageTypes.INDIRECT_MAGIC)) {
                return;
            }

            // --- 2. DODGE SYSTEM (Agility) ---
            // Max 25% dodge chance at 100 Agility. Doesn't work on fall damage.
            if (!event.getSource().is(DamageTypes.FALL)) {
                double dodgeChance = Math.min(0.25, stats.getAgility() * 0.0025);
                if (player.getRandom().nextDouble() < dodgeChance) {
                    event.setCanceled(true);
                    player.displayClientMessage(Component.literal("§b* Dodged! *"), true);
                    return;
                }
            }

            // --- 3. DEFENSE REDUCTION (DEF) ---
            // Reduction 1% per level, max 80%
            float reduction = Math.min(0.8f, stats.getDefense() * 0.01f);
            event.setAmount(event.getAmount() * (1.0f - reduction));
            ModDataAttachments.sync(player);
        }

        // --- 4. ATTACKER STRENGTH BONUS ---
        if (event.getSource().getEntity() instanceof ServerPlayer attacker) {
            PlayerStats attackerStats = attacker.getData(ModDataAttachments.PLAYER_STATS);
            int str = attackerStats.getStrength();
            float attackCost = 3.0f * (1.0f + (str * 0.08f));

            if (attackerStats.getCurrentStamina() >= attackCost) {
                float strengthBonus = str * 0.5f;
                event.setAmount(event.getAmount() + strengthBonus);
                attackerStats.subStamina(attackCost);
            }
            ModDataAttachments.sync(attacker);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && !player.level().isClientSide) {
            PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
            int con = stats.getConstitution();
            int agi = stats.getAgility();
            int str = stats.getStrength();

            boolean needsSync = false;

            // --- 1. MINING SPEED (Strength) ---
            if (str >= 50) {
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 2, 1, false, false, false));
            }

            // --- 2. STAMINA REGEN & HUNGER ---
            boolean hasHungerEffect = player.hasEffect(MobEffects.HUNGER);
            int hungerLevel = player.getFoodData().getFoodLevel();

            if (hungerLevel <= 0) {
                stats.subStamina(0.2f); // Drain stamina when starving
                needsSync = true;
            }

            if (player.isSprinting()) {
                if (stats.getCurrentStamina() <= 0.1f) {
                    player.setSprinting(false);
                    speedCooldown = 20;
                    needsSync = true;
                } else {
                    AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
                    if (speedAttr != null && !speedAttr.hasModifier(AGILITY_SPRINT_ID) && agi > 0) {
                        speedAttr.addTransientModifier(new AttributeModifier(AGILITY_SPRINT_ID,
                                agi * 0.01D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                    }
                    stats.subStamina(0.65f);
                    needsSync = true;
                }
            } else {
                // Passive regeneration: Higher CON = faster regen
                // Regeneration is disabled if player has Hunger effect
                if (!player.isBlocking() && !hasHungerEffect && stats.getCurrentStamina() < stats.getMaxStamina()) {
                    float regenRate = 0.03f + (con * 0.005f);
                    stats.addStamina(regenRate);
                    if (player.tickCount % 5 == 0) needsSync = true;
                }
            }

            // --- 3. TRAINING MILESTONES (XP Tracking) ---
            if (stats.getTotalXpGained() >= stats.getXpNeededForNextPoint()) {
                stats.setTrainingPoints(stats.getTrainingPoints() + 1);
                stats.setAvailablePoints(stats.getAvailablePoints() + 1);
                player.displayClientMessage(Component.literal("§6§l+1 Training Point!"), false);
                needsSync = true;
            }

            if (player.tickCount % 20 == 0) {
                if (player.getHealth() < player.getMaxHealth() && con > 0) {
                    player.heal(con * 0.05f);
                }
                ModDataAttachments.sync(player);
            } else if (needsSync) {
                ModDataAttachments.sync(player);
            }
        }
    }

    @SubscribeEvent
    public static void onXpPickup(PlayerXpEvent.PickupXp event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
            stats.addXp(event.getOrb().getValue()); // Add raw experience to our RPG tracking
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
            ItemStack item = event.getItem();
            if (item.has(DataComponents.FOOD)) {
                PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
                float conBonus = stats.getConstitution() * 0.2f;

                float staminaRestore = 20.0f;
                float healthRestore = 1.0f;
                float manaRestore = 10.0f;

                // --- EDIBLE SPECIFIC REGEN ---
                if (item.is(Items.COOKED_PORKCHOP) || item.is(Items.COOKED_BEEF)) {
                    staminaRestore = 60.0f;
                    healthRestore = 3.0f;
                } else if (item.is(Items.DRIED_KELP) || item.is(Items.COOKIE)) {
                    staminaRestore = 5.0f;
                    healthRestore = 0.5f;
                } else if (item.is(Items.PORKCHOP) || item.is(Items.BEEF)) {
                    staminaRestore = 25.0f;
                    healthRestore = 1.5f;
                }

                player.heal(healthRestore + (conBonus * 0.1f));
                stats.addMana(manaRestore + conBonus);
                stats.addStamina(staminaRestore + (conBonus * 2.0f));
                ModDataAttachments.sync(player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerUseItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getItemStack().has(DataComponents.FOOD)) {
            event.getEntity().startUsingItem(event.getHand());
        }
    }
}