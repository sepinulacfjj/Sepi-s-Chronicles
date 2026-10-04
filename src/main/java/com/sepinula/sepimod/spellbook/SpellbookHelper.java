package com.sepinula.sepimod.spellbook;

import com.sepinula.sepimod.util.ModDataAttachments;
import com.sepinula.sepimod.util.PlayerSpellData;
import com.sepinula.sepimod.util.PlayerSpellbookData;
import net.minecraft.world.entity.player.Player;

/**
 * Small orchestration helper for spellbook-related player state.
 */
public final class SpellbookHelper {

    private SpellbookHelper() {
    }

    public static boolean hasSpellbook(Player player) {
        return player.getData(ModDataAttachments.PLAYER_SPELLBOOK_DATA).hasSpellbook();
    }

    public static int getCapacity(Player player) {
        return player.getData(ModDataAttachments.PLAYER_SPELLBOOK_DATA).getSpellSlotCapacity();
    }

    /**
     * Makes sure the player's configured spells cannot exceed the equipped
     * spellbook's capacity.
     */
    public static void enforceCapacity(Player player) {
        PlayerSpellData spells = player.getData(ModDataAttachments.PLAYER_SPELL_DATA);
        PlayerSpellbookData book = player.getData(ModDataAttachments.PLAYER_SPELLBOOK_DATA);

        // The player's spell configuration must survive removing a physical
        // spellbook. A missing book means the spells are temporarily unusable,
        // not forgotten.
        if (!book.hasSpellbook()) {
            return;
        }

        // The physical book supplies only the current usable capacity.
        // Never delete the player's saved active-spell configuration.
        spells.clampSelectionToCapacity(book.getSpellSlotCapacity());
    }
}
