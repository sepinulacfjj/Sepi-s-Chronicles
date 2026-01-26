package com.sepinula.sepimod.util;

public enum RpgArchetype {
    // Parameters: Name, Description, Str, Agi, Con, Def, Wil, Mnd, Mana, Cha
    NONE("None", "§7Choose your path...", 0, 0, 0, 0, 0, 0, 0, 0),
    WARRIOR("Warrior", "§cHigh strength and defense.", 2, 1, 2, 2, 0, 0, 0, 0),
    MAGE("Mage", "§bMaster of mana and mind.", 0, 1, 0, 0, 2, 2, 2, 0),
    ROGUE("Rogue", "§eHigh agility and criticals.", 1,3, 1, 1, 0, 0, 0, 1);

    private final String name;
    private final String description;

    // Base stats provided by the class
    public final int baseStr, baseAgi, baseCon, baseDef, baseWil, baseMnd, baseMana, baseCha;

    RpgArchetype(String name, String description, int str, int agi, int con, int def, int wil, int mnd, int mana, int cha) {
        this.name = name;
        this.description = description;
        this.baseStr = str;
        this.baseAgi = agi;
        this.baseCon = con;
        this.baseDef = def;
        this.baseWil = wil;
        this.baseMnd = mnd;
        this.baseMana = mana;
        this.baseCha = cha;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
}