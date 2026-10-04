package com.sepinula.sepimod.spells;

import com.sepinula.sepimod.spellbook.SpellbookHelper;
import com.sepinula.sepimod.spellbook.SpellbookItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Calculates the final cooldown a player experiences for a spell.
 *
 * All equipment-based reductions are collected here so casting, HUD and the
 * spellbook information page always use exactly the same calculation.
 */
public final class SpellCooldownHelper {

    private SpellCooldownHelper() {
    }

    public static int getEffectiveCooldownTicks(Player player, Spell spell) {
        int baseCooldown = Math.max(0, spell.cooldownTicks());
        float reduction = getCooldownReduction(player);
        return Math.max(1, Math.round(baseCooldown * (1.0F - reduction)));
    }

    public static float getCooldownReductionFromSpellbook(ItemStack spellbook) {
        if (!spellbook.isEmpty() && spellbook.getItem() instanceof SpellCooldownModifier modifier) {
            return Math.min(0.75F, Math.max(0.0F, modifier.getSpellCooldownReduction(null, spellbook)));
        }
        return 0.0F;
    }

    public static float getCooldownReduction(Player player) {
        float reduction = 0.0F;

        ItemStack spellbook = SpellbookHelper.getSpellbook(player);
        if (!spellbook.isEmpty() && spellbook.getItem() instanceof SpellCooldownModifier modifier) {
            reduction += modifier.getSpellCooldownReduction(player, spellbook);
        }

        for (ItemStack armor : player.getArmorSlots()) {
            if (armor.getItem() instanceof SpellCooldownModifier modifier) {
                reduction += modifier.getSpellCooldownReduction(player, armor);
            }
        }

        return Math.min(0.75F, Math.max(0.0F, reduction));
    }

    public static int getEffectiveCooldownTicks(Player player, int baseCooldownTicks) {
        float reduction = getCooldownReduction(player);
        return Math.max(1, Math.round(Math.max(0, baseCooldownTicks) * (1.0F - reduction)));
    }

    public static String formatReduction(float reduction) {
        return Math.round(reduction * 100.0F) + "%";
    }
}
