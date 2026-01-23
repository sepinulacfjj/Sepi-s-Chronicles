package com.sepinula.sepimod.network;

import com.sepinula.sepimod.SepiMod;
import com.sepinula.sepimod.client.gui.ClassSelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketOpenClassScreen() implements CustomPacketPayload {
    public static final Type<PacketOpenClassScreen> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "open_class_screen"));
    public static final StreamCodec<FriendlyByteBuf, PacketOpenClassScreen> STREAM_CODEC = StreamCodec.unit(new PacketOpenClassScreen());

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(final PacketOpenClassScreen payload, final IPayloadContext context) {
        context.enqueueWork(() -> Minecraft.getInstance().setScreen(new ClassSelectionScreen()));
    }
}