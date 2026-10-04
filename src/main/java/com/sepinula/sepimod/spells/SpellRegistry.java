package com.sepinula.sepimod.spells;

import com.sepinula.sepimod.SepiMod;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public final class SpellRegistry {

    private static final Map<ResourceLocation, Spell> SPELLS = new LinkedHashMap<>();

    public static final Spell FIREBALL = register(
            "fireball",
            "Fireball",
            "Launches a powerful ball of fire at your target.",
            15
    );

    public static final Spell GUST = register(
            "gust",
            "Gust",
            "Unleashes a burst of wind that pushes nearby creatures away.",
            10
    );

    public static final Spell ICE_SHARD = register(
            "ice_shard",
            "Ice Shard",
            "Fires a freezing shard that damages a nearby target.",
            12
    );

    public static final Spell FIRE_WIND = register(
            "fire_wind",
            "Fire Wind",
            "Combines flame and wind into a devastating magical attack.",
            25
    );

    private SpellRegistry() {
    }

    private static Spell register(
            String path,
            String displayName,
            String description,
            int manaCost
    ) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, path);
        Spell spell = new Spell(id, displayName, description, manaCost);
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
