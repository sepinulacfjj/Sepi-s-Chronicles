package com.sepinula.sepimod.network;

import com.sepinula.sepimod.SepiMod;
import com.sepinula.sepimod.util.ModDataAttachments;
import com.sepinula.sepimod.util.PlayerSpellbookData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncSpellbookData(boolean equipped, int capacity, java.util.Optional<ResourceLocation> itemId) implements CustomPacketPayload {
    public static final Type<PacketSyncSpellbookData> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "sync_spellbook_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSyncSpellbookData> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, PacketSyncSpellbookData::equipped,
                    ByteBufCodecs.VAR_INT, PacketSyncSpellbookData::capacity,
                    ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), PacketSyncSpellbookData::itemId,
                    PacketSyncSpellbookData::new
            );

    public static PacketSyncSpellbookData from(PlayerSpellbookData data) {
        ResourceLocation itemId = data.hasSpellbook() ? BuiltInRegistries.ITEM.getKey(data.getSpellbook().getItem()) : null;
        return new PacketSyncSpellbookData(data.hasSpellbook(), data.getSpellSlotCapacity(), java.util.Optional.ofNullable(itemId));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncSpellbookData payload, IPayloadContext context) {
        context.enqueueWork(() ->
                context.player().setData(
                        ModDataAttachments.PLAYER_SPELLBOOK_DATA,
                        PlayerSpellbookData.clientState(payload.equipped(), payload.capacity(), payload.itemId().map(id -> new ItemStack(BuiltInRegistries.ITEM.get(id))).orElse(ItemStack.EMPTY))
                )
        );
    }
}
