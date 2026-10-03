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

        spells.setActiveSpells(spells.getActiveSpells(), book.getSpellSlotCapacity());
    }
}
