package com.sepinula.sepimod.init;

import com.sepinula.sepimod.SepiMod;
import com.sepinula.sepimod.spellbook.SpellbookMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.world.flag.FeatureFlags;

public final class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, SepiMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<SpellbookMenu>> SPELLBOOK =
            MENUS.register("spellbook", () -> new MenuType<>(SpellbookMenu::new, FeatureFlags.DEFAULT_FLAGS));

    private ModMenus() {
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
