package com.sepinula.sepimod;

import com.sepinula.sepimod.init.ModCreativeTabs;
import com.sepinula.sepimod.init.ModEntities;
import com.sepinula.sepimod.init.ModItems;
import com.sepinula.sepimod.util.ModCommands;
import com.sepinula.sepimod.util.ModDataAttachments;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(SepiMod.MODID)
public class SepiMod {
    public static final String MODID = "sepimod";

    public SepiMod(IEventBus modEventBus) {
        ModEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModDataAttachments.register(modEventBus);

        // Register Commands to the Game Bus
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }
}