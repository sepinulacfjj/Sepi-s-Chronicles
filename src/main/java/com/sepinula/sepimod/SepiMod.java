package com.sepinula.sepimod;

import com.sepinula.sepimod.init.*;
import com.sepinula.sepimod.item.BasicStaffClientExtensions;
import com.sepinula.sepimod.util.ModCommands;
import com.sepinula.sepimod.util.ModDataAttachments;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(SepiMod.MODID)
public class SepiMod {
    public static final String MODID = "sepimod";

    public SepiMod(IEventBus modEventBus) {
        // Registry setup
        ModEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModDataAttachments.register(modEventBus);

        // Client Setup (Connects the 3D Model)
        modEventBus.addListener(this::registerClientExtensions);

        // Server Setup (Commands)
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
    }

    private void registerClientExtensions(RegisterClientExtensionsEvent event) {
        // Link the Item to its 3D Extension
        event.registerItem(new BasicStaffClientExtensions(), ModItems.BASIC_STAFF);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }
}