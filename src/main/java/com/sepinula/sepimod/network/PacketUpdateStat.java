package com.sepinula.sepimod.network;

import com.sepinula.sepimod.SepiMod;
import com.sepinula.sepimod.util.ModDataAttachments;
import com.sepinula.sepimod.util.PlayerStats;
import com.sepinula.sepimod.util.StatLogicHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketUpdateStat(String statName) implements CustomPacketPayload {
    public static final Type<PacketUpdateStat> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "update_stat"));

    public static final StreamCodec<FriendlyByteBuf, PacketUpdateStat> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.STRING_UTF8, PacketUpdateStat::statName, PacketUpdateStat::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final PacketUpdateStat payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
            if (stats.getAvailablePoints() <= 0) return;

            String name = payload.statName().toLowerCase();
            boolean valid = true;

            switch (name) {
                case "strength" -> stats.setStrength(stats.getStrengthRaw() + 1);
                case "agility" -> stats.setAgility(stats.getAgilityRaw() + 1);
                case "constitution" -> stats.setConstitution(stats.getConstitutionRaw() + 1);
                case "magic_resistance" -> stats.setMagicResistance(stats.getMagicResistanceRaw() + 1);
                case "magic_power" -> stats.setMagicPower(stats.getMagicPowerRaw() + 1);
                case "mana" -> stats.setMana(stats.getManaRaw() + 1);
                case "defense" -> stats.setDefense(stats.getDefenseRaw() + 1);
                case "mind" -> stats.setMind(stats.getMindRaw() + 1);
                default -> valid = false;
            }

            if (!valid) return;

            stats.setAvailablePoints(stats.getAvailablePoints() - 1);
            StatLogicHandler.applyStatModifiers(player, stats);

            if (name.equals("constitution")) {
                player.heal(1.0F);
            }

            ModDataAttachments.sync(player);
        });
    }
}
