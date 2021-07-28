package tallestegg.guardvillagers.client.models;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import tallestegg.guardvillagers.entities.Guard;

public class GuardSteveModel extends PlayerModel<Guard> {
    public GuardSteveModel(ModelPart part) {
        super(part, false);
    }
    
    @Override
    public void setupAnim(Guard entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netbipedHeadYaw, float bipedHeadPitch) {
        super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netbipedHeadYaw, bipedHeadPitch);
        if (entityIn.getKickTicks() > 0) {
            float f1 = 1.0F - (float) Mth.abs(10 - 2 * entityIn.getKickTicks()) / 10.0F;
            this.rightLeg.xRot = Mth.lerp(f1, this.rightLeg.xRot, -1.40F);
        }
        if (entityIn.getMainArm() == HumanoidArm.RIGHT) {
            this.eatingAnimationRightHand(InteractionHand.MAIN_HAND, entityIn, ageInTicks);
            this.eatingAnimationLeftHand(InteractionHand.OFF_HAND, entityIn, ageInTicks);
        } else {
            this.eatingAnimationRightHand(InteractionHand.OFF_HAND, entityIn, ageInTicks);
            this.eatingAnimationLeftHand(InteractionHand.MAIN_HAND, entityIn, ageInTicks);
        }
    }
    
    public static LayerDefinition createMesh() {
        MeshDefinition meshdefinition = PlayerModel.createMesh(CubeDeformation.NONE, false);
        return LayerDefinition.create(meshdefinition, 64, 64);
     }

    public void eatingAnimationRightHand(InteractionHand hand, Guard entity, float ageInTicks) {
        ItemStack itemstack = entity.getItemInHand(hand);
        boolean drinkingoreating = itemstack.getUseAnimation() == UseAnim.EAT
                || itemstack.getUseAnimation() == UseAnim.DRINK;
        if (entity.isEating() && drinkingoreating
                || entity.getUseItemRemainingTicks() > 0 && drinkingoreating && entity.getUsedItemHand() == hand) {
            this.rightArm.yRot = -0.5F;
            this.rightArm.xRot = -1.3F;
            this.rightArm.zRot = Mth.cos(ageInTicks) * 0.1F;
            this.head.xRot = Mth.cos(ageInTicks) * 0.2F;
            this.head.yRot = 0.0F;
            this.hat.copyFrom(head);
        }
    }

    public void eatingAnimationLeftHand(InteractionHand hand, Guard entity, float ageInTicks) {
        ItemStack itemstack = entity.getItemInHand(hand);
        boolean drinkingoreating = itemstack.getUseAnimation() == UseAnim.EAT
                || itemstack.getUseAnimation() == UseAnim.DRINK;
        if (entity.isEating() && drinkingoreating
                || entity.getUseItemRemainingTicks() > 0 && drinkingoreating && entity.getUsedItemHand() == hand) {
            this.leftArm.yRot = 0.5F;
            this.leftArm.xRot = -1.3F;
            this.leftArm.zRot = Mth.cos(ageInTicks) * 0.1F;
            this.head.xRot = Mth.cos(ageInTicks) * 0.2F;
            this.head.yRot = 0.0F;
            this.hat.copyFrom(head);
        }
    }
}
