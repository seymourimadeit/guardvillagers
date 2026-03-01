package tallestegg.guardvillagers.client.models;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemUseAnimation;
import tallestegg.guardvillagers.client.renderer.state.GuardRenderState;

public class GuardSteveModel extends HumanoidModel<GuardRenderState> {

    public GuardSteveModel(ModelPart part) {
        super(part);
    }

    public static LayerDefinition createMesh() {
        MeshDefinition meshdefinition = PlayerModel.createMesh(CubeDeformation.NONE, false);
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(GuardRenderState state) {
        super.setupAnim(state);

        if (state.kickTicks > 0) {
            float f1 = 1.0F - (float) Mth.abs(10 - 2 * state.kickTicks) / 10.0F;
            this.rightLeg.xRot = Mth.lerp(f1, this.rightLeg.xRot, -1.40F);
        }

        double speed = 0.005D;
        if (state.aggressive && !state.holdingShootable && state.horizontalSpeedSqr > speed && !state.mainHandEmpty && !state.blocking) {
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

    private void eatingAnimationRightHand(ItemUseAnimation useAnim, boolean eating, float ageInTicks) {
        boolean drinkingOrEating = useAnim == ItemUseAnimation.EAT || useAnim == ItemUseAnimation.DRINK;
        if (eating && drinkingOrEating) {
            this.rightArm.yRot = -0.5F;
            this.rightArm.xRot = -1.3F;
            this.rightArm.zRot = Mth.cos(ageInTicks) * 0.1F;
            this.head.xRot = Mth.cos(ageInTicks) * 0.2F;
            this.head.yRot = 0.0F;
            this.hat.visible = this.head.visible;
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
            this.hat.visible = this.head.visible;
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