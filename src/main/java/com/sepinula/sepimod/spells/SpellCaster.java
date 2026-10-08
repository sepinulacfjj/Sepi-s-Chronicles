package com.sepinula.sepimod.spells;

import com.sepinula.sepimod.network.PacketSyncSpellCooldown;
import com.sepinula.sepimod.spellbook.SpellbookHelper;
import com.sepinula.sepimod.util.ModDataAttachments;
import com.sepinula.sepimod.util.PlayerSpellCooldownData;
import com.sepinula.sepimod.util.PlayerSpellData;
import com.sepinula.sepimod.util.PlayerStats;
import com.sepinula.sepimod.util.StatLogicHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public final class SpellCaster {
    private SpellCaster() {}

    public static boolean castSelected(ServerPlayer player) {
        PlayerSpellData spellData = player.getData(ModDataAttachments.PLAYER_SPELL_DATA);
        int capacity = Math.min(
                PlayerSpellData.MAX_SPELL_SLOTS,
                Math.max(0, SpellbookHelper.getCapacity(player))
        );

        if (capacity <= 0 || spellData.getSelectedSpellIndex() >= spellData.getActiveSpells().size()
                || spellData.getSelectedSpellIndex() >= capacity) {
            return false;
        }

        String spellId = spellData.getSelectedSpellId();
        if (spellId.isBlank()) return false;

        Spell spell = SpellRegistry.get(net.minecraft.resources.ResourceLocation.parse(spellId));
        if (spell == null) return false;

        PlayerSpellCooldownData cooldowns = player.getData(ModDataAttachments.PLAYER_SPELL_COOLDOWNS);
        if (cooldowns.isOnCooldown(spellId)) return false;

        PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
        float cost = spell.manaCost();

        if (stats.getCurrentMana() < cost) {
            player.displayClientMessage(Component.literal("§cNot enough Mana!"), true);
            return false;
        }

        int cooldownTicks = SpellCooldownHelper.getEffectiveCooldownTicks(player, spell);

        boolean cast = switch (spellId) {
            case "sepimod:fireball" -> castFireball(player, stats);
            case "sepimod:gust" -> castGust(player, stats);
            case "sepimod:ice_shard" -> castIceShard(player, stats);
            case "sepimod:fire_wind" -> castFireWind(player, stats);
            default -> false;
        };

        if (cast) {
            stats.subMana(cost);
            cooldowns.start(spellId, cooldownTicks);
            PacketDistributor.sendToPlayer(player, new PacketSyncSpellCooldown(spellId, cooldownTicks));
            ModDataAttachments.sync(player);
        }

        return cast;
    }

    private static boolean castFireball(ServerPlayer player, PlayerStats stats) {
        Vec3 look = player.getLookAngle();
        int explosionPower = 1 + (stats.getMagicPower() / 100);
        LargeFireball fireball = new LargeFireball(player.level(), player, look, explosionPower);
        fireball.setPos(player.getX(), player.getEyeY(), player.getZ());
        player.level().addFreshEntity(fireball);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F,
                1.0F + (stats.getMagicPower() * 0.003F));
        return true;
    }

    private static boolean castGust(ServerPlayer player, PlayerStats stats) {
        AABB area = player.getBoundingBox().inflate(4.0D);
        double strength = 1.1D * StatLogicHandler.getMagicPowerMultiplier(stats);
        boolean affected = false;

        for (LivingEntity entity : player.level().getEntitiesOfClass(
                LivingEntity.class, area, entity -> entity != player && entity.isAlive())) {
            Vec3 push = entity.position().subtract(player.position());
            if (push.lengthSqr() > 0.001D) {
                Vec3 normalized = push.normalize();
                entity.push(normalized.x * strength, 0.45D * StatLogicHandler.getMagicPowerMultiplier(stats), normalized.z * strength);
                entity.hurtMarked = true;
                affected = true;
            }
        }

        if (affected) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BREEZE_SHOOT, SoundSource.PLAYERS, 1.0F,
                    0.9F + (stats.getMagicPower() * 0.003F));
        }

        return true;
    }

    private static boolean castIceShard(ServerPlayer player, PlayerStats stats) {
        LivingEntity target = null;
        double bestDistance = 16.0D;

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();

        for (LivingEntity entity : player.level().getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(16.0D),
                entity -> entity != player && entity.isAlive())) {
            Vec3 toTarget = entity.getBoundingBox().getCenter().subtract(eye);
            double distance = toTarget.length();
            if (distance > bestDistance) continue;
            if (look.dot(toTarget.normalize()) < 0.94D) continue;
            if (!player.hasLineOfSight(entity)) continue;

            if (distance < bestDistance) {
                bestDistance = distance;
                target = entity;
            }
        }

        if (target == null) return false;

        float damage = 6.0F * StatLogicHandler.getMagicPowerMultiplier(stats);
        target.hurt(player.damageSources().magic(), damage);
        player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.8F,
                1.1F + (stats.getMagicPower() * 0.004F));
        return true;
    }

    private static boolean castFireWind(ServerPlayer player, PlayerStats stats) {
        castFireball(player, stats);
        castGust(player, stats);
        return true;
    }
}
