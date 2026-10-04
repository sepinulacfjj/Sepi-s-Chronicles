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

    /**
     * Player-specific spell knowledge, active spell slots and selected spell.
     * This is intentionally separate from the physical spellbook item.
     */
    public static final Supplier<AttachmentType<PlayerSpellData>> PLAYER_SPELL_DATA = ATTACHMENT_TYPES.register(
            "player_spell_data", () -> AttachmentType.builder(PlayerSpellData::new)
                    .serialize(PlayerSpellData.CODEC)
                    .copyOnDeath()
                    .build());

    /**
     * Runtime spell cooldowns. These are not persistent player progression.
     */
    public static final Supplier<AttachmentType<PlayerSpellCooldownData>> PLAYER_SPELL_COOLDOWNS = ATTACHMENT_TYPES.register(
            "player_spell_cooldowns", () -> AttachmentType.builder((java.util.function.Supplier<PlayerSpellCooldownData>) PlayerSpellCooldownData::new)
                    .build());

    /**
     * The physical spellbook currently equipped in the player's dedicated
     * spellbook slot. This is separate from PlayerSpellData.
     */
    public static final Supplier<AttachmentType<PlayerSpellbookData>> PLAYER_SPELLBOOK_DATA = ATTACHMENT_TYPES.register(
            "player_spellbook_data", () -> AttachmentType.builder((java.util.function.Supplier<PlayerSpellbookData>) PlayerSpellbookData::new)
                    .serialize(PlayerSpellbookData.CODEC)
                    .copyOnDeath()
                    .build());

    public static void sync(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new PacketSyncStats(player.getData(PLAYER_STATS)));
    }

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
