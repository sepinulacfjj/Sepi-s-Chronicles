package com.sepinula.sepimod.util;

import com.sepinula.sepimod.network.PacketSyncStats;
import com.sepinula.sepimod.SepiMod;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import java.util.function.Supplier;

public class ModDataAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, SepiMod.MODID);

    public static final Supplier<AttachmentType<PlayerStats>> PLAYER_STATS = ATTACHMENT_TYPES.register(
            "player_stats", () -> AttachmentType.builder(PlayerStats::new)
                    .serialize(PlayerStats.CODEC)
                    .copyOnDeath()
                    .build());

    public static void sync(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new PacketSyncStats(player.getData(PLAYER_STATS)));
    }

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}