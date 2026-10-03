package com.sepinula.sepimod.spells;

import net.minecraft.resources.ResourceLocation;

/**
 * Describes one recipe for combining two known spells into another spell.
 *
 * The order of the two input spells does not matter.
 */
public record SpellCombination(ResourceLocation first, ResourceLocation second, ResourceLocation result) {

    public boolean matches(ResourceLocation a, ResourceLocation b) {
        return (first.equals(a) && second.equals(b))
                || (first.equals(b) && second.equals(a));
    }
}
