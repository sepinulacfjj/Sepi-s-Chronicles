package com.sepinula.sepimod.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Runtime cooldown state for the player's spells.
 * Cooldowns belong to the player, not to the physical spellbook.
 */
public class PlayerSpellCooldownData {
    private final Map<String, Integer> remainingTicks;

    public PlayerSpellCooldownData() {
        this.remainingTicks = new HashMap<>();
    }

    public PlayerSpellCooldownData(Map<String, Integer> remainingTicks) {
        this.remainingTicks = new HashMap<>();
        for (var entry : remainingTicks.entrySet()) {
            if (entry.getValue() > 0) {
                this.remainingTicks.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public int getRemainingTicks(String spellId) {
        return Math.max(0, remainingTicks.getOrDefault(spellId, 0));
    }

    public boolean isOnCooldown(String spellId) {
        return getRemainingTicks(spellId) > 0;
    }

    public void start(String spellId, int ticks) {
        if (spellId == null || spellId.isBlank() || ticks <= 0) {
            return;
        }
        remainingTicks.put(spellId, ticks);
    }

    public void tick() {
        remainingTicks.replaceAll((id, ticks) -> ticks - 1);
        remainingTicks.entrySet().removeIf(entry -> entry.getValue() <= 0);
    }

    public Map<String, Integer> getRemainingTicks() {
        return Map.copyOf(remainingTicks);
    }
}
