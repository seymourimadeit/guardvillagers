package tallestegg.guardvillagers.client.renderer;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import tallestegg.guardvillagers.GuardVillagers;
import tallestegg.guardvillagers.client.models.GuardArmorModel;
import tallestegg.guardvillagers.client.models.GuardModel;
import tallestegg.guardvillagers.entities.GuardEntity;

public class GuardRenderer extends BipedRenderer<GuardEntity, GuardModel> {
    public GuardRenderer(EntityRendererManager manager) {
        super(manager, new GuardModel(0), 0.5f);
        this.addLayer(new BipedArmorLayer<GuardEntity, GuardModel, GuardArmorModel>(this, new GuardArmorModel(0.5F), new GuardArmorModel(1.0F)));
    }

    @Override
    public void render(GuardEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        this.setModelVisibilities(entityIn);
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    private void setModelVisibilities(GuardEntity entityIn) {
        GuardModel guardmodel = this.getEntityModel();
        ItemStack itemstack = entityIn.getHeldItemMainhand();
        ItemStack itemstack1 = entityIn.getHeldItemOffhand();
        guardmodel.setVisible(true);
        BipedModel.ArmPose bipedmodel$armpose = this.getArmPose(entityIn, itemstack, itemstack1, Hand.MAIN_HAND);
        BipedModel.ArmPose bipedmodel$armpose1 = this.getArmPose(entityIn, itemstack, itemstack1, Hand.OFF_HAND);
        guardmodel.isSneak = entityIn.isCrouching();
        if (entityIn.getPrimaryHand() == HandSide.RIGHT) {
            guardmodel.rightArmPose = bipedmodel$armpose;
            guardmodel.leftArmPose = bipedmodel$armpose1;
        } else {
            guardmodel.rightArmPose = bipedmodel$armpose1;
            guardmodel.leftArmPose = bipedmodel$armpose;
        }
    }

    private BipedModel.ArmPose getArmPose(GuardEntity entityIn, ItemStack itemStackMain, ItemStack itemStackOff, Hand handIn) {
        BipedModel.ArmPose bipedmodel$armpose = BipedModel.ArmPose.EMPTY;
        ItemStack itemstack = handIn == Hand.MAIN_HAND ? itemStackMain : itemStackOff;
        if (!itemstack.isEmpty()) {
            bipedmodel$armpose = BipedModel.ArmPose.ITEM;
            if (entityIn.getItemInUseCount() > 0) {
                UseAction useaction = itemstack.getUseAction();
                switch (useaction) {
                case BLOCK:
                    bipedmodel$armpose = BipedModel.ArmPose.BLOCK;
                    break;
                case BOW:
                    bipedmodel$armpose = BipedModel.ArmPose.BOW_AND_ARROW;
                    break;
                case SPEAR:
                    bipedmodel$armpose = BipedModel.ArmPose.THROW_SPEAR;
                    break;
                case CROSSBOW:
                    if (handIn == entityIn.getActiveHand()) {
                        bipedmodel$armpose = BipedModel.ArmPose.CROSSBOW_CHARGE;
                    }
                    break;
                default:
                    bipedmodel$armpose = BipedModel.ArmPose.EMPTY;
                    break;
                }
            } else {
                boolean flag1 = itemStackMain.getItem() instanceof CrossbowItem;
                boolean flag2 = itemStackOff.getItem() instanceof CrossbowItem;
                if (flag1 && entityIn.isAggressive()) {
                    bipedmodel$armpose = BipedModel.ArmPose.CROSSBOW_HOLD;
                }

                if (flag2 && itemStackMain.getItem().getUseAction(itemStackMain) == UseAction.NONE && entityIn.isAggressive()) {
                    bipedmodel$armpose = BipedModel.ArmPose.CROSSBOW_HOLD;
                }
            }
        }
        return bipedmodel$armpose;
    }

    @Override
    protected void preRenderCallback(GuardEntity entitylivingbaseIn, MatrixStack matrixStackIn, float partialTickTime) {
        matrixStackIn.scale(0.9375F, 0.9375F, 0.9375F);
    }

    @Nullable
    @Override
    public ResourceLocation getEntityTexture(GuardEntity entity) {
        return new ResourceLocation(GuardVillagers.MODID, "textures/entity/guard/guard_" + entity.getGuardVariant() + ".png");
    }
}
