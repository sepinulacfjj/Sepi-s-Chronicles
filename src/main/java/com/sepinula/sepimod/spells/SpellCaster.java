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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
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
        final double range = 6.0D;
        final double minimumFacingDot = 0.68D; // Broad forward cone, roughly 47 degrees.
        double magicMultiplier = StatLogicHandler.getMagicPowerMultiplier(stats);
        double knockbackStrength = 1.35D * magicMultiplier;
        float damage = (float) (4.0D * magicMultiplier);
        Vec3 look = player.getLookAngle().normalize();
        Vec3 eye = player.getEyePosition();
        AABB area = player.getBoundingBox().inflate(range, 3.0D, range);

        // Play the casting animation and sound even if no target is caught in the blast.
        player.swing(InteractionHand.MAIN_HAND, true);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BREEZE_SHOOT, SoundSource.PLAYERS, 1.0F,
                0.9F + (stats.getMagicPower() * 0.003F));

        // Replicate a short stream of wind particles along the direction of the blast.
        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 horizontalLook = new Vec3(look.x, 0.0D, look.z);
            if (horizontalLook.lengthSqr() < 0.001D) {
                horizontalLook = new Vec3(0.0D, 0.0D, 1.0D);
            } else {
                horizontalLook = horizontalLook.normalize();
            }
            Vec3 side = new Vec3(-horizontalLook.z, 0.0D, horizontalLook.x);
            for (int step = 1; step <= 6; step++) {
                double distance = step * 0.8D;
                Vec3 center = eye.add(look.scale(distance));
                double spread = 0.18D + step * 0.10D;
                for (int lane = -2; lane <= 2; lane++) {
                    Vec3 particlePos = center.add(side.scale(lane * spread));
                    serverLevel.sendParticles(ParticleTypes.CLOUD,
                            particlePos.x, particlePos.y, particlePos.z,
                            2, spread * 0.25D, 0.18D, spread * 0.25D, 0.015D);
                }
            }
            serverLevel.sendParticles(ParticleTypes.POOF,
                    eye.x + look.x * 1.2D, eye.y + look.y * 1.2D, eye.z + look.z * 1.2D,
                    8, 0.25D, 0.25D, 0.25D, 0.04D);
        }

        for (LivingEntity entity : player.level().getEntitiesOfClass(
                LivingEntity.class, area, entity -> entity != player && entity.isAlive() && !entity.isAlliedTo(player))) {
            Vec3 toTarget = entity.getBoundingBox().getCenter().subtract(eye);
            double distance = toTarget.length();
            if (distance < 0.001D || distance > range) {
                continue;
            }

            // Only affect entities in front of the caster and visible through line of sight.
            if (look.dot(toTarget.scale(1.0D / distance)) < minimumFacingDot
                    || !player.hasLineOfSight(entity)) {
                continue;
            }

            entity.hurt(player.damageSources().magic(), damage);

            Vec3 push = entity.position().subtract(player.position());
            push = new Vec3(push.x, 0.0D, push.z);
            if (push.lengthSqr() < 0.001D) {
                push = new Vec3(look.x, 0.0D, look.z);
            }
            if (push.lengthSqr() > 0.001D) {
                push = push.normalize();
                entity.push(push.x * knockbackStrength,
                        0.35D + 0.15D * magicMultiplier,
                        push.z * knockbackStrength);
                entity.hurtMarked = true;
            }
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
