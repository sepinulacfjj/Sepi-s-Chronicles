package com.sepinula.sepimod.spells;

import com.sepinula.sepimod.spellbook.SpellbookHelper;
import net.minecraft.world.entity.player.Player;

/**
 * Calculates the cooldown a player actually experiences for a spell.
 *
 * Keeping this calculation in one place lets spellbooks, armor and other
 * future equipment modify cooldowns without changing every spell individually.
 */
public final class SpellCooldownHelper {

    private SpellCooldownHelper() {
    }

    public static int getEffectiveCooldownTicks(Player player, Spell spell) {
        int baseCooldown = Math.max(0, spell.cooldownTicks());

        // Future spellbook and armor modifiers belong here.
        // For now, every equipped spellbook uses the spell's base cooldown.
        return Math.max(0, Math.round(baseCooldown * getCooldownMultiplier(player)));
    }

    private static float getCooldownMultiplier(Player player) {
        // Reserved for spellbook-tier, armor-set and other RPG modifiers.
        // Keeping the hook here means the rest of the spell system does not
        // need to know how equipment affects cooldowns.
        if (!SpellbookHelper.hasSpellbook(player)) {
            return 1.0F;
        }

        return 1.0F;
    }
}
