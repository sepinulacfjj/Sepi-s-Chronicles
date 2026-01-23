package com.sepinula.sepimod.network;

import com.sepinula.sepimod.SepiMod;
import com.sepinula.sepimod.util.ModDataAttachments;
import com.sepinula.sepimod.util.PlayerStats;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncStats(PlayerStats stats) implements CustomPacketPayload {
    public static final Type<PacketSyncStats> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "sync_stats"));

    // FIX: Changed FriendlyByteBuf to RegistryFriendlyByteBuf to match PlayerStats.STREAM_CODEC
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSyncStats> STREAM_CODEC = StreamCodec.composite(
            PlayerStats.STREAM_CODEC, PacketSyncStats::stats, PacketSyncStats::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final PacketSyncStats payload, final IPayloadContext context) {
        context.enqueueWork(() -> context.player().setData(ModDataAttachments.PLAYER_STATS, payload.stats()));
    }
}