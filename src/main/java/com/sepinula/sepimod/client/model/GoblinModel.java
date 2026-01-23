package com.sepinula.sepimod.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sepinula.sepimod.entity.GoblinEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class GoblinModel<T extends GoblinEntity> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart Head;
    private final ModelPart Torso;
    private final ModelPart RightArm;
    private final ModelPart LeftArm;
    private final ModelPart RigthLeg;
    private final ModelPart LeftLeg;

    public GoblinModel(ModelPart root) {
        this.root = root.getChild("Goblin");
        this.Head = this.root.getChild("Head");
        this.Torso = this.root.getChild("Torso");
        this.RightArm = this.root.getChild("RightArm");
        this.LeftArm = this.root.getChild("LeftArm");
        this.RigthLeg = this.root.getChild("RigthLeg");
        this.LeftLeg = this.root.getChild("LeftLeg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Goblin = partdefinition.addOrReplaceChild("Goblin", CubeListBuilder.create(), PartPose.offset(0.0F, 22.0F, 0.0F));

        PartDefinition Head = Goblin.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(8, 5).addBox(1.2F, -1.2F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(4, 8).addBox(-2.2F, -1.2F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.25F))
                .texOffs(6, 3).addBox(-0.5F, -0.8F, -1.4F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F)), PartPose.offset(0.0F, -2.0F, -0.5F));

        PartDefinition Torso = Goblin.addOrReplaceChild("Torso", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition RightArm = Goblin.addOrReplaceChild("RightArm", CubeListBuilder.create().texOffs(0, 8).addBox(-1.0F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, -2.0F, -0.5F));

        PartDefinition LeftArm = Goblin.addOrReplaceChild("LeftArm", CubeListBuilder.create().texOffs(6, 0).addBox(0.0F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, -2.0F, -0.5F));

        PartDefinition RigthLeg = Goblin.addOrReplaceChild("RigthLeg", CubeListBuilder.create().texOffs(4, 5).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 0.0F, -0.5F));

        PartDefinition LeftLeg = Goblin.addOrReplaceChild("LeftLeg", CubeListBuilder.create().texOffs(0, 5).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 0.0F, -0.5F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Reset parts to default state every frame before applying animations
        this.root().getAllParts().forEach(ModelPart::resetPose);

        // Head looking logic
        this.Head.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.Head.xRot = headPitch * ((float)Math.PI / 180F);

        // Trigger Blockbench Animations from BabyGoblin_ModelAnimation class
        this.animate(entity.idleAnimationState, BabyGoblin_ModelAnimation.Idle, ageInTicks);
        this.animateWalk(BabyGoblin_ModelAnimation.Walking, limbSwing, limbSwingAmount, 2.0f, 2.5f);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}