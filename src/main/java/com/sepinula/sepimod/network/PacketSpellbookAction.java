package com.sepinula.sepimod.network;

import com.sepinula.sepimod.SepiMod;
import com.sepinula.sepimod.spellbook.SpellbookHelper;
import com.sepinula.sepimod.spells.SpellCombinationRegistry;
import com.sepinula.sepimod.spells.SpellRegistry;
import com.sepinula.sepimod.util.ModDataAttachments;
import com.sepinula.sepimod.util.PlayerSpellData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSpellbookAction(int action, int firstIndex, int secondIndex) implements CustomPacketPayload {

    public static final int OPEN = 0;
    public static final int ADD_KNOWN = 1;
    public static final int REMOVE_ACTIVE = 2;
    public static final int COMBINE = 3;
    public static final int NEXT = 4;
    public static final int PREVIOUS = 5;
    public static final int CAST = 6;
    public static final int SELECT_ACTIVE = 7;

    public static final Type<PacketSpellbookAction> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "spellbook_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSpellbookAction> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, PacketSpellbookAction::action,
                    ByteBufCodecs.VAR_INT, PacketSpellbookAction::firstIndex,
                    ByteBufCodecs.VAR_INT, PacketSpellbookAction::secondIndex,
                    PacketSpellbookAction::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final PacketSpellbookAction payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            if (payload.action() == OPEN) {
                if (!SpellbookHelper.hasSpellbook(player)) {
                    player.displayClientMessage(Component.literal("§cEquip a spellbook first."), true);
                    return;
                }

                player.openMenu(new SimpleMenuProvider(
                        (containerId, inventory, ignoredPlayer) ->
                                new com.sepinula.sepimod.spellbook.SpellbookMenu(containerId, inventory),
                        Component.literal("Spellbook")
                ));
                return;
            }

            if (!SpellbookHelper.hasSpellbook(player)) {
                return;
            }

            PlayerSpellData data = player.getData(ModDataAttachments.PLAYER_SPELL_DATA);
            boolean changed = false;

            switch (payload.action()) {
                case ADD_KNOWN -> {
                    var known = data.getKnownSpells();
                    if (payload.firstIndex() >= 0 && payload.firstIndex() < known.size()) {
                        changed = data.addActiveSpell(known.get(payload.firstIndex()), SpellbookHelper.getCapacity(player));
                    }
                }
                case REMOVE_ACTIVE -> {
                    var active = data.getActiveSpells();
                    if (payload.firstIndex() >= 0 && payload.firstIndex() < active.size()) {
                        changed = data.removeActiveSpell(active.get(payload.firstIndex()));
                    }
                }
                case COMBINE -> {
                    var active = data.getActiveSpells();
                    int a = payload.firstIndex();
                    int b = payload.secondIndex();

                    if (a >= 0 && b >= 0 && a < active.size() && b < active.size() && a != b) {
                        ResourceLocation first = ResourceLocation.parse(active.get(a));
                        ResourceLocation second = ResourceLocation.parse(active.get(b));
                        var result = SpellCombinationRegistry.getResult(first, second);

                        if (result != null) {
                            changed = data.combineActiveSpells(a, b, result);
                        }
                    }
                }
                case NEXT -> {
                    data.selectNextSpell();
                    changed = true;
                }
                case PREVIOUS -> {
                    data.selectPreviousSpell();
                    changed = true;
                }
                case CAST -> {
                    changed = com.sepinula.sepimod.spells.SpellCaster.castSelected(player);
                }
                case SELECT_ACTIVE -> {
                    var active = data.getActiveSpells();
                    int index = payload.firstIndex();

                    if (index >= 0 && index < active.size()) {
                        data.setSelectedSpellIndex(index);
                        changed = true;
                    }
                }
                default -> {
                }
            }

            SpellbookHelper.enforceCapacity(player);

            if (changed) {
                PacketDistributor.sendToPlayer(player, PacketSyncSpellData.from(data));
            }
        });
    }
}
