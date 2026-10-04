package com.sepinula.sepimod.spells;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Implemented by equipment that modifies spell cooldowns.
 *
 * Values are additive percentage reductions expressed from 0.0 to 1.0.
 * For example, 0.10F means 10% cooldown reduction.
 */
public interface SpellCooldownModifier {

    float getSpellCooldownReduction(Player player, ItemStack stack);
}
