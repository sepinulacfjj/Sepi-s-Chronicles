package com.sepinula.sepimod.spells;

import com.sepinula.sepimod.util.ModDataAttachments;
import com.sepinula.sepimod.util.PlayerSpellData;
import com.sepinula.sepimod.util.PlayerStats;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class SpellCaster {
    private SpellCaster() {}

    public static boolean castSelected(ServerPlayer player) {
        PlayerSpellData spellData = player.getData(ModDataAttachments.PLAYER_SPELL_DATA);
        String spellId = spellData.getSelectedSpellId();

        if (spellId.isBlank()) {
            return false;
        }

        float cost = manaCost(spellId);
        PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);

        if (stats.getCurrentMana() < cost) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cNot enough Mana!"), true);
            return false;
        }

        boolean cast = switch (spellId) {
            case "sepimod:fireball" -> castFireball(player);
            case "sepimod:gust" -> castGust(player);
            case "sepimod:ice_shard" -> castIceShard(player);
            case "sepimod:fire_wind" -> castFireWind(player);
            default -> false;
        };

        if (cast) {
            stats.subMana(cost);
            player.getCooldowns().addCooldown(
                    com.sepinula.sepimod.init.ModItems.IRON_SPELLBOOK.get(),
                    10
            );
            ModDataAttachments.sync(player);
        }

        return cast;
    }

    private static float manaCost(String id) {
        return switch (id) {
            case "sepimod:fireball" -> 15.0f;
            case "sepimod:gust" -> 10.0f;
            case "sepimod:ice_shard" -> 12.0f;
            case "sepimod:fire_wind" -> 25.0f;
            default -> 10.0f;
        };
    }

    private static boolean castFireball(ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        LargeFireball fireball = new LargeFireball(player.level(), player, look, 1);
        fireball.setPos(player.getX(), player.getEyeY(), player.getZ());
        player.level().addFreshEntity(fireball);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    private static boolean castGust(ServerPlayer player) {
        AABB area = player.getBoundingBox().inflate(4.0D);
        boolean affected = false;

        for (LivingEntity entity : player.level().getEntitiesOfClass(
                LivingEntity.class, area, entity -> entity != player && entity.isAlive())) {
            Vec3 push = entity.position().subtract(player.position());
            if (push.lengthSqr() > 0.001D) {
                entity.push(push.normalize().x * 1.1D, 0.45D, push.normalize().z * 1.1D);
                entity.hurtMarked = true;
                affected = true;
            }
        }

        if (affected) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BREEZE_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        return true;
    }

    private static boolean castIceShard(ServerPlayer player) {
        LivingEntity target = null;
        double bestDistance = 16.0D;

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();

        for (LivingEntity entity : player.level().getEntitiesOfClass(
                LivingEntity.class, player.getBoundingBox().inflate(16.0D),
                entity -> entity != player && entity.isAlive())) {
            Vec3 toTarget = entity.getBoundingBox().getCenter().subtract(eye);
            double distance = toTarget.length();
            if (distance > bestDistance) {
                continue;
            }

            if (look.dot(toTarget.normalize()) < 0.94D) {
                continue;
            }

            if (!player.hasLineOfSight(entity)) {
                continue;
            }

            if (distance < bestDistance) {
                bestDistance = distance;
                target = entity;
            }
        }

        if (target == null) {
            return false;
        }

        target.hurt(player.damageSources().magic(), 6.0F);
        player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.8F, 1.4F);
        return true;
    }

    private static boolean castFireWind(ServerPlayer player) {
        castFireball(player);
        castGust(player);
        return true;
    }
}
