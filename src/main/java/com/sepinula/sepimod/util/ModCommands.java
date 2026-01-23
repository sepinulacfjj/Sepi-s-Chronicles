package com.sepinula.sepimod.util;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sepi")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("stats")
                        .then(Commands.literal("max")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
                                    stats.setStrength(100);
                                    stats.setAgility(100);
                                    // ... Set all other stats to 100 ...
                                    ModDataAttachments.sync(player);
                                    context.getSource().sendSuccess(() -> Component.literal("§6[SepiMod] §fAll stats maxed!"), true);
                                    return 1;
                                }))
                        .then(Commands.literal("reset")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    player.setData(ModDataAttachments.PLAYER_STATS, new PlayerStats());
                                    ModDataAttachments.sync(player);
                                    context.getSource().sendSuccess(() -> Component.literal("§aStats reset successfully!"), true);
                                    return 1;
                                }))));
    }
}