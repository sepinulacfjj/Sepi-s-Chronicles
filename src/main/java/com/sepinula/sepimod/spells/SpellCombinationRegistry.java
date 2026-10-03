package com.sepinula.sepimod.spells;

import com.sepinula.sepimod.SepiMod;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

/**
 * Central registry for spell-combination recipes.
 *
 * New combinations can be added here without changing the spellbook logic.
 */
public final class SpellCombinationRegistry {

    private static final List<SpellCombination> COMBINATIONS = new ArrayList<>();

    static {
        register(SpellRegistry.FIREBALL, SpellRegistry.GUST, SpellRegistry.FIRE_WIND);
    }

    private SpellCombinationRegistry() {
    }

    public static void register(Spell first, Spell second, Spell result) {
        COMBINATIONS.add(new SpellCombination(first.id(), second.id(), result.id()));
    }

    public static Spell getResult(ResourceLocation first, ResourceLocation second) {
        for (SpellCombination combination : COMBINATIONS) {
            if (combination.matches(first, second)) {
                return SpellRegistry.get(combination.result());
            }
        }

        return null;
    }

    public static ResourceLocation getResultId(ResourceLocation first, ResourceLocation second) {
        Spell result = getResult(first, second);
        return result == null ? null : result.id();
    }

    public static List<SpellCombination> all() {
        return List.copyOf(COMBINATIONS);
    }
}
