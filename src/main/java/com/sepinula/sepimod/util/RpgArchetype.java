package com.sepinula.sepimod.util;

public enum RpgArchetype {
    NONE("None", "§7Choose your path...", 0, 0, 0, 0, 0, 0),
    WARRIOR("Warrior", "§cHigh strength and defense.", 3, 0, 2, 0, 0, 0),
    MAGE("Mage", "§bMaster of mana and mind.", 0, 0, 0, 3, 2, 0),
    ROGUE("Rogue", "§eHigh agility and criticals.", 2, 3, 0, 0, 0, 0);

    private final String name;
    private final String description;

    // Base stats provided by the class
    public final int baseStr, baseAgi, baseDef, baseWil, baseMnd, baseMana;

    RpgArchetype(String name, String description, int str, int agi, int def, int wil, int mnd, int mana) {
        this.name = name;
        this.description = description;
        this.baseStr = str;
        this.baseAgi = agi;
        this.baseDef = def;
        this.baseWil = wil;
        this.baseMnd = mnd;
        this.baseMana = mana;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
}