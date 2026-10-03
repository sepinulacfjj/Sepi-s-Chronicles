package com.sepinula.sepimod.spells;

import com.sepinula.sepimod.SepiMod;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

/**
 * Central list of spells known to Sepi's Chronicles.
 *
 * This is deliberately separate from NeoForge's item/block registries:
 * spells are gameplay definitions, not physical Minecraft registry objects.
 */
public final class SpellRegistry {

    private static final Map<ResourceLocation, Spell> SPELLS = new LinkedHashMap<>();

    public static final Spell FIREBALL = register("fireball", "Fireball");
    public static final Spell GUST = register("gust", "Gust");
    public static final Spell ICE_SHARD = register("ice_shard", "Ice Shard");
    public static final Spell FIRE_WIND = register("fire_wind", "Fire Wind");

    private SpellRegistry() {
    }

    private static Spell register(String path, String displayName) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, path);
        Spell spell = new Spell(id, displayName);
        SPELLS.put(id, spell);
        return spell;
    }

    public static Spell get(ResourceLocation id) {
        return SPELLS.get(id);
    }

    public static Collection<Spell> all() {
        return SPELLS.values();
    }
}
