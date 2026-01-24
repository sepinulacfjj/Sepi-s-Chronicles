package com.sepinula.sepimod.network;

import com.sepinula.sepimod.SepiMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = SepiMod.MODID)
public class Messages {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(SepiMod.MODID);

        // Registering the records we created
        registrar.playToServer(PacketUpdateStat.TYPE, PacketUpdateStat.STREAM_CODEC, PacketUpdateStat::handle);
        registrar.playToServer(PacketSelectClass.TYPE, PacketSelectClass.STREAM_CODEC, PacketSelectClass::handle);
        registrar.playToClient(PacketSyncStats.TYPE, PacketSyncStats.STREAM_CODEC, PacketSyncStats::handle);
        registrar.playToClient(PacketOpenClassScreen.TYPE, PacketOpenClassScreen.STREAM_CODEC, PacketOpenClassScreen::handle);
    }

    public static <MSG extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> void sendToServer(MSG message) {
        PacketDistributor.sendToServer(message);
    }
}