package com.sepinula.sepimod.network;

import com.sepinula.sepimod.SepiMod;
import com.sepinula.sepimod.util.ModDataAttachments;
import com.sepinula.sepimod.util.PlayerStats;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSelectClass(String className) implements CustomPacketPayload {
    public static final Type<PacketSelectClass> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "select_class"));

    public static final StreamCodec<FriendlyByteBuf, PacketSelectClass> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PacketSelectClass::className,
            PacketSelectClass::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(final PacketSelectClass payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            PlayerStats stats = player.getData(ModDataAttachments.PLAYER_STATS);
            stats.setPlayerClass(payload.className());
            ModDataAttachments.sync(player);
        });
    }
}