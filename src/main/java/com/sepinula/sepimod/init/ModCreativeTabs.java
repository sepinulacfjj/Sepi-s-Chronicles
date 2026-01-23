package com.sepinula.sepimod.init;

import com.sepinula.sepimod.SepiMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SepiMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SEPI_TAB = CREATIVE_MODE_TABS.register("sepi_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.BASIC_STAFF.get()))
                    .title(Component.translatable("creativetab.sepimod"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.BASIC_STAFF.get());
                        output.accept(ModItems.GOBLIN_SPAWN_EGG.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}