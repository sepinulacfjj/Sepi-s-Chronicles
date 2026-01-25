package com.sepinula.sepimod.init;

import com.sepinula.sepimod.SepiMod;
import com.sepinula.sepimod.item.BasicStaffItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SepiMod.MODID);

    public static final DeferredItem<BasicStaffItem> BASIC_STAFF = ITEMS.register("basic_staff",
            () -> new BasicStaffItem(new Item.Properties() // This creates your animated item
                    .stacksTo(1)
                    .durability(500)
                    .rarity(Rarity.EPIC)));


    // FIX: Use DeferredSpawnEggItem instead of SpawnEggItem to avoid the deprecation warning
    public static final DeferredItem<Item> GOBLIN_SPAWN_EGG = ITEMS.register("goblin_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.BabyGOBLIN, 0x475E3E, 0x8B1E28, new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}