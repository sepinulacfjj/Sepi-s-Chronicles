package com.sepinula.sepimod.util;

import com.sepinula.sepimod.SepiMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class StatLogicHandler {
    private static final ResourceLocation STRENGTH_MOD_ID = ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "stat_strength");
    private static final ResourceLocation AGILITY_MOD_ID = ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "stat_agility");
    private static final ResourceLocation HEALTH_MOD_ID = ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "stat_health");

    public static void applyStatModifiers(Player player, PlayerStats stats) {
        // Strength -> Damage
        var attackAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr != null) {
            attackAttr.removeModifier(STRENGTH_MOD_ID);
            attackAttr.addTransientModifier(new AttributeModifier(STRENGTH_MOD_ID,
                    stats.getStrength() * 0.5D, AttributeModifier.Operation.ADD_VALUE));
        }

        // Agility -> Speed
        var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(AGILITY_MOD_ID);
            speedAttr.addTransientModifier(new AttributeModifier(AGILITY_MOD_ID,
                    stats.getAgility() * 0.01D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }

        // Constitution -> Health
        var healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.removeModifier(HEALTH_MOD_ID);
            healthAttr.addTransientModifier(new AttributeModifier(HEALTH_MOD_ID,
                    (double) stats.getConstitution(), AttributeModifier.Operation.ADD_VALUE));
        }

        // DEXTERITY: We removed the Armor Attribute here to hide the GUI icons.
    }
}