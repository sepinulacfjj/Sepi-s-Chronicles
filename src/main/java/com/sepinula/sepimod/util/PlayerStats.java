package com.sepinula.sepimod.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class PlayerStats {
    public static final Codec<PlayerStats> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("playerClass").forGetter(PlayerStats::getPlayerClass),
                    Codec.INT.fieldOf("strength").forGetter(PlayerStats::getStrength),
                    Codec.INT.fieldOf("agility").forGetter(PlayerStats::getAgility),
                    Codec.INT.fieldOf("constitution").forGetter(PlayerStats::getConstitution),
                    Codec.INT.fieldOf("willpower").forGetter(PlayerStats::getWillpower),
                    Codec.INT.fieldOf("mind").forGetter(PlayerStats::getMind),
                    Codec.INT.fieldOf("mana").forGetter(PlayerStats::getMana),
                    Codec.INT.fieldOf("dexterity").forGetter(PlayerStats::getDexterity),
                    Codec.INT.fieldOf("charisma").forGetter(PlayerStats::getCharisma),
                    Codec.INT.fieldOf("availablePoints").forGetter(PlayerStats::getAvailablePoints),
                    Codec.INT.fieldOf("trainingPoints").forGetter(PlayerStats::getTrainingPoints),
                    // --- ADDED TO CODEC ---
                    Codec.FLOAT.fieldOf("currentMana").forGetter(PlayerStats::getCurrentMana)
            ).apply(instance, PlayerStats::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerStats> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);

    private String playerClass;
    private int strength, agility, constitution, willpower, mind, mana, dexterity, charisma;
    private int availablePoints, trainingPoints;

    // --- ADDED FIELD ---
    private float currentMana;

    public PlayerStats() {
        this.playerClass = "NONE";
        this.trainingPoints = 100;
        this.availablePoints = 0;
        this.currentMana = 20.0f; // Start with some mana
    }

    // --- UPDATED CONSTRUCTOR ---
    public PlayerStats(String pc, int str, int agi, int con, int wil, int min, int man, int dex, int cha, int ap, int tp, float cm) {
        this.playerClass = pc; this.strength = str; this.agility = agi; this.constitution = con;
        this.willpower = wil; this.mind = min; this.mana = man; this.dexterity = dex;
        this.charisma = cha; this.availablePoints = ap; this.trainingPoints = tp;
        this.currentMana = cm;
    }

    // --- CLAMPING HELPERS ---
    private int clamp(int value) {
        return Math.max(0, Math.min(value, 100));
    }

    private int clampPoints(int value) {
        return Math.max(0, Math.min(value, 999));
    }

    public void addTrainingPoints(int points) {
        this.trainingPoints -= points;
        if (this.trainingPoints <= 0) {
            this.trainingPoints = 100;
            this.setAvailablePoints(this.availablePoints + 1);
        }
    }

    // --- MANA LOGIC HELPERS ---

    /**
     * Calculates Max Mana based on the Mana Stat.
     * Base 100 + 10 per level.
     */
    public float getMaxMana() {
        return 100.0f + (this.mana * 10.0f);
    }

    public float getCurrentMana() { return currentMana; }

    public void setCurrentMana(float val) {
        // Clamp current mana between 0 and the player's current Max Mana
        this.currentMana = Math.max(0, Math.min(val, getMaxMana()));
    }

    // --- GETTERS AND SETTERS ---
    public String getPlayerClass() { return playerClass; }
    public void setPlayerClass(String playerClass) { this.playerClass = playerClass; }

    public int getStrength() { return strength; }
    public void setStrength(int val) { this.strength = clamp(val); }

    public int getAgility() { return agility; }
    public void setAgility(int val) { this.agility = clamp(val); }

    public int getConstitution() { return constitution; }
    public void setConstitution(int val) { this.constitution = clamp(val); }

    public int getWillpower() { return willpower; }
    public void setWillpower(int val) { this.willpower = clamp(val); }

    public int getMind() { return mind; }
    public void setMind(int val) { this.mind = clamp(val); }

    public int getMana() { return mana; }
    public void setMana(int val) { this.mana = clamp(val); }

    public int getDexterity() { return dexterity; }
    public void setDexterity(int val) { this.dexterity = clamp(val); }

    public int getCharisma() { return charisma; }
    public void setCharisma(int val) { this.charisma = clamp(val); }

    public int getAvailablePoints() { return availablePoints; }
    public void setAvailablePoints(int availablePoints) { this.availablePoints = clampPoints(availablePoints); }

    public int getTrainingPoints() { return trainingPoints; }
}