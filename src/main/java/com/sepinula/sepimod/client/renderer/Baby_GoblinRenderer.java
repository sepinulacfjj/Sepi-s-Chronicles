package com.sepinula.sepimod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sepinula.sepimod.entity.Baby_GoblinEntity;
import com.sepinula.sepimod.client.model.Baby_GoblinModel;
import com.sepinula.sepimod.init.ModModelLayers;
import com.sepinula.sepimod.SepiMod;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class Baby_GoblinRenderer extends MobRenderer<Baby_GoblinEntity, Baby_GoblinModel<Baby_GoblinEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(SepiMod.MODID, "textures/entity/baby_goblin.png");

    public Baby_GoblinRenderer(EntityRendererProvider.Context context) {
        // Increase the shadow size (last parameter) to 0.8f or 1.0f for a bigger mob
        super(context, new Baby_GoblinModel<>(context.bakeLayer(ModModelLayers.GOBLIN_LAYER)), 0.4f);
    }

    @Override
    protected void scale(Baby_GoblinEntity entity, PoseStack poseStack, float partialTickTime) {
        // Change 2.0F to whatever size you want (1.0F is default)
        poseStack.scale(2.5F, 2.5F, 2.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(Baby_GoblinEntity entity) {
        return TEXTURE;
    }
}