package com.sepinula.sepimod.network;

import com.sepinula.sepimod.SepiMod;
import com.sepinula.sepimod.util.ModDataAttachments;
import com.sepinula.sepimod.util.PlayerSpellData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record PacketSyncSpellData(List<String> knownSpells, List<String> activeSpells, int selectedSpellIndex)
        implements CustomPacketPayload {

    public static final Type<PacketSyncSpellData> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "sync_spell_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSyncSpellData> STREAM_CODEC =
            StreamCodec.of(PacketSyncSpellData::encode, PacketSyncSpellData::decode);

    private static void encode(RegistryFriendlyByteBuf buffer, PacketSyncSpellData packet) {
        buffer.writeVarInt(packet.knownSpells.size());
        for (String spell : packet.knownSpells) {
            buffer.writeUtf(spell);
        }

        buffer.writeVarInt(packet.activeSpells.size());
        for (String spell : packet.activeSpells) {
            buffer.writeUtf(spell);
        }

        buffer.writeVarInt(packet.selectedSpellIndex);
    }

    private static PacketSyncSpellData decode(RegistryFriendlyByteBuf buffer) {
        int knownCount = buffer.readVarInt();
        List<String> known = new ArrayList<>(knownCount);
        for (int i = 0; i < knownCount; i++) {
            known.add(buffer.readUtf());
        }

        int activeCount = buffer.readVarInt();
        List<String> active = new ArrayList<>(activeCount);
        for (int i = 0; i < activeCount; i++) {
            active.add(buffer.readUtf());
        }

        return new PacketSyncSpellData(known, active, buffer.readVarInt());
    }

    public static PacketSyncSpellData from(PlayerSpellData data) {
        return new PacketSyncSpellData(
                data.getKnownSpells(),
                data.getActiveSpells(),
                data.getSelectedSpellIndex()
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final PacketSyncSpellData payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            context.player().setData(
                    ModDataAttachments.PLAYER_SPELL_DATA,
                    new PlayerSpellData(payload.knownSpells(), payload.activeSpells(), payload.selectedSpellIndex())
            );
        });
    }
}
