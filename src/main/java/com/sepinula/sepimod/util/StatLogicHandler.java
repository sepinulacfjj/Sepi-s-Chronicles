package com.sepinula.sepimod.util;

import com.sepinula.sepimod.SepiMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class StatLogicHandler {
    private static final ResourceLocation HEALTH_MOD_ID =
            ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "stat_health");
    private static final ResourceLocation KB_MOD_ID =
            ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "stat_kb_res");

    private StatLogicHandler() {}

    public static void applyStatModifiers(Player player, PlayerStats stats) {
        var healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.removeModifier(HEALTH_MOD_ID);
            healthAttr.addTransientModifier(new AttributeModifier(
                    HEALTH_MOD_ID,
                    (double) stats.getConstitution(),
                    AttributeModifier.Operation.ADD_VALUE
            ));
        }

        var kbAttr = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kbAttr != null) {
            kbAttr.removeModifier(KB_MOD_ID);
            kbAttr.addTransientModifier(new AttributeModifier(
                    KB_MOD_ID,
                    stats.getDefense() * 0.005D,
                    AttributeModifier.Operation.ADD_VALUE
            ));
        }
    }

    /** 100 Magic Power = 2x spell damage/effect strength. */
    public static float getMagicPowerMultiplier(PlayerStats stats) {
        return 1.0F + (stats.getMagicPower() * 0.01F);
    }

    /** 100 Magic Resistance = 50% less magic/curse-type damage. */
    public static float getMagicDamageMultiplier(PlayerStats stats) {
        return 1.0F - Math.min(0.50F, stats.getMagicResistance() * 0.005F);
    }

    /** 100 Magic Resistance = 75% chance to reject harmful status effects. */
    public static double getStatusResistanceChance(PlayerStats stats) {
        return Math.min(0.75D, stats.getMagicResistance() * 0.0075D);
    }
}
