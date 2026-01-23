package com.sepinula.sepimod.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class PlayerStats {
    // This Codec handles NBT saving and serves as the base for the StreamCodec
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
                    Codec.INT.fieldOf("trainingPoints").forGetter(PlayerStats::getTrainingPoints)
            ).apply(instance, PlayerStats::new));

    // FIX: Use fromCodecWithRegistries because composite() has a 6-parameter limit
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerStats> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);

    private String playerClass;
    private int strength, agility, constitution, willpower, mind, mana, dexterity, charisma;
    private int availablePoints, trainingPoints;

    public PlayerStats() {
        this.playerClass = "NONE";
        this.trainingPoints = 100;
        this.availablePoints = 0;
    }

    public PlayerStats(String pc, int str, int agi, int con, int wil, int min, int man, int dex, int cha, int ap, int tp) {
        this.playerClass = pc; this.strength = str; this.agility = agi; this.constitution = con;
        this.willpower = wil; this.mind = min; this.mana = man; this.dexterity = dex;
        this.charisma = cha; this.availablePoints = ap; this.trainingPoints = tp;
    }

    public void addTrainingPoints(int points) {
        this.trainingPoints -= points;
        if (this.trainingPoints <= 0) {
            this.trainingPoints = 100;
            this.availablePoints++;
        }
    }

    // --- GETTERS AND SETTERS ---
    public String getPlayerClass() { return playerClass; }
    public void setPlayerClass(String playerClass) { this.playerClass = playerClass; }
    public int getStrength() { return strength; }
    public void setStrength(int strength) { this.strength = strength; }
    public int getAgility() { return agility; }
    public void setAgility(int agility) { this.agility = agility; }
    public int getConstitution() { return constitution; }
    public void setConstitution(int constitution) { this.constitution = constitution; }
    public int getWillpower() { return willpower; }
    public void setWillpower(int willpower) { this.willpower = willpower; }
    public int getMind() { return mind; }
    public void setMind(int mind) { this.mind = mind; }
    public int getMana() { return mana; }
    public void setMana(int mana) { this.mana = mana; }
    public int getDexterity() { return dexterity; }
    public void setDexterity(int dexterity) { this.dexterity = dexterity; }
    public int getCharisma() { return charisma; }
    public void setCharisma(int charisma) { this.charisma = charisma; }
    public int getAvailablePoints() { return availablePoints; }
    public void setAvailablePoints(int availablePoints) { this.availablePoints = availablePoints; }
    public int getTrainingPoints() { return trainingPoints; }
}