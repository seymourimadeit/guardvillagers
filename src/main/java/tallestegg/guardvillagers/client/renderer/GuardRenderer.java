package tallestegg.guardvillagers.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.neoforged.fml.ModList;
import tallestegg.guardvillagers.GuardVillagers;
import tallestegg.guardvillagers.ModCompat;
import tallestegg.guardvillagers.client.GuardClientEvents;
import tallestegg.guardvillagers.client.models.GuardArmorModel;
import tallestegg.guardvillagers.client.models.GuardModel;
import tallestegg.guardvillagers.client.models.GuardSteveModel;
import tallestegg.guardvillagers.client.renderer.state.GuardRenderState;
import tallestegg.guardvillagers.common.entities.Guard;
import tallestegg.guardvillagers.configuration.GuardConfig;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;

public class GuardRenderer extends HumanoidMobRenderer<Guard, GuardRenderState, HumanoidModel<GuardRenderState>> {

    private static final ResourceLocation GUARD_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            GuardVillagers.MODID,
            "textures/entity/guard/guard" + (GuardConfig.CLIENT.GuardSteve.get() ? "_steve" : "") + ".png"
    );

    public GuardRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                GuardConfig.CLIENT.GuardSteve.get()
                        ? new GuardSteveModel(context.bakeLayer(GuardClientEvents.GUARD_STEVE))
                        : new GuardModel(context.bakeLayer(GuardClientEvents.GUARD)),
                0.5F
        );

        this.addLayer(new GuardVariantLayer(this));
        this.addLayer(new ItemInHandLayer<>(this, context.getItemRenderer()));

        this.addLayer(new HumanoidArmorLayer<>(
                this,
                !GuardConfig.CLIENT.GuardSteve.get()
                        ? new GuardArmorModel(context.bakeLayer(GuardClientEvents.GUARD_ARMOR_INNER))
                        : new HumanoidArmorModel<>(context.bakeLayer(GuardClientEvents.GUARD_PLAYER_ARMOR_INNER)),
                !GuardConfig.CLIENT.GuardSteve.get()
                        ? new GuardArmorModel(context.bakeLayer(GuardClientEvents.GUARD_ARMOR_OUTER))
                        : new HumanoidArmorModel<>(context.bakeLayer(GuardClientEvents.GUARD_PLAYER_ARMOR_OUTER)),
                context.getEquipmentRenderer()
        ));
    }

    @Override
    public GuardRenderState createRenderState() {
        return new GuardRenderState();
    }

    @Override
    public void extractRenderState(Guard entity, GuardRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);

        state.attackTime = entity.getAttackAnim(partialTick);
        state.mainArm = entity.getMainArm();
        state.isCrouching = entity.isCrouching();

        state.isUsingItem = entity.isUsingItem();
        if (state.isUsingItem) {
            state.useItemHand = entity.getUsedItemHand();
            state.ticksUsingItem = entity.getTicksUsingItem();

            ItemStack use = entity.getUseItem();
            if (use.getItem() instanceof CrossbowItem) {
                state.maxCrossbowChargeDuration = CrossbowItem.getChargeDuration(use, entity);
            } else {
                state.maxCrossbowChargeDuration = 0.0F;
            }
        } else {
            state.ticksUsingItem = 0;
            state.maxCrossbowChargeDuration = 0.0F;
        }

        ItemStack main = entity.getMainHandItem();
        ItemStack off = entity.getOffhandItem();

        boolean holdingShootable = main.getItem() instanceof ProjectileWeaponItem
                || (ModList.get().isLoaded("musketmod") && ModCompat.isHoldingMusket(main));

        state.kickTicks = entity.getKickTicks();
        state.aggressive = entity.isAggressive();
        state.eating = entity.isEating();
        state.blocking = entity.isBlocking();
        state.horizontalSpeedSqr = entity.getDeltaMovement().horizontalDistanceSqr();
        state.holdingShootable = holdingShootable;
        state.showQuiver = holdingShootable;
        state.showShoulderPads = !(entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ArmorItem);
        state.mainHandEmpty = main.isEmpty();
        state.offHandEmpty = off.isEmpty();
        state.mainHandUseAnimation = main.getUseAnimation();
        state.offHandUseAnimation = off.getUseAnimation();
        state.variant = entity.getVariant();

        HumanoidModel.ArmPose mainPose = this.getArmPose(entity, main, off, InteractionHand.MAIN_HAND);
        HumanoidModel.ArmPose offPose = this.getArmPose(entity, main, off, InteractionHand.OFF_HAND);

        if (entity.getMainArm() == HumanoidArm.RIGHT) {
            state.rightArmPose = mainPose;
            state.leftArmPose = offPose;
        } else {
            state.rightArmPose = offPose;
            state.leftArmPose = mainPose;
        }

        state.isCrouching = entity.getPose() == net.minecraft.world.entity.Pose.CROUCHING;
    }

    private HumanoidModel.ArmPose getArmPose(Guard entity, ItemStack itemStackMain, ItemStack itemStackOff, InteractionHand handIn) {
        HumanoidModel.ArmPose armPose = HumanoidModel.ArmPose.EMPTY;
        ItemStack itemstack = handIn == InteractionHand.MAIN_HAND ? itemStackMain : itemStackOff;

        if (!itemstack.isEmpty()) {
            armPose = HumanoidModel.ArmPose.ITEM;

            if (entity.getUseItemRemainingTicks() > 0) {
                ItemUseAnimation useAction = itemstack.getUseAnimation();
                switch (useAction) {
                    case BLOCK -> armPose = HumanoidModel.ArmPose.BLOCK;
                    case BOW -> armPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
                    case SPEAR -> armPose = HumanoidModel.ArmPose.THROW_SPEAR;
                    case CROSSBOW -> {
                        if (handIn == entity.getUsedItemHand()) {
                            armPose = HumanoidModel.ArmPose.CROSSBOW_CHARGE;
                        }
                    }
                    default -> armPose = HumanoidModel.ArmPose.ITEM;
                }

                if (ModList.get().isLoaded("musketmod")) {
                    armPose = ModCompat.reloadMusketAnim(itemstack, handIn, entity, armPose);
                }
            } else {
                if (ModList.get().isLoaded("musketmod")) {
                    armPose = ModCompat.holdMusketAnim(itemstack, entity);
                }

                boolean mainCrossbow = itemStackMain.getItem() instanceof CrossbowItem;
                boolean offCrossbow = itemStackOff.getItem() instanceof CrossbowItem;

                if (mainCrossbow && entity.isAggressive()) {
                    armPose = HumanoidModel.ArmPose.CROSSBOW_HOLD;
                }

                if (offCrossbow && itemStackMain.getUseAnimation() == ItemUseAnimation.NONE && entity.isAggressive()) {
                    armPose = HumanoidModel.ArmPose.CROSSBOW_HOLD;
                }
            }
        }

        return armPose;
    }

    @Override
    public ResourceLocation getTextureLocation(GuardRenderState state) {
        return GUARD_TEXTURE;
    }

    public static class GuardVariantLayer extends RenderLayer<GuardRenderState, HumanoidModel<GuardRenderState>> {

        public GuardVariantLayer(RenderLayerParent<GuardRenderState, HumanoidModel<GuardRenderState>> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, GuardRenderState state, float yRot, float xRot) {
            EntityModel<GuardRenderState> model = this.getParentModel();

            String guardSteve = GuardConfig.CLIENT.GuardSteve.get() ? "_steve" : "";
            ResourceLocation variantTexture = ResourceLocation.fromNamespaceAndPath(
                    GuardVillagers.MODID,
                    "textures/entity/guard/guard_variants/guard" + guardSteve + "_" + state.variant + ".png"
            );

            AbstractTexture tex = Minecraft.getInstance().getTextureManager().getTexture(variantTexture);
            if (tex == MissingTextureAtlasSprite.getTexture()) {
                variantTexture = ResourceLocation.fromNamespaceAndPath(
                        GuardVillagers.MODID,
                        "textures/entity/guard/guard_variants/guard" + guardSteve + "_plains.png"
                );
            }

            renderColoredCutoutModel(model, variantTexture, poseStack, bufferSource, packedLight, state, -1);
        }
    }
}