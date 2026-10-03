package com.sepinula.sepimod.network;

import com.sepinula.sepimod.SepiMod;
import com.sepinula.sepimod.spellbook.SpellbookItem;
import com.sepinula.sepimod.util.ModDataAttachments;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncSpellbookData(ItemStack spellbook) implements CustomPacketPayload {
    public static final Type<PacketSyncSpellbookData> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "sync_spellbook_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSyncSpellbookData> STREAM_CODEC =
            StreamCodec.composite(
                    ItemStack.STREAM_CODEC, PacketSyncSpellbookData::spellbook,
                    PacketSyncSpellbookData::new
            );

    public static PacketSyncSpellbookData from(ItemStack stack) {
        return new PacketSyncSpellbookData(stack.copy());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncSpellbookData payload, IPayloadContext context) {
        context.enqueueWork(() ->
                context.player().setData(
                        ModDataAttachments.PLAYER_SPELLBOOK_DATA,
                        new com.sepinula.sepimod.util.PlayerSpellbookData(payload.spellbook())
                )
        );
    }
}