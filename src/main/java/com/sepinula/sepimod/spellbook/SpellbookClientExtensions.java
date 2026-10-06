package com.sepinula.sepimod.spellbook;

import com.sepinula.sepimod.client.renderer.SpellbookRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class SpellbookClientExtensions implements IClientItemExtensions {
    private SpellbookRenderer renderer;

    @Override
    public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
        if (renderer == null) {
            renderer = new SpellbookRenderer();
        }
        return renderer;
    }
}