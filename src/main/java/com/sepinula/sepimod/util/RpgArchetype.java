package com.sepinula.sepimod.util;

public enum RpgArchetype {
    NONE("None", "§7Choose your path...", 0, 0, 0),
    WARRIOR("Warrior", "§cHigh strength and defense.", 5, 2, 5),
    MAGE("Mage", "§bMaster of mana and mind.", 0, 8, 2),
    ROGUE("Rogue", "§eHigh agility and criticals.", 3, 0,0);

    private final String name;
    private final String description;
    public final int baseStr, baseMana, baseDef;

    RpgArchetype(String name, String description, int str, int mana, int def) {
        this.name = name;
        this.description = description;
        this.baseStr = str;
        this.baseMana = mana;
        this.baseDef = def;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
}