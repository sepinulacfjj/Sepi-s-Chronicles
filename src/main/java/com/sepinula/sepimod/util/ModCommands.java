package com.sepinula.sepimod.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Collections;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> baseCommand = Commands.literal("sepi")
                .requires(source -> source.hasPermission(2));

        LiteralArgumentBuilder<CommandSourceStack> statsCommand = Commands.literal("stats");

        statsCommand.then(Commands.argument("targets", EntityArgument.players())
                .then(Commands.literal("max").executes(context -> performMax(context.getSource(), EntityArgument.getPlayers(context, "targets"))))
                .then(Commands.literal("reset").executes(context -> performReset(context.getSource(), EntityArgument.getPlayers(context, "targets"))))
                .then(Commands.literal("reset_points").executes(context -> performResetPoints(context.getSource(), EntityArgument.getPlayers(context, "targets"))))
                .then(Commands.literal("reset_class").executes(context -> performResetClass(context.getSource(), EntityArgument.getPlayers(context, "targets"))))
                .then(Commands.literal("training_points").then(Commands.argument("amount", IntegerArgumentType.integer())
                        .executes(context -> performAddTrainingPoints(context.getSource(), EntityArgument.getPlayers(context, "targets"), IntegerArgumentType.getInteger(context, "amount")))))

                .then(addStatNode("strength"))
                .then(addStatNode("agility"))
                .then(addStatNode("constitution"))
                .then(addStatNode("willpower"))
                .then(addStatNode("defense"))
                .then(addStatNode("charisma"))
                .then(addStatNode("mana"))
                .then(addStatNode("mind"))
        );

        statsCommand.then(Commands.literal("max").executes(context -> performMax(context.getSource(), Collections.singleton(context.getSource().getPlayerOrException()))));
        statsCommand.then(Commands.literal("reset").executes(context -> performReset(context.getSource(), Collections.singleton(context.getSource().getPlayerOrException()))));
        statsCommand.then(Commands.literal("reset_points").executes(context -> performResetPoints(context.getSource(), Collections.singleton(context.getSource().getPlayerOrException()))));
        statsCommand.then(Commands.literal("reset_class").executes(context -> performResetClass(context.getSource(), Collections.singleton(context.getSource().getPlayerOrException()))));
        statsCommand.then(Commands.literal("training_points").then(Commands.argument("amount", IntegerArgumentType.integer())
                .executes(context -> performAddTrainingPoints(context.getSource(), Collections.singleton(context.getSource().getPlayerOrException()), IntegerArgumentType.getInteger(context, "amount")))));

        dispatcher.register(baseCommand.then(statsCommand));
    }

    private static int performMax(CommandSourceStack source, Collection<ServerPlayer> targets) {
        for (ServerPlayer player : targets) {
            PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
            stats.setStrength(100); stats.setAgility(100); stats.setConstitution(100);
            stats.setDefense(100); stats.setWillpower(100); stats.setCharisma(100);
            stats.setMana(100); stats.setMind(100);
            stats.setAvailablePoints(800);
            stats.setTrainingPoints(800);
            sync(player);
        }
        source.sendSuccess(() -> Component.literal("§6[SepiMod] §fStats maxed and points set to 800."), true);
        return targets.size();
    }

    private static int performReset(CommandSourceStack source, Collection<ServerPlayer> targets) {
        for (ServerPlayer player : targets) {
            player.getData(ModDataAttachments.PLAYER_STATS).resetAll();
            sync(player);
        }
        source.sendSuccess(() -> Component.literal("§aFull reset complete for target(s)."), true);
        return targets.size();
    }

    private static int performResetPoints(CommandSourceStack source, Collection<ServerPlayer> targets) {
        for (ServerPlayer player : targets) {
            PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
            stats.setAvailablePoints(0);
            recalculateTrainingPoints(stats);
            sync(player);
        }
        source.sendSuccess(() -> Component.literal("§eAvailable points cleared and XP bar synced."), true);
        return targets.size();
    }

    private static int performResetClass(CommandSourceStack source, Collection<ServerPlayer> targets) {
        for (ServerPlayer player : targets) {
            player.getData(ModDataAttachments.PLAYER_STATS).setArchetype(RpgArchetype.NONE);
            sync(player);
        }
        source.sendSuccess(() -> Component.literal("§bClass reset to NONE."), true);
        return targets.size();
    }

    private static int performAddTrainingPoints(CommandSourceStack source, Collection<ServerPlayer> targets, int amount) {
        for (ServerPlayer player : targets) {
            PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
            int currentPoints = stats.getTrainingPoints();
            int canAdd = 800 - currentPoints;
            int actualAdd = Math.min(amount, canAdd);

            if (actualAdd > 0 || amount < 0) {
                stats.setAvailablePoints(Math.max(0, stats.getAvailablePoints() + actualAdd));
                stats.setTrainingPoints(Math.max(0, stats.getTrainingPoints() + actualAdd));
            }
            sync(player);
        }
        source.sendSuccess(() -> Component.literal("§6[SepiMod] §fPoints updated (Capped at 800)."), true);
        return targets.size();
    }

    private static LiteralArgumentBuilder<CommandSourceStack> addStatNode(String statName) {
        return Commands.literal(statName)
                .then(Commands.literal("reset").executes(context -> {
                    Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
                    for (ServerPlayer player : targets) {
                        PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
                        setRawValue(stats, statName, 0);
                        recalculateTrainingPoints(stats);
                        sync(player);
                    }
                    context.getSource().sendSuccess(() -> Component.literal("§6[SepiMod] §f" + statName + " reset to 0."), true);
                    return targets.size();
                }))
                .then(Commands.argument("amount", IntegerArgumentType.integer())
                        .executes(context -> {
                            Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
                            int amount = IntegerArgumentType.getInteger(context, "amount");

                            for (ServerPlayer player : targets) {
                                PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
                                int currentRaw = getRawValue(stats, statName);

                                if (currentRaw >= 100 && amount > 0) {
                                    context.getSource().sendFailure(Component.literal("§c" + player.getScoreboardName() + " is already at max " + statName + "!"));
                                    continue;
                                }

                                int newValue = Math.min(100, Math.max(0, currentRaw + amount));
                                setRawValue(stats, statName, newValue);

                                recalculateTrainingPoints(stats);
                                sync(player);
                                context.getSource().sendSuccess(() -> Component.literal("§6[SepiMod] §f" + statName + " updated to §e" + newValue + "§f. XP bar synced."), true);
                            }
                            return targets.size();
                        }));
    }

    private static void recalculateTrainingPoints(PlayerStats stats) {
        int total = stats.getStrengthRaw() + stats.getAgilityRaw() + stats.getConstitution() +
                stats.getWillpowerRaw() + stats.getDefenseRaw() + stats.getCharisma() +
                stats.getManaRaw() + stats.getMindRaw() + stats.getAvailablePoints();

        stats.setTrainingPoints(Math.min(800, total));
    }

    private static int getRawValue(PlayerStats stats, String stat) {
        return switch (stat) {
            case "strength" -> stats.getStrengthRaw();
            case "agility" -> stats.getAgilityRaw();
            case "constitution" -> stats.getConstitution();
            case "willpower" -> stats.getWillpowerRaw();
            case "defense" -> stats.getDefenseRaw();
            case "charisma" -> stats.getCharisma();
            case "mana" -> stats.getManaRaw();
            case "mind" -> stats.getMindRaw();
            default -> 0;
        };
    }

    private static void setRawValue(PlayerStats stats, String stat, int val) {
        switch (stat) {
            case "strength" -> stats.setStrength(val);
            case "agility" -> stats.setAgility(val);
            case "constitution" -> stats.setConstitution(val);
            case "willpower" -> stats.setWillpower(val);
            case "defense" -> stats.setDefense(val);
            case "charisma" -> stats.setCharisma(val);
            case "mana" -> stats.setMana(val);
            case "mind" -> stats.setMind(val);
        }
    }

    private static void sync(ServerPlayer player) {
        StatLogicHandler.applyStatModifiers(player, player.getData(ModDataAttachments.PLAYER_STATS));
        ModDataAttachments.sync(player);
    }
}