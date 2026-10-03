package com.sepinula.sepimod.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Mth;

/**
 * Stores the spell information that belongs to a player.
 *
 * The physical spellbook does NOT own this data. This is important because
 * trading or storing a spellbook must not transfer the player's learned spells.
 */
public class PlayerSpellData {

    public static final int MAX_SPELL_SLOTS = 9;

    public static final Codec<PlayerSpellData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.listOf()
                            .fieldOf("knownSpells")
                            .forGetter(PlayerSpellData::getKnownSpells),
                    Codec.STRING.listOf()
                            .fieldOf("activeSpells")
                            .forGetter(PlayerSpellData::getActiveSpells),
                    Codec.INT.fieldOf("selectedSpellIndex")
                            .forGetter(PlayerSpellData::getSelectedSpellIndex)
            ).apply(instance, PlayerSpellData::new)
    );

    private final List<String> knownSpells;
    private final List<String> activeSpells;
    private int selectedSpellIndex;

    public PlayerSpellData() {
        this(new ArrayList<>(), new ArrayList<>(), 0);
    }

    public PlayerSpellData(List<String> knownSpells, List<String> activeSpells, int selectedSpellIndex) {
        this.knownSpells = new ArrayList<>(knownSpells);
        this.activeSpells = new ArrayList<>(activeSpells);
        this.selectedSpellIndex = 0;
        setSelectedSpellIndex(selectedSpellIndex);
    }

    public List<String> getKnownSpells() {
        return List.copyOf(knownSpells);
    }

    public List<String> getActiveSpells() {
        return List.copyOf(activeSpells);
    }

    public int getSelectedSpellIndex() {
        return selectedSpellIndex;
    }

    public boolean knowsSpell(String spellId) {
        return knownSpells.contains(spellId);
    }

    public void learnSpell(String spellId) {
        if (spellId == null || spellId.isBlank() || knowsSpell(spellId)) {
            return;
        }

        knownSpells.add(spellId);
    }

    public void forgetSpell(String spellId) {
        knownSpells.remove(spellId);
        removeActiveSpell(spellId);
    }

    public boolean addActiveSpell(String spellId, int capacity) {
        if (!knowsSpell(spellId) || activeSpells.contains(spellId)) {
            return false;
        }

        if (activeSpells.size() >= Math.min(capacity, MAX_SPELL_SLOTS)) {
            return false;
        }

        activeSpells.add(spellId);
        clampSelectedIndex();
        return true;
    }

    public boolean removeActiveSpell(String spellId) {
        int removedIndex = activeSpells.indexOf(spellId);

        if (removedIndex < 0) {
            return false;
        }

        activeSpells.remove(removedIndex);

        if (selectedSpellIndex > removedIndex) {
            selectedSpellIndex--;
        }

        clampSelectedIndex();
        return true;
    }

    public void setActiveSpells(List<String> spells, int capacity) {
        activeSpells.clear();

        int max = Math.min(capacity, MAX_SPELL_SLOTS);

        for (String spellId : spells) {
            if (activeSpells.size() >= max) {
                break;
            }

            if (knowsSpell(spellId) && !activeSpells.contains(spellId)) {
                activeSpells.add(spellId);
            }
        }

        clampSelectedIndex();
    }

    public void setSelectedSpellIndex(int index) {
        if (activeSpells.isEmpty()) {
            selectedSpellIndex = 0;
            return;
        }

        selectedSpellIndex = Mth.clamp(index, 0, activeSpells.size() - 1);
    }

    public void selectNextSpell() {
        if (activeSpells.isEmpty()) {
            return;
        }

        selectedSpellIndex = (selectedSpellIndex + 1) % activeSpells.size();
    }

    public void selectPreviousSpell() {
        if (activeSpells.isEmpty()) {
            return;
        }

        selectedSpellIndex =
                (selectedSpellIndex - 1 + activeSpells.size()) % activeSpells.size();
    }

    
    /**
     * Combines two active spell slots into one result spell.
     *
     * The two input slots are removed and the resulting spell is placed at
     * the lower of the two original positions. The player must already know
     * the resulting spell.
     */
    public boolean combineActiveSpells(int firstIndex, int secondIndex, com.sepinula.sepimod.spells.Spell result) {
        if (result == null
                || firstIndex < 0
                || secondIndex < 0
                || firstIndex >= activeSpells.size()
                || secondIndex >= activeSpells.size()
                || firstIndex == secondIndex) {
            return false;
        }

        String firstSpell = activeSpells.get(firstIndex);
        String secondSpell = activeSpells.get(secondIndex);

        if (!knowsSpell(result.id().toString())) {
            return false;
        }

        int lowerIndex = Math.min(firstIndex, secondIndex);
        int higherIndex = Math.max(firstIndex, secondIndex);

        activeSpells.remove(higherIndex);
        activeSpells.remove(lowerIndex);
        activeSpells.add(lowerIndex, result.id().toString());

        clampSelectedIndex();
        return true;
    }

    public String getSelectedSpellId() {
        if (activeSpells.isEmpty()) {
            return "";
        }

        return activeSpells.get(selectedSpellIndex);
    }

    private void clampSelectedIndex() {
        setSelectedSpellIndex(selectedSpellIndex);
    }
}
