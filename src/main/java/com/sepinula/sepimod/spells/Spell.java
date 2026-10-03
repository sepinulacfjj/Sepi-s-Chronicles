package com.sepinula.sepimod.spells;

import net.minecraft.resources.ResourceLocation;

/**
 * A definition of a spell the player can learn and place into spellbook slots.
 *
 * The spell itself is not stored on the physical spellbook item.
 */
public record Spell(ResourceLocation id, String displayName) {
}
