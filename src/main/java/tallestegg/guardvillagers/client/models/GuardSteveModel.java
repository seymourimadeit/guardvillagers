package tallestegg.guardvillagers.client.models;

import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import tallestegg.guardvillagers.entities.GuardEntity;

public class GuardSteveModel extends PlayerModel<GuardEntity> {
    public GuardSteveModel(float modelSize) {
        super(modelSize, false);
    }

    @Override
    public void setRotationAngles(GuardEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netbipedHeadYaw, float bipedHeadPitch) {
        super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netbipedHeadYaw, bipedHeadPitch);
        if (entityIn.getKickTicks() > 0) {
            float f1 = 1.0F - (float) MathHelper.abs(10 - 2 * entityIn.getKickTicks()) / 10.0F;
            this.bipedRightLeg.rotateAngleX = MathHelper.lerp(f1, this.bipedRightLeg.rotateAngleX, -1.40F);
        }
        if (entityIn.getPrimaryHand() == HandSide.RIGHT) {
            this.eatingAnimationRightHand(Hand.MAIN_HAND, entityIn, ageInTicks);
            this.eatingAnimationLeftHand(Hand.OFF_HAND, entityIn, ageInTicks);
        } else {
            this.eatingAnimationRightHand(Hand.OFF_HAND, entityIn, ageInTicks);
            this.eatingAnimationLeftHand(Hand.MAIN_HAND, entityIn, ageInTicks);
        }
    }

    public void eatingAnimationRightHand(Hand hand, GuardEntity entity, float ageInTicks) {
        ItemStack itemstack = entity.getHeldItem(hand);
        boolean drinkingoreating = itemstack.getUseAction() == UseAction.EAT || itemstack.getUseAction() == UseAction.DRINK;
        if (entity.isEating() && drinkingoreating || entity.getItemInUseCount() > 0 && drinkingoreating && entity.getActiveHand() == hand) {
            this.bipedRightArm.rotateAngleY = -0.5F;
            this.bipedRightArm.rotateAngleX = -1.3F;
            this.bipedRightArm.rotateAngleZ = MathHelper.cos(ageInTicks) * 0.1F;
            this.bipedHead.rotateAngleX = MathHelper.cos(ageInTicks) * 0.2F;
            this.bipedHead.rotateAngleY = 0.0F;
            this.bipedHeadwear.copyModelAngles(bipedHead);
        }
    }

    public void eatingAnimationLeftHand(Hand hand, GuardEntity entity, float ageInTicks) {
        ItemStack itemstack = entity.getHeldItem(hand);
        boolean drinkingoreating = itemstack.getUseAction() == UseAction.EAT || itemstack.getUseAction() == UseAction.DRINK;
        if (entity.isEating() && drinkingoreating || entity.getItemInUseCount() > 0 && drinkingoreating && entity.getActiveHand() == hand) {
            this.bipedLeftArm.rotateAngleY = 0.5F;
            this.bipedLeftArm.rotateAngleX = -1.3F;
            this.bipedLeftArm.rotateAngleZ = MathHelper.cos(ageInTicks) * 0.1F;
            this.bipedHead.rotateAngleX = MathHelper.cos(ageInTicks) * 0.2F;
            this.bipedHead.rotateAngleY = 0.0F;
            this.bipedHeadwear.copyModelAngles(bipedHead);
        }
    }
}
