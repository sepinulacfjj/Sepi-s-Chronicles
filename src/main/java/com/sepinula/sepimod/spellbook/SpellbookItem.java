package com.sepinula.sepimod.spellbook;

import com.sepinula.sepimod.spells.SpellCooldownModifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SpellbookItem extends Item implements SpellCooldownModifier, GeoItem {
    private static final RawAnimation OPEN_ANIMATION =
            RawAnimation.begin().thenPlayAndHold("spellbook.animation.open");

    private final SpellbookTier tier;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public SpellbookItem(SpellbookTier tier, Properties properties) {
        super(properties.stacksTo(1));
        this.tier = tier;
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
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
                .triggerableAnim("open", OPEN_ANIMATION)
                .triggerableAnim("close", RawAnimation.begin().thenPlay("spellbook.animation.close")));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            // Start immediately on the rendering client so the animation is
            // visible in first person without waiting for a server sync packet.
            this.triggerAnim(player, GeoItem.getId(stack), "controller", "open");
        } else if (player instanceof ServerPlayer serverPlayer) {
            // Keep the server authoritative for opening the actual menu.
            SpellbookOpenScheduler.schedule(serverPlayer, hand, this);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
