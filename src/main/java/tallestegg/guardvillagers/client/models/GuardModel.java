package tallestegg.guardvillagers.client.models;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemUseAnimation;
import tallestegg.guardvillagers.client.renderer.state.GuardRenderState;

public class GuardModel extends HumanoidModel<GuardRenderState> {
    public final ModelPart nose = this.head.getChild("nose");
    public final ModelPart quiver = this.body.getChild("quiver");
    public final ModelPart armLShoulderPad = this.rightArm.getChild("shoulderPad_left");
    public final ModelPart armRShoulderPad = this.leftArm.getChild("shoulderPad_right");

    public GuardModel(ModelPart part) {
        super(part);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition torso = partdefinition.addOrReplaceChild(
                "body",
                CubeListBuilder.create().texOffs(52, 50)
                        .addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, new CubeDeformation(0.25F)),
                PartPose.offset(0.0F, 0.0F, 0.0F)
        );

        PartDefinition head = partdefinition.addOrReplaceChild(
                "head",
                CubeListBuilder.create().texOffs(49, 99)
                        .addBox(-4.0F, -10.0F, -4.0F, 8, 10, 8, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 1.0F, 0.0F)
        );

        PartDefinition rightArm = partdefinition.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create().texOffs(32, 75)
                        .mirror().addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, new CubeDeformation(0.0F)),
                PartPose.offset(-5.0F, 2.0F, 0.0F)
        );

        PartDefinition leftArm = partdefinition.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create().texOffs(33, 48)
                        .addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, new CubeDeformation(0.0F)),
                PartPose.offset(5.0F, 2.0F, 0.0F)
        );

        torso.addOrReplaceChild(
                "quiver",
                CubeListBuilder.create().texOffs(100, 0)
                        .addBox(-2.5F, -2.0F, 0.0F, 5, 10, 5, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.5F, 3.0F, 2.3F, 0.0F, 0.0F, 0.2617993877991494F)
        );

        head.addOrReplaceChild(
                "nose",
                CubeListBuilder.create().texOffs(54, 0)
                        .addBox(-1.0F, 0.0F, -2.0F, 2, 4, 2, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -3.0F, -4.0F)
        );

        partdefinition.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create().texOffs(16, 48)
                        .mirror().addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, new CubeDeformation(0.0F)),
                PartPose.offset(-1.9F, 12.0F, 0.0F)
        );

        partdefinition.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create().texOffs(16, 28)
                        .addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, new CubeDeformation(0.0F)),
                PartPose.offset(1.9F, 12.0F, 0.0F)
        );

        leftArm.addOrReplaceChild(
                "shoulderPad_right",
                CubeListBuilder.create().texOffs(72, 33)
                        .mirror().addBox(0.0F, 0.0F, -3.0F, 5, 3, 6, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.5F, -3.5F, 0.0F, 0.0F, 0.0F, 0.3490658503988659F)
        );

        rightArm.addOrReplaceChild(
                "shoulderPad_left",
                CubeListBuilder.create().texOffs(72, 33)
                        .addBox(-5.0F, 0.0F, -3.0F, 5, 3, 6, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.5F, -3.5F, 0.0F, 0.0F, 0.0F, -0.3490658503988659F)
        );

        partdefinition.addOrReplaceChild(
                "hat",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-4.5F, -11.0F, -4.5F, 9, 11, 9, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 1.0F, 0.0F)
        );

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(GuardRenderState state) {
        super.setupAnim(state);
        applyArmPoses(state);

        this.quiver.visible = state.showQuiver;
        this.armLShoulderPad.visible = state.showShoulderPads;
        this.armRShoulderPad.visible = state.showShoulderPads;

        if (state.kickTicks > 0) {
            float f1 = 1.0F - (float) Mth.abs(10 - 2 * state.kickTicks) / 10.0F;
            this.rightLeg.xRot = Mth.lerp(f1, this.rightLeg.xRot, -1.40F);
        }

        if (state.aggressive && !state.holdingShootable && !state.mainHandEmpty && !state.blocking) {
            this.holdWeaponHigh(state.mainArm);
        }

        if (state.mainArm == HumanoidArm.RIGHT) {
            this.eatingAnimationRightHand(state.mainHandUseAnimation, state.eating, state.ageInTicks);
            this.eatingAnimationLeftHand(state.offHandUseAnimation, state.eating, state.ageInTicks);
        } else {
            this.eatingAnimationRightHand(state.offHandUseAnimation, state.eating, state.ageInTicks);
            this.eatingAnimationLeftHand(state.mainHandUseAnimation, state.eating, state.ageInTicks);
        }

        this.hat.visible = this.head.visible;
        this.hat.copyFrom(this.head);

        this.hat.x = this.head.x;
        this.hat.y = this.head.y;
        this.hat.z = this.head.z;
        this.hat.xRot = this.head.xRot;
        this.hat.yRot = this.head.yRot;
        this.hat.zRot = this.head.zRot;
    }

    private void applyArmPoses(GuardRenderState state) {
        if (state.rightArmPose == HumanoidModel.ArmPose.BOW_AND_ARROW
                || state.leftArmPose == HumanoidModel.ArmPose.BOW_AND_ARROW) {

            boolean rightHanded = state.mainArm == HumanoidArm.RIGHT;
            ModelPart draw = rightHanded ? this.rightArm : this.leftArm;
            ModelPart hold = rightHanded ? this.leftArm : this.rightArm;

            draw.yRot = -0.1F + this.head.yRot;
            hold.yRot =  0.1F + this.head.yRot + 0.4F;
            draw.xRot = -(float)Math.PI / 2F + this.head.xRot;
            hold.xRot = -(float)Math.PI / 2F + this.head.xRot;
            return;
        }

        // Crossbow hold
        if (state.rightArmPose == HumanoidModel.ArmPose.CROSSBOW_HOLD
                || state.leftArmPose == HumanoidModel.ArmPose.CROSSBOW_HOLD) {

            boolean rightHanded = state.mainArm == HumanoidArm.RIGHT;
            ModelPart main = rightHanded ? this.rightArm : this.leftArm;
            ModelPart off  = rightHanded ? this.leftArm  : this.rightArm;

            main.yRot = -0.3F + this.head.yRot;
            main.xRot = -(float)Math.PI / 2F + this.head.xRot + 0.1F;
            off.xRot  = -0.8F + this.head.xRot;
            off.yRot  =  0.6F + this.head.yRot;
            return;
        }

        // Crossbow charge
        if (state.rightArmPose == HumanoidModel.ArmPose.CROSSBOW_CHARGE
                || state.leftArmPose == HumanoidModel.ArmPose.CROSSBOW_CHARGE) {

            boolean rightHanded = state.mainArm == HumanoidArm.RIGHT;
            ModelPart main = rightHanded ? this.rightArm : this.leftArm;
            ModelPart off  = rightHanded ? this.leftArm  : this.rightArm;

            main.yRot = -0.8F + this.head.yRot;
            main.xRot = -0.9F + this.head.xRot;
            off.yRot  =  0.4F + this.head.yRot;
            off.xRot  = -0.9F + this.head.xRot;
            return;
        }
    }

    private void eatingAnimationRightHand(ItemUseAnimation useAnim, boolean eating, float ageInTicks) {
        boolean drinkingOrEating = useAnim == ItemUseAnimation.EAT || useAnim == ItemUseAnimation.DRINK;
        if (eating && drinkingOrEating) {
            this.rightArm.yRot = -0.5F;
            this.rightArm.xRot = -1.3F;
            this.rightArm.zRot = Mth.cos(ageInTicks) * 0.1F;
            this.head.xRot = Mth.cos(ageInTicks) * 0.2F;
            this.head.yRot = 0.0F;
            this.hat.copyFrom(this.head);
        }
    }

    private void eatingAnimationLeftHand(ItemUseAnimation useAnim, boolean eating, float ageInTicks) {
        boolean drinkingOrEating = useAnim == ItemUseAnimation.EAT || useAnim == ItemUseAnimation.DRINK;
        if (eating && drinkingOrEating) {
            this.leftArm.yRot = 0.5F;
            this.leftArm.xRot = -1.3F;
            this.leftArm.zRot = Mth.cos(ageInTicks) * 0.1F;
            this.head.xRot = Mth.cos(ageInTicks) * 0.2F;
            this.head.yRot = 0.0F;
            this.hat.copyFrom(this.head);
        }
    }

    private void holdWeaponHigh(HumanoidArm mainArm) {
        if (mainArm == HumanoidArm.LEFT) {
            this.leftArm.xRot = -1.8F;
        } else {
            this.rightArm.xRot = -1.8F;
        }
    }
}