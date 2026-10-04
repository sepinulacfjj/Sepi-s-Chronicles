package com.sepinula.sepimod.init;

import com.sepinula.sepimod.SepiMod;
import com.sepinula.sepimod.item.BasicStaffItem;
import com.sepinula.sepimod.spellbook.SpellbookItem;
import com.sepinula.sepimod.spellbook.SpellbookTier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SepiMod.MODID);

    public static final DeferredItem<SpellbookItem> SPELLBOOK = ITEMS.register("spellbook",
            () -> new SpellbookItem(SpellbookTier.ORIGINAL, new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<SpellbookItem> COPPER_SPELLBOOK = ITEMS.register("copper_spellbook",
            () -> new SpellbookItem(SpellbookTier.COPPER, new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<SpellbookItem> IRON_SPELLBOOK = ITEMS.register("iron_spellbook",
            () -> new SpellbookItem(SpellbookTier.IRON, new Item.Properties().rarity(Rarity.RARE)));

    public static final DeferredItem<SpellbookItem> GOLD_SPELLBOOK = ITEMS.register("gold_spellbook",
            () -> new SpellbookItem(SpellbookTier.GOLD, new Item.Properties().rarity(Rarity.RARE)));

    public static final DeferredItem<SpellbookItem> DIAMOND_SPELLBOOK = ITEMS.register("diamond_spellbook",
            () -> new SpellbookItem(SpellbookTier.DIAMOND, new Item.Properties().rarity(Rarity.EPIC)));

    public static final DeferredItem<SpellbookItem> NETHERITE_SPELLBOOK = ITEMS.register("netherite_spellbook",
            () -> new SpellbookItem(SpellbookTier.NETHERITE, new Item.Properties().rarity(Rarity.EPIC)));

    public static final DeferredItem<BasicStaffItem> BASIC_STAFF = ITEMS.register("basic_staff",
            () -> new BasicStaffItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(500)
                    .rarity(Rarity.EPIC)));

    public static final DeferredItem<Item> GOBLIN_SPAWN_EGG = ITEMS.register("goblin_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.BabyGOBLIN, 0x475E3E, 0x8B1E28, new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
