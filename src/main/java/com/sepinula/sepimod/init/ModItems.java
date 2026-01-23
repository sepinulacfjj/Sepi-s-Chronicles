package com.sepinula.sepimod.init;

import com.sepinula.sepimod.SepiMod;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SepiMod.MODID);

    public static final DeferredItem<Item> BASIC_STAFF = ITEMS.register("basic_staff",
            () -> new Item(new Item.Properties()
                    .stacksTo(1)
                    .durability(500)
                    .rarity(Rarity.EPIC)));

    // This creates the Goblin Spawn Egg
    // Colors: 0x475E3E (Dark Green base), 0xB02E26 (Red spots/eyes)
    public static final DeferredHolder<Item, Item> GOBLIN_SPAWN_EGG = ITEMS.register("goblin_spawn_egg",
            () -> new SpawnEggItem(ModEntities.GOBLIN.get(), 0x475E3E, 0xB02E26, new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}