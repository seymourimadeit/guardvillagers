package tallestegg.guardvillagers.client.renderer;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.ZombieVillagerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.resources.ResourceLocation;
import tallestegg.guardvillagers.GuardVillagers;
import tallestegg.guardvillagers.client.models.GuardArmorModel;
import tallestegg.guardvillagers.client.models.GuardModel;
import tallestegg.guardvillagers.entities.Guard;

public class GuardRenderer extends HumanoidMobRenderer<Guard, GuardModel> {
    public GuardRenderer(EntityRendererProvider.Context p_174463_) {
        super(p_174463_, new GuardModel(p_174463_.bakeLayer(GuardVillagers.GUARD)), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this, new GuardArmorModel(p_174463_.bakeLayer(GuardVillagers.GUARD_ARMOR)), new GuardArmorModel(p_174463_.bakeLayer(GuardVillagers.GUARD_ARMOR))));
    }

    @Override
    public void render(Guard entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        this.setModelVisibilities(entityIn);
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    private void setModelVisibilities(Guard entityIn) {
        GuardModel guardmodel = this.getModel();
        ItemStack itemstack = entityIn.getMainHandItem();
        ItemStack itemstack1 = entityIn.getOffhandItem();
        guardmodel.setAllVisible(true);
        HumanoidModel.ArmPose bipedmodel$armpose = this.getArmPose(entityIn, itemstack, itemstack1, InteractionHand.MAIN_HAND);
        HumanoidModel.ArmPose bipedmodel$armpose1 = this.getArmPose(entityIn, itemstack, itemstack1, InteractionHand.OFF_HAND);
        guardmodel.crouching = entityIn.isCrouching();
        if (entityIn.getMainArm() == HumanoidArm.RIGHT) {
            guardmodel.rightArmPose = bipedmodel$armpose;
            guardmodel.leftArmPose = bipedmodel$armpose1;
        } else {
            guardmodel.rightArmPose = bipedmodel$armpose1;
            guardmodel.leftArmPose = bipedmodel$armpose;
        }
    }

    private HumanoidModel.ArmPose getArmPose(Guard entityIn, ItemStack itemStackMain, ItemStack itemStackOff, InteractionHand handIn) {
        HumanoidModel.ArmPose bipedmodel$armpose = HumanoidModel.ArmPose.EMPTY;
        ItemStack itemstack = handIn == InteractionHand.MAIN_HAND ? itemStackMain : itemStackOff;
        if (!itemstack.isEmpty()) {
            bipedmodel$armpose = HumanoidModel.ArmPose.ITEM;
            if (entityIn.getUseItemRemainingTicks() > 0) {
                UseAnim useaction = itemstack.getUseAnimation();
                switch (useaction) {
                case BLOCK:
                    bipedmodel$armpose = HumanoidModel.ArmPose.BLOCK;
                    break;
                case BOW:
                    bipedmodel$armpose = HumanoidModel.ArmPose.BOW_AND_ARROW;
                    break;
                case SPEAR:
                    bipedmodel$armpose = HumanoidModel.ArmPose.THROW_SPEAR;
                    break;
                case CROSSBOW:
                    if (handIn == entityIn.getUsedItemHand()) {
                        bipedmodel$armpose = HumanoidModel.ArmPose.CROSSBOW_CHARGE;
                    }
                    break;
                default:
                    bipedmodel$armpose = HumanoidModel.ArmPose.EMPTY;
                    break;
                }
            } else {
                boolean flag1 = itemStackMain.getItem() instanceof CrossbowItem;
                boolean flag2 = itemStackOff.getItem() instanceof CrossbowItem;
                if (flag1 && entityIn.isAggressive()) {
                    bipedmodel$armpose = HumanoidModel.ArmPose.CROSSBOW_HOLD;
                }

                if (flag2 && itemStackMain.getItem().getUseAnimation(itemStackMain) == UseAnim.NONE && entityIn.isAggressive()) {
                    bipedmodel$armpose = HumanoidModel.ArmPose.CROSSBOW_HOLD;
                }
            }
        }
        return bipedmodel$armpose;
    }

    @Override
    protected void scale(Guard entitylivingbaseIn, PoseStack matrixStackIn, float partialTickTime) {
        matrixStackIn.scale(0.9375F, 0.9375F, 0.9375F);
    }

    @Nullable
    @Override
    public ResourceLocation getTextureLocation(Guard entity) {
        return new ResourceLocation(GuardVillagers.MODID, "textures/entity/guard/guard_" + entity.getGuardVariant() + ".png");
    }
}
