package com.sepinula.sepimod.util;

public enum RpgArchetype {
    NONE("None", "§7Choose your path...", 0, 0, 0, 0, 0, 0, 0, 0),
    WARRIOR("Warrior", "§cHigh strength and defense.", 2, 1, 2, 2, 0, 0, 0, 0),
    MAGE("Mage", "§bMaster of mana and magic.", 0, 1, 0, 0, 1, 2, 2, 2),
    ROGUE("Rogue", "§eHigh agility and mobility.", 1, 3, 1, 1, 0, 0, 0, 1);

    private final String name;
    private final String description;

    public final int baseStrength;
    public final int baseAgility;
    public final int baseConstitution;
    public final int baseDefense;
    public final int baseMagicResistance;
    public final int baseMagicPower;
    public final int baseMana;
    public final int baseMind;

    RpgArchetype(String name, String description, int strength, int agility, int constitution,
                 int defense, int magicResistance, int magicPower, int mana, int mind) {
        this.name = name;
        this.description = description;
        this.baseStrength = strength;
        this.baseAgility = agility;
        this.baseConstitution = constitution;
        this.baseDefense = defense;
        this.baseMagicResistance = magicResistance;
        this.baseMagicPower = magicPower;
        this.baseMana = mana;
        this.baseMind = mind;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
