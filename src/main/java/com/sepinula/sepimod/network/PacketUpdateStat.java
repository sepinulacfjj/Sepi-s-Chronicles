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

    public static final Type<PacketUpdateStat> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "update_stat"));

    public static final StreamCodec<FriendlyByteBuf, PacketUpdateStat> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PacketUpdateStat::statName, PacketUpdateStat::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(final PacketUpdateStat payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);

            if (stats.getAvailablePoints() > 0) {
                String name = payload.statName().toLowerCase();
                boolean isConstitution = name.equals("constitution");

                switch (name) {
                    case "strength" -> stats.setStrength(stats.getStrength() + 1);
                    case "agility" -> stats.setAgility(stats.getAgility() + 1);
                    case "constitution" -> stats.setConstitution(stats.getConstitution() + 1);
                    case "willpower" -> stats.setWillpower(stats.getWillpower() + 1);
                    case "mind" -> stats.setMind(stats.getMind() + 1);
                    case "mana" -> stats.setMana(stats.getMana() + 1);
                    case "defense" -> stats.setDefense(stats.getDefense() + 1); // Updated
                    case "charisma" -> stats.setCharisma(stats.getCharisma() + 1);
                }

                stats.setAvailablePoints(stats.getAvailablePoints() - 1);

                // Update physical attributes (Health/Knockback Res)
                StatLogicHandler.applyStatModifiers(player, stats);

                if (isConstitution) {
                    player.heal(1.0F);
                }

                ModDataAttachments.sync(player);
            }
        });
    }
}