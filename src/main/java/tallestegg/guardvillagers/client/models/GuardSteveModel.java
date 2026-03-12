package tallestegg.guardvillagers.client.models;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.player.PlayerModel;
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

        syncHatToHead();
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
        }
    }

    private void holdWeaponHigh(HumanoidArm mainArm) {
        if (mainArm == HumanoidArm.LEFT) {
            this.leftArm.xRot = -1.8F;
        } else {
            this.rightArm.xRot = -1.8F;
        }
    }
    private void syncHatToHead() {
        this.hat.visible = this.head.visible;

        try {
            if (this.head.getChild("hat") != this.hat) {
                copyPart(this.hat, this.head);
            }
        } catch (Exception e) {
            copyPart(this.hat, this.head);
        }
    }

    private static void copyPart(ModelPart dst, ModelPart src) {
        dst.x = src.x;
        dst.y = src.y;
        dst.z = src.z;

        dst.xRot = src.xRot;
        dst.yRot = src.yRot;
        dst.zRot = src.zRot;

        dst.xScale = src.xScale;
        dst.yScale = src.yScale;
        dst.zScale = src.zScale;

        dst.visible = src.visible;
        dst.skipDraw = src.skipDraw;
    }
}