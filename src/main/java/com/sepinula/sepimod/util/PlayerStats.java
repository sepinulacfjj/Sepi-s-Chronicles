package com.sepinula.sepimod.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class PlayerStats {
    public static final Codec<PlayerStats> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("archetype").forGetter(s -> s.getArchetype().name()),
                    Codec.INT.fieldOf("strength").forGetter(PlayerStats::getStrengthRaw),
                    Codec.INT.fieldOf("agility").forGetter(PlayerStats::getAgilityRaw),
                    Codec.INT.fieldOf("constitution").forGetter(PlayerStats::getConstitutionRaw),
                    Codec.INT.fieldOf("magicResistance").forGetter(PlayerStats::getMagicResistanceRaw),
                    Codec.INT.fieldOf("magicPower").forGetter(PlayerStats::getMagicPowerRaw),
                    Codec.INT.fieldOf("mana").forGetter(PlayerStats::getManaRaw),
                    Codec.INT.fieldOf("defense").forGetter(PlayerStats::getDefenseRaw),
                    Codec.INT.fieldOf("mind").forGetter(PlayerStats::getMindRaw),
                    Codec.INT.fieldOf("availablePoints").forGetter(PlayerStats::getAvailablePoints),
                    Codec.INT.fieldOf("trainingPoints").forGetter(PlayerStats::getTrainingPoints),
                    Codec.FLOAT.fieldOf("currentMana").forGetter(PlayerStats::getCurrentMana),
                    Codec.FLOAT.fieldOf("currentStamina").forGetter(PlayerStats::getCurrentStamina),
                    Codec.FLOAT.fieldOf("totalXpGained").forGetter(PlayerStats::getTotalXpGained)
            ).apply(instance, (archName, str, agi, con, res, power, man, def, mind, avail, train, curMan, curSta, xp) -> {
                RpgArchetype arch = RpgArchetype.NONE;
                try {
                    arch = RpgArchetype.valueOf(archName);
                } catch (Exception ignored) {
                }
                return new PlayerStats(arch, str, agi, con, res, power, man, def, mind, avail, train, curMan, curSta, xp);
            }));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerStats> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public PlayerStats decode(RegistryFriendlyByteBuf buffer) {
            PlayerStats stats = new PlayerStats();
            stats.setArchetype(buffer.readEnum(RpgArchetype.class));
            stats.setStrength(buffer.readInt());
            stats.setAgility(buffer.readInt());
            stats.setConstitution(buffer.readInt());
            stats.setMagicResistance(buffer.readInt());
            stats.setMagicPower(buffer.readInt());
            stats.setMana(buffer.readInt());
            stats.setDefense(buffer.readInt());
            stats.setMind(buffer.readInt());
            stats.setAvailablePoints(buffer.readInt());
            stats.setTrainingPoints(buffer.readInt());
            stats.setCurrentMana(buffer.readFloat());
            stats.setCurrentStamina(buffer.readFloat());
            stats.setTotalXpGained(buffer.readFloat());
            return stats;
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, PlayerStats stats) {
            buffer.writeEnum(stats.getArchetype());
            buffer.writeInt(stats.getStrengthRaw());
            buffer.writeInt(stats.getAgilityRaw());
            buffer.writeInt(stats.getConstitutionRaw());
            buffer.writeInt(stats.getMagicResistanceRaw());
            buffer.writeInt(stats.getMagicPowerRaw());
            buffer.writeInt(stats.getManaRaw());
            buffer.writeInt(stats.getDefenseRaw());
            buffer.writeInt(stats.getMindRaw());
            buffer.writeInt(stats.getAvailablePoints());
            buffer.writeInt(stats.getTrainingPoints());
            buffer.writeFloat(stats.getCurrentMana());
            buffer.writeFloat(stats.getCurrentStamina());
            buffer.writeFloat(stats.getTotalXpGained());
        }
    };

    private RpgArchetype archetype = RpgArchetype.NONE;
    private int strength = 0;
    private int agility = 0;
    private int constitution = 0;
    private int magicResistance = 0;
    private int magicPower = 0;
    private int mana = 0;
    private int defense = 0;
    private int mind = 0;

    private int availablePoints = 0;
    private int trainingPoints = 0;
    private float currentMana = 20.0f;
    private float currentStamina = 100.0f;
    private float totalXpGained = 0.0f;
    private int staminaTickCounter = 0;

    public PlayerStats() {}

    public PlayerStats(RpgArchetype archetype, int strength, int agility, int constitution,
                       int magicResistance, int magicPower, int mana, int defense, int mind,
                       int availablePoints, int trainingPoints, float currentMana,
                       float currentStamina, float totalXpGained) {
        this.archetype = archetype;
        this.strength = strength;
        this.agility = agility;
        this.constitution = constitution;
        this.magicResistance = magicResistance;
        this.magicPower = magicPower;
        this.mana = mana;
        this.defense = defense;
        this.mind = mind;
        this.availablePoints = availablePoints;
        this.trainingPoints = trainingPoints;
        this.currentMana = currentMana;
        this.currentStamina = currentStamina;
        this.totalXpGained = totalXpGained;
    }

    private int clampSpent(int value) {
        return Mth.clamp(value, 0, 100);
    }

    private int clampTotal(int value) {
        return Mth.clamp(value, 0, 800);
    }

    public void addXp(int amount) {
        if (trainingPoints < 800) {
            totalXpGained += amount;
            while (totalXpGained >= getXpNeededForNextPoint() && trainingPoints < 800) {
                totalXpGained -= getXpNeededForNextPoint();
                availablePoints++;
                trainingPoints++;
            }
        }
    }

    public void resetProgressAfterCap() {
        totalXpGained = 0;
    }

    public void resetAll() {
        archetype = RpgArchetype.NONE;
        strength = 0;
        agility = 0;
        constitution = 0;
        magicResistance = 0;
        magicPower = 0;
        mana = 0;
        defense = 0;
        mind = 0;
        availablePoints = 0;
        trainingPoints = 0;
        totalXpGained = 0;
        currentMana = 20.0f;
        currentStamina = 100.0f;
    }

    public float getMaxMana() {
        return 20.0f + (getMana() * 5.0f);
    }

    public float getMaxStamina() {
        return 100.0f + (getAgility() * 2.0f);
    }

    public void tickStaminaRegen(Player player) {
        if (currentStamina >= getMaxStamina()) {
            staminaTickCounter = 0;
            return;
        }

        int ticksToWait = Math.max(1, 5 - (getConstitution() / 25));
        staminaTickCounter++;

        if (staminaTickCounter >= ticksToWait) {
            float regenAmount = 1.5f + (getConstitution() * 0.1f);
            addStamina(regenAmount);
            staminaTickCounter = 0;
            if (player instanceof ServerPlayer serverPlayer) {
                ModDataAttachments.sync(serverPlayer);
            }
        }
    }

    public int getMiningHasteLevel() {
        int str = getStrength();
        if (str >= 95) return 1;
        if (str >= 50) return 0;
        return -1;
    }

    public float getXpNeededForNextPoint() {
        return 500f + (trainingPoints * 150f);
    }

    public RpgArchetype getArchetype() {
        return archetype;
    }

    public void setArchetype(RpgArchetype archetype) {
        this.archetype = archetype;
    }

    public int getStrength() {
        return clampTotal(strength + archetype.baseStrength);
    }

    public int getStrengthRaw() {
        return strength;
    }

    public void setStrength(int value) {
        strength = clampSpent(value);
    }

    public int getAgility() {
        return clampTotal(agility + archetype.baseAgility);
    }

    public int getAgilityRaw() {
        return agility;
    }

    public void setAgility(int value) {
        agility = clampSpent(value);
    }

    public int getConstitution() {
        return clampTotal(constitution + archetype.baseConstitution);
    }

    public int getConstitutionRaw() {
        return constitution;
    }

    public void setConstitution(int value) {
        constitution = clampSpent(value);
    }

    public int getMagicResistance() {
        return clampTotal(magicResistance + archetype.baseMagicResistance);
    }

    public int getMagicResistanceRaw() {
        return magicResistance;
    }

    public void setMagicResistance(int value) {
        magicResistance = clampSpent(value);
    }

    public int getMagicPower() {
        return clampTotal(magicPower + archetype.baseMagicPower);
    }

    public int getMagicPowerRaw() {
        return magicPower;
    }

    public void setMagicPower(int value) {
        magicPower = clampSpent(value);
    }

    public int getMana() {
        return clampTotal(mana + archetype.baseMana);
    }

    public int getManaRaw() {
        return mana;
    }

    public void setMana(int value) {
        mana = clampSpent(value);
    }

    public int getDefense() {
        return clampTotal(defense + archetype.baseDefense);
    }

    public int getDefenseRaw() {
        return defense;
    }

    public void setDefense(int value) {
        defense = clampSpent(value);
    }

    public int getMind() {
        return clampTotal(mind + archetype.baseMind);
    }

    public int getMindRaw() {
        return mind;
    }

    public void setMind(int value) {
        mind = clampSpent(value);
    }

    public int getAvailablePoints() {
        return availablePoints;
    }

    public void setAvailablePoints(int availablePoints) {
        this.availablePoints = availablePoints;
    }

    public int getTrainingPoints() {
        return trainingPoints;
    }

    public void setTrainingPoints(int value) {
        trainingPoints = clampTotal(value);
    }

    public float getCurrentMana() {
        return currentMana;
    }

    public void setCurrentMana(float value) {
        currentMana = Math.max(0.0f, Math.min(getMaxMana(), value));
    }

    public float getCurrentStamina() {
        return currentStamina;
    }

    public void setCurrentStamina(float value) {
        currentStamina = Math.max(0.0f, Math.min(getMaxStamina(), value));
    }

    public float getTotalXpGained() {
        return totalXpGained;
    }

    public void setTotalXpGained(float value) {
        totalXpGained = value;
    }

    public void addMana(float amount) {
        currentMana = Math.min(getMaxMana(), currentMana + amount);
    }

    public void subMana(float amount) {
        currentMana = Math.max(0, currentMana - amount);
    }

    public void addStamina(float amount) {
        currentStamina = Math.min(getMaxStamina(), currentStamina + amount);
    }

    public void subStamina(float amount) {
        currentStamina = Math.max(0, currentStamina - amount);
    }
}
