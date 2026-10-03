package com.sepinula.sepimod.spellbook;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SpellbookItem extends Item {
    private final SpellbookTier tier;

    public SpellbookItem(SpellbookTier tier, Properties properties) {
        super(properties.stacksTo(1));
        this.tier = tier;
    }

    public SpellbookTier getTier() {
        return tier;
    }

    public int getSpellSlots() {
        return tier.getSpellSlots();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            player.openMenu(new SimpleMenuProvider(
                    (containerId, inventory, ignoredPlayer) ->
                            new SpellbookMenu(containerId, inventory),
                    Component.literal("Spellbook")
            ));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}