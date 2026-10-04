package com.sepinula.sepimod.network;

import com.sepinula.sepimod.SepiMod;
import com.sepinula.sepimod.util.ModDataAttachments;
import com.sepinula.sepimod.util.PlayerSpellCooldownData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncSpellCooldown(String spellId, int remainingTicks) implements CustomPacketPayload {

    public static final Type<PacketSyncSpellCooldown> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "sync_spell_cooldown"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSyncSpellCooldown> STREAM_CODEC =
            StreamCodec.of(PacketSyncSpellCooldown::encode, PacketSyncSpellCooldown::decode);

    private static void encode(RegistryFriendlyByteBuf buffer, PacketSyncSpellCooldown packet) {
        buffer.writeUtf(packet.spellId());
        buffer.writeVarInt(packet.remainingTicks());
    }

    private static PacketSyncSpellCooldown decode(RegistryFriendlyByteBuf buffer) {
        return new PacketSyncSpellCooldown(buffer.readUtf(), buffer.readVarInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncSpellCooldown payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            PlayerSpellCooldownData cooldowns =
                    context.player().getData(ModDataAttachments.PLAYER_SPELL_COOLDOWNS);
            cooldowns.start(payload.spellId(), payload.remainingTicks());
        });
    }
}
