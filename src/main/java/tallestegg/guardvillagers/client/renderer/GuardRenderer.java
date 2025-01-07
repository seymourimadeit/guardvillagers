package tallestegg.guardvillagers.client.renderer;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraftforge.fml.ModList;
import tallestegg.guardvillagers.ModCompat;
import tallestegg.guardvillagers.client.GuardClientEvents;
import tallestegg.guardvillagers.GuardVillagers;
import tallestegg.guardvillagers.client.models.GuardArmorModel;
import tallestegg.guardvillagers.client.models.GuardModel;
import tallestegg.guardvillagers.client.models.GuardSteveModel;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.entities.Guard;

public class GuardRenderer extends HumanoidMobRenderer<Guard, HumanoidModel<Guard>> {
    private static final ResourceLocation guardTextures = new ResourceLocation(GuardVillagers.MODID,
            "textures/entity/guard/guard" + (GuardConfig.CLIENT.GuardSteve.get() ? "_steve" : "") + ".png");

    public GuardRenderer(EntityRendererProvider.Context context) {
        super(context, new GuardModel(context.bakeLayer(GuardClientEvents.GUARD)), 0.5F);
        HumanoidModel<Guard> steve = new GuardSteveModel(context.bakeLayer(GuardClientEvents.GUARD_STEVE));
        HumanoidModel<Guard> normal = this.getModel();
        if (GuardConfig.guardSteve)
            this.model = steve;
        else
            this.model = normal;
        this.addLayer(new GuardVariantLayer(this));
        this.addLayer(new HumanoidArmorLayer(this, !GuardConfig.guardSteve ? new GuardArmorModel(context.bakeLayer(GuardClientEvents.GUARD_ARMOR_INNER)) : new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                !GuardConfig.guardSteve ? new GuardArmorModel(context.bakeLayer(GuardClientEvents.GUARD_ARMOR_OUTER)) : new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), context.getModelManager()));
    }

    @Override
    public void render(Guard entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        this.setModelVisibilities(entityIn);
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    private void setModelVisibilities(Guard entityIn) {
        HumanoidModel<Guard> guardmodel = this.getModel();
        ItemStack itemstack = entityIn.getMainHandItem();
        ItemStack itemstack1 = entityIn.getOffhandItem();
        guardmodel.setAllVisible(true);
        HumanoidModel.ArmPose bipedmodel$armpose = this.getArmPose(entityIn, itemstack, itemstack1,
                InteractionHand.MAIN_HAND);
        HumanoidModel.ArmPose bipedmodel$armpose1 = this.getArmPose(entityIn, itemstack, itemstack1,
                InteractionHand.OFF_HAND);
        guardmodel.crouching = entityIn.isCrouching();
        if (entityIn.getMainArm() == HumanoidArm.RIGHT) {
            guardmodel.rightArmPose = bipedmodel$armpose;
            guardmodel.leftArmPose = bipedmodel$armpose1;
        } else {
            guardmodel.rightArmPose = bipedmodel$armpose1;
            guardmodel.leftArmPose = bipedmodel$armpose;
        }
    }

    private HumanoidModel.ArmPose getArmPose(Guard entityIn, ItemStack itemStackMain, ItemStack itemStackOff,
                                             InteractionHand handIn) {
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
                if (ModList.get().isLoaded("musketmod"))
                    bipedmodel$armpose = ModCompat.reloadMusketAnim(itemstack, handIn, entityIn, bipedmodel$armpose);
            } else {
                if (ModList.get().isLoaded("musketmod"))
                    bipedmodel$armpose = ModCompat.holdMusketAnim(itemstack, entityIn, bipedmodel$armpose);
                boolean flag1 = itemStackMain.getItem() instanceof CrossbowItem;
                boolean flag2 = itemStackOff.getItem() instanceof CrossbowItem;
                if (flag1 && entityIn.isAggressive()) {
                    bipedmodel$armpose = HumanoidModel.ArmPose.CROSSBOW_HOLD;
                }

                if (flag2 && itemStackMain.getItem().getUseAnimation(itemStackMain) == UseAnim.NONE
                        && entityIn.isAggressive()) {
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
        return guardTextures;
    }

    public static class GuardVariantLayer extends RenderLayer<Guard, HumanoidModel<Guard>> {
        public GuardVariantLayer(RenderLayerParent renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, Guard livingEntity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            if (!livingEntity.isInvisible()) {
                EntityModel m = this.getParentModel();
                String guardSteve = GuardConfig.CLIENT.GuardSteve.get() ? "_steve" : "";
                ResourceLocation resourcelocation = new ResourceLocation(GuardVillagers.MODID, "textures/entity/guard/guard_variants/guard" + guardSteve + "_" + livingEntity.getGuardVariant() + ".png");
                AbstractTexture abstracttexture = Minecraft.getInstance().getTextureManager().getTexture(resourcelocation);
                if (abstracttexture == MissingTextureAtlasSprite.getTexture())
                    resourcelocation = new ResourceLocation(GuardVillagers.MODID, "textures/entity/guard/guard_variants/guard" + guardSteve + "_plains.png");
                renderColoredCutoutModel(m, resourcelocation, poseStack, bufferSource, packedLight, livingEntity, 1.0F, 1.0F, 1.0F);
            }
        }
    }
}
