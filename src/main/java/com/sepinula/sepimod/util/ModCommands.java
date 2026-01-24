package com.sepinula.sepimod.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sepi")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("stats")
                        // --- MAX STATS ---
                        .then(Commands.literal("max")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
                                    stats.setStrength(100);
                                    stats.setAgility(100);
                                    stats.setConstitution(100);
                                    stats.setDexterity(100);
                                    stats.setWillpower(100);
                                    stats.setCharisma(100);
                                    stats.setMana(100);
                                    stats.setMind(100);
                                    ModDataAttachments.sync(player);
                                    context.getSource().sendSuccess(() -> Component.literal("§6[SepiMod] §fAll stats set to §dMAX§f!"), true);
                                    return 1;
                                }))
                        // --- RESET STAT LEVELS ---
                        .then(Commands.literal("reset")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
                                    stats.setStrength(0);
                                    stats.setAgility(0);
                                    stats.setConstitution(0);
                                    stats.setDexterity(0);
                                    stats.setWillpower(0);
                                    stats.setCharisma(0);
                                    stats.setMana(0);
                                    stats.setMind(0);
                                    ModDataAttachments.sync(player);
                                    context.getSource().sendSuccess(() -> Component.literal("§aStat levels have been reset!"), true);
                                    return 1;
                                }))
                        // --- RESET POINTS ---
                        .then(Commands.literal("reset_points")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
                                    stats.setAvailablePoints(0);
                                    ModDataAttachments.sync(player);
                                    context.getSource().sendSuccess(() -> Component.literal("§eTraining points have been cleared!"), true);
                                    return 1;
                                }))
                        // --- ADD TRAINING POINTS (WITH 999 CAP LOGIC) ---
                        .then(Commands.literal("training_points")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                            int amount = IntegerArgumentType.getInteger(context, "amount");
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);

                                            int current = stats.getAvailablePoints();
                                            if (current >= 999) {
                                                context.getSource().sendFailure(Component.literal("§cPlayer is already at the maximum limit of 999 points!"));
                                                return 0;
                                            }

                                            // Points setter in PlayerStats.java should also have Math.min(val, 999)
                                            stats.setAvailablePoints(current + amount);
                                            ModDataAttachments.sync(player);

                                            int finalPoints = stats.getAvailablePoints();
                                            context.getSource().sendSuccess(() -> Component.literal("§6[SepiMod] §fPoints updated. Total: §e" + finalPoints + " §7(Cap: 999)"), true);
                                            return 1;
                                        }))))
                // --- RESET CLASS ---
                .then(Commands.literal("reset_class")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
                            stats.setPlayerClass("NONE");
                            ModDataAttachments.sync(player);
                            context.getSource().sendSuccess(() -> Component.literal("§bClass reset! You can now press 'O' to choose again."), true);
                            return 1;
                        })));
    }
}