package com.sepinula.sepimod.util;

import com.sepinula.sepimod.SepiMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class StatLogicHandler {
    private static final ResourceLocation HEALTH_MOD_ID = ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "stat_health");
    private static final ResourceLocation KB_MOD_ID = ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "stat_kb_res");

    public static void applyStatModifiers(Player player, PlayerStats stats) {
        // Constitution -> Max Health
        var healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.removeModifier(HEALTH_MOD_ID);
            healthAttr.addTransientModifier(new AttributeModifier(HEALTH_MOD_ID,
                    (double) stats.getConstitution(), AttributeModifier.Operation.ADD_VALUE));
        }

        // Defense -> Knockback Resistance (0.005 per point = 0.5 or 50% resistance at 100 DEF)
        var kbAttr = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kbAttr != null) {
            kbAttr.removeModifier(KB_MOD_ID);
            kbAttr.addTransientModifier(new AttributeModifier(KB_MOD_ID,
                    stats.getDefense() * 0.005, AttributeModifier.Operation.ADD_VALUE));
        }
    }
}