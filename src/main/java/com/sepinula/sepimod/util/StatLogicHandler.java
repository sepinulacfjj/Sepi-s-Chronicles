package com.sepinula.sepimod.util;

import com.sepinula.sepimod.SepiMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class StatLogicHandler {
    private static final ResourceLocation HEALTH_MOD_ID = ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "stat_health");

    public static void applyStatModifiers(Player player, PlayerStats stats) {
        // We handle Strength and Agility Speed dynamically in ModEvents now.
        // This ensures Strength bonus is only added if there's stamina,
        // and Agility FOV doesn't snap instantly.

        // Constitution -> Max Health
        var healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.removeModifier(HEALTH_MOD_ID);
            healthAttr.addTransientModifier(new AttributeModifier(HEALTH_MOD_ID,
                    (double) stats.getConstitution(), AttributeModifier.Operation.ADD_VALUE));
        }
    }
}