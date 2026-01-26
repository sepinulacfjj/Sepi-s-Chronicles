package com.sepinula.sepimod.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffects;

public class PlayerStats {
    public static final Codec<PlayerStats> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("archetype").forGetter(s -> s.getArchetype().name()),
                    Codec.INT.fieldOf("strength").forGetter(PlayerStats::getStrengthRaw),
                    Codec.INT.fieldOf("agility").forGetter(PlayerStats::getAgilityRaw),
                    Codec.INT.fieldOf("constitution").forGetter(PlayerStats::getConstitution),
                    Codec.INT.fieldOf("willpower").forGetter(PlayerStats::getWillpowerRaw),
                    Codec.INT.fieldOf("mind").forGetter(PlayerStats::getMindRaw),
                    Codec.INT.fieldOf("mana").forGetter(PlayerStats::getManaRaw),
                    Codec.INT.fieldOf("defense").forGetter(PlayerStats::getDefenseRaw),
                    Codec.INT.fieldOf("charisma").forGetter(PlayerStats::getCharisma),
                    Codec.INT.fieldOf("availablePoints").forGetter(PlayerStats::getAvailablePoints),
                    Codec.INT.fieldOf("trainingPoints").forGetter(PlayerStats::getTrainingPoints),
                    Codec.FLOAT.fieldOf("currentMana").forGetter(PlayerStats::getCurrentMana),
                    Codec.FLOAT.fieldOf("currentStamina").forGetter(PlayerStats::getCurrentStamina),
                    Codec.FLOAT.fieldOf("totalXpGained").forGetter(PlayerStats::getTotalXpGained)
            ).apply(instance, (archName, str, agi, con, wil, min, man, def, cha, avail, train, curMan, curSta, xp) -> {
                RpgArchetype arch = RpgArchetype.NONE;
                try { arch = RpgArchetype.valueOf(archName); } catch (Exception e) {}
                return new PlayerStats(arch, str, agi, con, wil, min, man, def, cha, avail, train, curMan, curSta, xp);
            }));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerStats> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public PlayerStats decode(RegistryFriendlyByteBuf buffer) {
            PlayerStats stats = new PlayerStats();
            stats.setArchetype(buffer.readEnum(RpgArchetype.class));
            stats.setStrength(buffer.readInt());
            stats.setAgility(buffer.readInt());
            stats.setConstitution(buffer.readInt());
            stats.setWillpower(buffer.readInt());
            stats.setMind(buffer.readInt());
            stats.setMana(buffer.readInt());
            stats.setDefense(buffer.readInt());
            stats.setCharisma(buffer.readInt());
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
            buffer.writeInt(stats.getConstitution());
            buffer.writeInt(stats.getWillpowerRaw());
            buffer.writeInt(stats.getMindRaw());
            buffer.writeInt(stats.getManaRaw());
            buffer.writeInt(stats.getDefenseRaw());
            buffer.writeInt(stats.getCharisma());
            buffer.writeInt(stats.getAvailablePoints());
            buffer.writeInt(stats.getTrainingPoints());
            buffer.writeFloat(stats.getCurrentMana());
            buffer.writeFloat(stats.getCurrentStamina());
            buffer.writeFloat(stats.getTotalXpGained());
        }
    };

    private RpgArchetype archetype = RpgArchetype.NONE;
    private int strength = 0, agility = 0, constitution = 0, willpower = 0, mind = 0, mana = 0, defense = 0, charisma = 0;
    private int availablePoints = 0;
    private int trainingPoints = 0;
    private float currentMana = 20.0f;
    private float currentStamina = 100.0f;
    private float totalXpGained = 0.0f;
    private int staminaTickCounter = 0;

    public PlayerStats() {}

    public PlayerStats(RpgArchetype archetype, int str, int agi, int con, int wil, int min, int man, int def, int cha, int avail, int train, float curMan, float curSta, float xp) {
        this.archetype = archetype;
        this.strength = str;
        this.agility = agi;
        this.constitution = con;
        this.willpower = wil;
        this.mind = min;
        this.mana = man;
        this.defense = def;
        this.charisma = cha;
        this.availablePoints = avail;
        this.trainingPoints = train;
        this.currentMana = curMan;
        this.currentStamina = curSta;
        this.totalXpGained = xp;
    }

    private int clampSpent(int val) { return Mth.clamp(val, 0, 100); }
    private int clampTotal(int val) { return Mth.clamp(val, 0, 800); }

    public void addXp(int amount) {
        if (this.trainingPoints < 800) {
            this.totalXpGained += amount;
            while (this.totalXpGained >= getXpNeededForNextPoint() && this.trainingPoints < 800) {
                this.totalXpGained -= getXpNeededForNextPoint();
                this.availablePoints++;
                this.trainingPoints++;
            }
        }
    }

    public void resetProgressAfterCap() { this.totalXpGained = 0; }

    public void resetAll() {
        this.archetype = RpgArchetype.NONE;
        this.strength = 0; this.agility = 0; this.constitution = 0;
        this.willpower = 0; this.mind = 0; this.mana = 0;
        this.defense = 0; this.charisma = 0; this.availablePoints = 0;
        this.trainingPoints = 0; this.totalXpGained = 0;
        this.currentMana = 20.0f; this.currentStamina = 100.0f;
    }

    public float getMaxMana() { return 20.0f + (getMana() * 5.0f); }
    public float getMaxStamina() { return 100.0f + (getAgility() * 2.0f); }

    public void tickStaminaRegen(Player player) {
        if (player.hasEffect(MobEffects.HUNGER) || player.getFoodData().getFoodLevel() < 6) {
            staminaTickCounter = 0;
            return;
        }
        if (this.currentStamina >= getMaxStamina()) {
            staminaTickCounter = 0;
            return;
        }
        int ticksToWait = Math.max(2, 40 - (getConstitution() * 38 / 100));
        staminaTickCounter++;
        if (staminaTickCounter >= ticksToWait) {
            this.addStamina(1.0f);
            staminaTickCounter = 0;
            if (player instanceof ServerPlayer sp) ModDataAttachments.sync(sp);
        }
    }

    public int getMiningHasteLevel() {
        int str = getStrength();
        if (str >= 95) return 1;
        if (str >= 50) return 0;
        return -1;
    }

    public float getXpNeededForNextPoint() { return 500f + (this.trainingPoints * 150f); }

    public RpgArchetype getArchetype() { return archetype; }
    public void setArchetype(RpgArchetype archetype) { this.archetype = archetype; }

    public int getStrength() { return clampTotal(strength + archetype.baseStr); }
    public int getStrengthRaw() { return strength; }
    public void setStrength(int val) { this.strength = clampSpent(val); }

    public int getAgility() { return clampTotal(agility + archetype.baseAgi); }
    public int getAgilityRaw() { return agility; }
    public void setAgility(int val) { this.agility = clampSpent(val); }

    public int getConstitution() { return clampSpent(constitution); }
    public void setConstitution(int val) { this.constitution = clampSpent(val); }

    public int getWillpower() { return clampTotal(willpower + archetype.baseWil); }
    public int getWillpowerRaw() { return willpower; }
    public void setWillpower(int val) { this.willpower = clampSpent(val); }

    public int getMind() { return clampTotal(mind + archetype.baseMnd); }
    public int getMindRaw() { return mind; }
    public void setMind(int val) { this.mind = clampSpent(val); }

    public int getMana() { return clampTotal(mana + archetype.baseMana); }
    public int getManaRaw() { return mana; }
    public void setMana(int val) { this.mana = clampSpent(val); }

    public int getDefense() { return clampTotal(defense + archetype.baseDef); }
    public int getDefenseRaw() { return defense; }
    public void setDefense(int val) { this.defense = clampSpent(val); }

    public int getCharisma() { return clampSpent(charisma); }
    public void setCharisma(int val) { this.charisma = clampSpent(val); }

    public int getAvailablePoints() { return availablePoints; }
    public void setAvailablePoints(int availablePoints) { this.availablePoints = availablePoints; }

    public int getTrainingPoints() { return trainingPoints; }
    public void setTrainingPoints(int val) { this.trainingPoints = clampTotal(val); }

    public float getCurrentMana() { return currentMana; }
    public void setCurrentMana(float val) { this.currentMana = val; }

    public float getCurrentStamina() { return currentStamina; }
    public void setCurrentStamina(float val) { this.currentStamina = val; }

    public float getTotalXpGained() { return totalXpGained; }
    public void setTotalXpGained(float val) { this.totalXpGained = val; }

    public void addMana(float amount) { this.currentMana = Math.min(getMaxMana(), this.currentMana + amount); }
    public void subMana(float amount) { this.currentMana = Math.max(0, this.currentMana - amount); }
    public void addStamina(float amount) { this.currentStamina = Math.min(getMaxStamina(), this.currentStamina + amount); }
    public void subStamina(float amount) { this.currentStamina = Math.max(0, this.currentStamina - amount); }
}