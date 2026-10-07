package com.sepinula.sepimod.client.renderer;

import com.sepinula.sepimod.client.SpellbookPreviewItem;
import com.sepinula.sepimod.client.event.ClientEvents;
import com.sepinula.sepimod.spellbook.SpellbookItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.vertex.PoseStack;

public class SpellbookPreviewLayer extends ItemInHandLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public SpellbookPreviewLayer(
            RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer,
            ItemInHandRenderer itemInHandRenderer
    ) {
        super(renderer, itemInHandRenderer);
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            AbstractClientPlayer player,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        Minecraft mc = Minecraft.getInstance();

        if (player != mc.player || mc.options.getCameraType().isFirstPerson()) {
            return;
        }

        ItemStack previewStack = ClientEvents.getSpellbookPreviewStack();
        if (previewStack.isEmpty()) {
            return;
        }

        // Let vanilla perform the exact third-person hand transform, then
        // render our separate preview stack instead of the player's real item.
        renderArmWithItem(
                player,
                previewStack,
                ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                HumanoidArm.RIGHT,
                poseStack,
                buffer,
                packedLight
        );
    }
}
