package com.sepinula.sepimod.event;

import com.sepinula.sepimod.SepiMod;
import com.sepinula.sepimod.entity.Baby_GoblinEntity;
import com.sepinula.sepimod.init.ModEntities;
import com.sepinula.sepimod.init.ModItems;
import com.sepinula.sepimod.util.ModDataAttachments;
import com.sepinula.sepimod.util.PlayerStats;
import com.sepinula.sepimod.util.PlayerSpellCooldownData;
import com.sepinula.sepimod.network.PacketSyncSpellData;
import com.sepinula.sepimod.network.PacketSyncSpellbookData;
import com.sepinula.sepimod.spells.SpellRegistry;
import com.sepinula.sepimod.util.StatLogicHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = SepiMod.MODID)
public class ModEvents {

    private static final ResourceLocation AGILITY_SPRINT_ID = ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "agility_sprint_bonus");

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
            StatLogicHandler.applyStatModifiers(player, stats);
            ModDataAttachments.sync(player);
            var spells = player.getData(ModDataAttachments.PLAYER_SPELL_DATA);
            spells.learnSpell(SpellRegistry.FIREBALL.id().toString());
            spells.learnSpell(SpellRegistry.GUST.id().toString());
            spells.learnSpell(SpellRegistry.ICE_SHARD.id().toString());
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, PacketSyncSpellData.from(spells));
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, PacketSyncSpellbookData.from(player.getData(ModDataAttachments.PLAYER_SPELLBOOK_DATA)));
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
            StatLogicHandler.applyStatModifiers(player, stats);
            player.setHealth(player.getMaxHealth());
            stats.setCurrentStamina(stats.getMaxStamina());
            stats.setCurrentMana(stats.getMaxMana());
            int totalStats = stats.getStrengthRaw() + stats.getAgilityRaw() + stats.getConstitutionRaw() +
                    stats.getMagicResistanceRaw() + stats.getMagicPowerRaw() + stats.getManaRaw() +
                    stats.getDefenseRaw() + stats.getMindRaw() + stats.getAvailablePoints();
            stats.setTrainingPoints(Math.min(800, totalStats));
            ModDataAttachments.sync(player);
            var spells = player.getData(ModDataAttachments.PLAYER_SPELL_DATA);
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, PacketSyncSpellData.from(spells));
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, PacketSyncSpellbookData.from(player.getData(ModDataAttachments.PLAYER_SPELLBOOK_DATA)));
        }
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.BabyGOBLIN.get(), Baby_GoblinEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void onMagicResistance(MobEffectEvent.Applicable event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Magic Resistance protects against harmful Minecraft status effects.
        if (event.getEffectInstance().getEffect().value().getCategory() != net.minecraft.world.effect.MobEffectCategory.HARMFUL) return;

        PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
        double resistChance = Math.min(0.75D, stats.getMagicResistance() * 0.0075D);

        if (player.getRandom().nextDouble() < resistChance) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            player.displayClientMessage(Component.literal("§b✦ Resisted!"), true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTakeDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);

            boolean magicDamage = event.getSource().is(DamageTypes.MAGIC)
                    || event.getSource().is(DamageTypes.INDIRECT_MAGIC)
                    || event.getSource().is(DamageTypes.WITHER)
                    || event.getSource().is(Tags.DamageTypes.IS_POISON);

            if (magicDamage) {
                event.setAmount(event.getAmount() * StatLogicHandler.getMagicDamageMultiplier(stats));
                ModDataAttachments.sync(player);
                return;
            }

            boolean unavoidable = event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD)
                    || event.getSource().is(DamageTypes.GENERIC_KILL)
                    || event.getSource().is(DamageTypes.IN_FIRE)
                    || event.getSource().is(DamageTypes.ON_FIRE)
                    || event.getSource().is(DamageTypes.LAVA)
                    || event.getSource().is(DamageTypes.HOT_FLOOR);

            if (!unavoidable && !event.getSource().is(DamageTypes.FALL)) {
                double dodgeChance = Math.min(0.25D, stats.getAgility() * 0.0025D);
                if (player.getRandom().nextDouble() < dodgeChance) {
                    event.setCanceled(true);
                    player.displayClientMessage(Component.literal("§b* Dodged! *"), true);
                    return;
                }
            }

            float reduction = Math.min(0.8F, stats.getDefense() * 0.01F);
            event.setAmount(event.getAmount() * (1.0F - reduction));
            ModDataAttachments.sync(player);
        }
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
            PlayerSpellCooldownData cooldowns = player.getData(ModDataAttachments.PLAYER_SPELL_COOLDOWNS);
            cooldowns.tick();

            PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
            int con = stats.getConstitution();
            int agi = stats.getAgility();
            int mna = stats.getMana();
            int foodLevel = player.getFoodData().getFoodLevel();
            boolean hasHungerEffect = player.hasEffect(MobEffects.HUNGER);
            boolean needsSync = false;

            int hasteLevel = stats.getMiningHasteLevel();
            if (hasteLevel >= 0 && player.swinging) {
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 5, hasteLevel, false, false, false));
            }

            if (foodLevel <= 0) {
                float drainRate = hasHungerEffect ? 0.5f : 0.25f;
                stats.subStamina(drainRate);
                needsSync = true;
            }

            AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (player.isSprinting()) {
                // TO REMOVE (Old logic): if (stats.getCurrentStamina() <= 0.1f) {

                // New logic: Blocks sprinting if stamina is empty OR player has Hunger effect
                if (stats.getCurrentStamina() <= 0.1f || hasHungerEffect) {
                    player.setSprinting(false);
                    if (speedAttr != null && speedAttr.hasModifier(AGILITY_SPRINT_ID)) {
                        speedAttr.removeModifier(AGILITY_SPRINT_ID);
                    }
                    needsSync = true;
                } else {
                    if (speedAttr != null && !speedAttr.hasModifier(AGILITY_SPRINT_ID) && agi > 0) {
                        speedAttr.addTransientModifier(new AttributeModifier(AGILITY_SPRINT_ID,
                                agi * 0.01D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                    }
                    stats.subStamina(0.65f);
                    needsSync = true;
                }
            } else {
                if (speedAttr != null && speedAttr.hasModifier(AGILITY_SPRINT_ID)) {
                    speedAttr.removeModifier(AGILITY_SPRINT_ID);
                }
                stats.tickStaminaRegen(player);
            }

            if (stats.getTrainingPoints() >= 800) {
                if (stats.getTotalXpGained() > 0) {
                    stats.resetProgressAfterCap();
                    needsSync = true;
                }
            } else if (stats.getTotalXpGained() >= stats.getXpNeededForNextPoint()) {
                stats.setTrainingPoints(stats.getTrainingPoints() + 1);
                stats.setAvailablePoints(stats.getAvailablePoints() + 1);
                if (stats.getTrainingPoints() == 800)
                    player.displayClientMessage(Component.literal("§6§lMAX LEVEL REACHED (800)!"), false);
                else player.displayClientMessage(Component.literal("§6§l+1 Training Point!"), false);
                stats.resetProgressAfterCap();
                needsSync = true;
            }

            if (player.tickCount % 20 == 0) {
                if (player.getHealth() < player.getMaxHealth() && con > 0) player.heal(con * 0.05f);

                if (stats.getCurrentMana() < stats.getMaxMana()) {
                    float manaRegen = 1.0f + (mna * 0.1f);
                    stats.addMana(manaRegen);
                }

                ModDataAttachments.sync(player);
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, PacketSyncSpellData.from(player.getData(ModDataAttachments.PLAYER_SPELL_DATA)));
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, PacketSyncSpellbookData.from(player.getData(ModDataAttachments.PLAYER_SPELLBOOK_DATA)));
            } else if (needsSync) {
                ModDataAttachments.sync(player);
            }
        }
    }

    @SubscribeEvent
    public static void onXpPickup(PlayerXpEvent.PickupXp event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
            int xpValue = event.getOrb().getValue();
            if (stats.getMind() >= 50) {
                xpValue *= 2;
            }
            if (stats.getTrainingPoints() < 800) {
                stats.addXp(xpValue);
                ModDataAttachments.sync(player);
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
                float staminaRestore = 20.0f, healthRestore = 1.0f, manaRestore = 10.0f;
                if (item.is(Items.COOKED_PORKCHOP) || item.is(Items.COOKED_BEEF)) {
                    staminaRestore = 60.0f;
                    healthRestore = 3.0f;
                } else if (item.is(Items.DRIED_KELP) || item.is(Items.COOKIE)) {
                    staminaRestore = 5.0f;
                    healthRestore = 0.5f;
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

        ItemStack stack = event.getItemStack();
        if (stack.is(ModItems.BASIC_STAFF.get())) {
            if (event.getEntity() instanceof ServerPlayer player) {
                PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
                float fireballCost = 15.0f;

                if (stats.getCurrentMana() >= fireballCost) {
                    stats.subMana(fireballCost);
                    Vec3 look = player.getLookAngle();
                    int explosionPower = 1 + (stats.getMagicPower() / 100);
                    LargeFireball fireball = new LargeFireball(player.level(), player, look, explosionPower);
                    fireball.setPos(player.getX(), player.getEyeY(), player.getZ());
                    player.level().addFreshEntity(fireball);
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
                    player.getCooldowns().addCooldown(stack.getItem(), 20);

                    ModDataAttachments.sync(player);
                } else {
                    player.displayClientMessage(Component.literal("§cNot enough Mana!"), true);
                }
            }
        }
    }
}