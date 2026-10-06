package com.sepinula.sepimod.spellbook;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Delays opening the spellbook screen long enough for the held GeoItem
 * opening animation to finish on the client.
 */
public final class SpellbookOpenScheduler {
    private static final int OPEN_ANIMATION_TICKS = 41;
    private static final List<PendingOpen> PENDING = new ArrayList<>();

    private SpellbookOpenScheduler() {
    }

    public static synchronized void schedule(ServerPlayer player, InteractionHand hand, Item item) {
        PENDING.add(new PendingOpen(player.getUUID(), hand, item, OPEN_ANIMATION_TICKS));
    }

    public static synchronized void onServerTick(ServerTickEvent.Post event) {
        Iterator<PendingOpen> iterator = PENDING.iterator();

        while (iterator.hasNext()) {
            PendingOpen pending = iterator.next();
            pending.ticksRemaining--;

            if (pending.ticksRemaining > 0) {
                continue;
            }

            iterator.remove();
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(pending.playerId);

            // Don't open a screen if the player disconnected, died, or switched
            // away from the spellbook while the animation was playing.
            if (player == null || !player.isAlive() || player.getItemInHand(pending.hand).is(pending.item)) {
                if (player != null && player.isAlive()
                        && player.getItemInHand(pending.hand).is(pending.item)) {
                    player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                            (containerId, inventory, ignoredPlayer) ->
                                    new SpellbookMenu(containerId, inventory),
                            net.minecraft.network.chat.Component.literal("Spellbook")
                    ));
                }
            }
        }
    }

    private static final class PendingOpen {
        private final UUID playerId;
        private final InteractionHand hand;
        private final Item item;
        private int ticksRemaining;

        private PendingOpen(UUID playerId, InteractionHand hand, Item item, int ticksRemaining) {
            this.playerId = playerId;
            this.hand = hand;
            this.item = item;
            this.ticksRemaining = ticksRemaining;
        }
    }
}
