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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.UseAnim;
import tallestegg.guardvillagers.entities.Guard;

public class GuardModel extends HumanoidModel<Guard> {
    public ModelPart Nose = this.head.getChild("nose");
    public ModelPart quiver = this.body.getChild("quiver");
    public ModelPart ArmLShoulderPad = this.rightArm.getChild("shoulderPad_left");
    public ModelPart ArmRShoulderPad = this.leftArm.getChild("shoulderPad_right");

    public GuardModel(ModelPart part) {
        super(part);
        this.setRotateAngle(quiver, 0.0F, 0.0F, 0.2617993877991494F);
        this.setRotateAngle(ArmLShoulderPad, 0.0F, 0.0F, -0.3490658503988659F);
        this.setRotateAngle(ArmRShoulderPad, 0.0F, 0.0F, 0.3490658503988659F);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition torso = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(52, 50)
                .addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(49, 99)
                .addBox(-4.0F, -10.0F, -4.0F, 8, 10, 8, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 0.0F));
        PartDefinition rightArm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(32, 75)
                .mirror().addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, new CubeDeformation(0.0F)),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        PartDefinition leftArm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(33, 48)
                .addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 2.0F, 0.0F));
        torso.addOrReplaceChild("quiver", CubeListBuilder.create().texOffs(100, 0).addBox(-2.5F, -2.0F, 0.0F, 5, 10, 5,
                new CubeDeformation(0.0F)), PartPose.offset(0.5F, 3.0F, 2.3F));
        head.addOrReplaceChild("nose",
                CubeListBuilder.create().texOffs(54, 0).addBox(-1.0F, 0.0F, -2.0F, 2, 4, 2, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -3.0F, -4.0F));
        partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(16, 48).mirror().addBox(-2.0F,
                0.0F, -2.0F, 4, 12, 4, new CubeDeformation(0.0F)), PartPose.offset(-1.9F, 12.0F, 0.0F));
        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 28).addBox(-2.0F, 0.0F, -2.0F,
                4, 12, 4, new CubeDeformation(0.0F)), PartPose.offset(1.9F, 12.0F, 0.0F));
        leftArm.addOrReplaceChild("shoulderPad_right",
                CubeListBuilder.create().texOffs(72, 33).mirror().addBox(0.0F, 0.0F, -3.0F, 5, 3, 6, new CubeDeformation(0.0F)),
                PartPose.offset(-0.5F, -3.5F, 0.0F));
        rightArm.addOrReplaceChild("shoulderPad_left",
                CubeListBuilder.create().texOffs(72, 33).addBox(-5.0F, 0.0F, -3.0F, 5, 3, 6, new CubeDeformation(0.0F)),
                PartPose.offset(0.5F, -3.5F, 0.0F));
        partdefinition.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -11.0F, -4.5F, 9,
                11, 9, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    public void setRotateAngle(ModelPart ModelRenderer, float x, float y, float z) {
        ModelRenderer.xRot = x;
        ModelRenderer.yRot = y;
        ModelRenderer.zRot = z;
    }

    @Override
    public void setupAnim(Guard entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netbipedHeadYaw, float bipedHeadPitch) {
        super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netbipedHeadYaw, bipedHeadPitch);
        ItemStack itemstack = entityIn.getItemInHand(InteractionHand.MAIN_HAND);
        boolean isHoldingShootable = itemstack.getItem() instanceof ProjectileWeaponItem;
        this.quiver.visible = isHoldingShootable;
        boolean hasChestplate = entityIn.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ArmorItem;
        this.ArmLShoulderPad.visible = !hasChestplate;
        this.ArmRShoulderPad.visible = !hasChestplate;
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