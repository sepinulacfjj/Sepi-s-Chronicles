package com.sepinula.sepimod.spellbook;

import com.sepinula.sepimod.spells.SpellCooldownModifier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SpellbookItem extends Item implements SpellCooldownModifier, GeoItem {
    private static final RawAnimation OPEN_ANIMATION =
            RawAnimation.begin().thenPlay("spellbook.animation.open");

    private final SpellbookTier tier;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

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
    public float getSpellCooldownReduction(Player player, ItemStack stack) {
        return tier.getCooldownReduction();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> PlayState.STOP)
                .triggerableAnim("open", OPEN_ANIMATION));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            long instanceId = GeoItem.getOrAssignId(stack, (ServerLevel) level);
            this.triggerAnim(player, instanceId, "controller", "open");

            player.openMenu(new SimpleMenuProvider(
                    (containerId, inventory, ignoredPlayer) ->
                            new SpellbookMenu(containerId, inventory),
                    Component.literal("Spellbook")
            ));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
