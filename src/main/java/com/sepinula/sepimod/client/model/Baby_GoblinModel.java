package com.sepinula.sepimod.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sepinula.sepimod.entity.Baby_GoblinEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class Baby_GoblinModel<T extends Baby_GoblinEntity> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart torso;
    private final ModelPart right_arm;
    private final ModelPart left_arm;
    private final ModelPart right_leg;
    private final ModelPart left_leg;

    public Baby_GoblinModel(ModelPart root) {
        this.root = root.getChild("BabyGoblin");
        this.head = this.root.getChild("Head");
        this.torso = this.root.getChild("Torso");
        this.right_arm = this.root.getChild("RightArm");
        this.left_arm = this.root.getChild("LeftArm");
        this.right_leg = this.root.getChild("RightLeg");
        this.left_leg = this.root.getChild("LeftLeg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Baby_Goblin = partdefinition.addOrReplaceChild("BabyGoblin", CubeListBuilder.create(), PartPose.offset(0.0F, 22.0F, 0.0F));

        PartDefinition Head = Baby_Goblin.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(8, 5).addBox(1.2F, -1.2F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(4, 8).addBox(-2.2F, -1.2F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.25F))
                .texOffs(6, 3).addBox(-0.5F, -0.8F, -1.4F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F)), PartPose.offset(0.0F, -2.0F, -0.5F));

        PartDefinition Torso = Baby_Goblin.addOrReplaceChild("Torso", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition RightArm = Baby_Goblin.addOrReplaceChild("RightArm", CubeListBuilder.create().texOffs(0, 8).addBox(-1.0F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, -2.0F, -0.5F));

        PartDefinition LeftArm = Baby_Goblin.addOrReplaceChild("LeftArm", CubeListBuilder.create().texOffs(6, 0).addBox(0.0F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, -2.0F, -0.5F));

        PartDefinition RightLeg = Baby_Goblin.addOrReplaceChild("RightLeg", CubeListBuilder.create().texOffs(4, 5).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 0.0F, -0.5F));

        PartDefinition LeftLeg = Baby_Goblin.addOrReplaceChild("LeftLeg", CubeListBuilder.create().texOffs(0, 5).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 0.0F, -0.5F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Reset parts to default state every frame before applying animations
        this.root().getAllParts().forEach(ModelPart::resetPose);

        // Head looking logic
        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = headPitch * ((float)Math.PI / 180F);

        // Trigger Blockbench Animations from Baby_Goblin_ModelAnimation class
        this.animate(entity.idleAnimationState, Baby_Goblin_ModelAnimation.Idle, ageInTicks);
        this.animateWalk(Baby_Goblin_ModelAnimation.Walking, limbSwing, limbSwingAmount, 2.0f, 2.5f);
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