package tallestegg.guardvillagers.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.item.equipment.Equippable;
import tallestegg.guardvillagers.GuardVillagers;
import tallestegg.guardvillagers.client.GuardClientEvents;
import tallestegg.guardvillagers.client.models.GuardArmorModel;
import tallestegg.guardvillagers.client.models.GuardModel;
import tallestegg.guardvillagers.client.models.GuardSteveModel;
import tallestegg.guardvillagers.client.renderer.state.GuardRenderState;
import tallestegg.guardvillagers.common.entities.Guard;
import tallestegg.guardvillagers.configuration.GuardConfig;

public class GuardRenderer extends HumanoidMobRenderer<Guard, GuardRenderState, HumanoidModel<GuardRenderState>> {

    private static Identifier baseTexture() {
        return Identifier.fromNamespaceAndPath(
                GuardVillagers.MODID,
                "textures/entity/guard/guard" + (GuardConfig.CLIENT.GuardSteve.get() ? "_steve" : "") + ".png"
        );
    }

    public GuardRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                GuardConfig.CLIENT.GuardSteve.get()
                        ? new GuardSteveModel(context.bakeLayer(GuardClientEvents.GUARD_STEVE))
                        : new GuardModel(context.bakeLayer(GuardClientEvents.GUARD)),
                0.5F
        );
        this.addLayer(new GuardVariantLayer(this, context.getResourceManager()));
        this.addLayer(new ItemInHandLayer<>(this));
        if (GuardConfig.CLIENT.GuardSteve.get()) {
            ArmorModelSet<HumanoidModel<GuardRenderState>> armorModels =
                    ArmorModelSet.bake(GuardClientEvents.GUARD_STEVE_ARMOR, context.getModelSet(), HumanoidModel::new);
            this.addLayer(new HumanoidArmorLayer<>(this, armorModels, context.getEquipmentRenderer()));
        } else {
            ArmorModelSet<HumanoidModel<GuardRenderState>> armorModels =
                    ArmorModelSet.bake(GuardClientEvents.GUARD_ARMOR, context.getModelSet(), GuardArmorModel::new);
            this.addLayer(new HumanoidArmorLayer<>(this, armorModels, context.getEquipmentRenderer()));
        }
    }

    @Override
    public GuardRenderState createRenderState() {
        return new GuardRenderState();
    }

    @Override
    public void extractRenderState(Guard entity, GuardRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
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

        boolean holdingShootable = main.getItem() instanceof ProjectileWeaponItem;

        state.kickTicks = entity.getKickTicks();
        state.aggressive = entity.isAggressive();
        state.eating = entity.isEating();
        state.blocking = entity.isBlocking();
        state.horizontalSpeedSqr = entity.getDeltaMovement().horizontalDistanceSqr();
        state.holdingShootable = holdingShootable;
        state.showQuiver = holdingShootable;
        state.showShoulderPads = entity.getItemBySlot(EquipmentSlot.CHEST).isEmpty();
        state.mainHandEmpty = main.isEmpty();
        state.offHandEmpty = off.isEmpty();
        state.mainHandUseAnimation = main.getUseAnimation();
        state.offHandUseAnimation = off.getUseAnimation();
        state.variant = entity.getVariant();

        state.rightArmPose = renderArmPose(entity, HumanoidArm.RIGHT);
        state.leftArmPose = renderArmPose(entity, HumanoidArm.LEFT);

        state.mainArm = entity.getMainArm();

        state.isCrouching = entity.getPose() == net.minecraft.world.entity.Pose.CROUCHING;
    }

    private static HumanoidModel.ArmPose renderArmPose(Guard guard, HumanoidArm arm) {
        ItemStack itemstack = guard.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack itemstack1 = guard.getItemInHand(InteractionHand.OFF_HAND);
        HumanoidModel.ArmPose humanoidmodel$armpose = identifyArmPoses(guard, itemstack, InteractionHand.MAIN_HAND);
        HumanoidModel.ArmPose humanoidmodel$armpose1 = identifyArmPoses(guard, itemstack1, InteractionHand.OFF_HAND);
        if (humanoidmodel$armpose.isTwoHanded()) {
            humanoidmodel$armpose1 = itemstack1.isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
        }

        return guard.getMainArm() == arm ? humanoidmodel$armpose : humanoidmodel$armpose1;
    }

    private static HumanoidModel.ArmPose identifyArmPoses(Guard guard, ItemStack handItem, InteractionHand hand) {
        var extensions = net.neoforged.neoforge.client.extensions.common.IClientItemExtensions.of(handItem);
        var armPose = extensions.getArmPose(guard, hand, handItem);
        if (armPose != null) {
            return armPose;
        }
        if (handItem.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        } else {
            if (guard.getUsedItemHand() == hand && guard.getUseItemRemainingTicks() > 0) {
                ItemUseAnimation itemuseanimation = handItem.getUseAnimation();
                switch (itemuseanimation) {
                    case BLOCK -> {
                        return HumanoidModel.ArmPose.BLOCK;
                    }
                    case BOW -> {
                        return HumanoidModel.ArmPose.BOW_AND_ARROW;
                    }
                    case TRIDENT -> {
                        return HumanoidModel.ArmPose.THROW_TRIDENT;
                    }
                    case CROSSBOW -> {
                        return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
                    }
                    case SPYGLASS -> {
                        return HumanoidModel.ArmPose.SPYGLASS;
                    }
                    case TOOT_HORN -> {
                        return HumanoidModel.ArmPose.TOOT_HORN;
                    }
                    case BRUSH -> {
                        return HumanoidModel.ArmPose.BRUSH;
                    }
                    case SPEAR -> {
                        return HumanoidModel.ArmPose.SPEAR;
                    }
                }
            }
            if (!guard.swinging && (handItem.getItem() instanceof CrossbowItem) && ((CrossbowItem.isCharged(handItem) && guard.isAggressive()) || guard.isAggressive())) {
                return HumanoidModel.ArmPose.CROSSBOW_HOLD;
            }
            SwingAnimation swinganimation = handItem.get(DataComponents.SWING_ANIMATION);
            if (swinganimation != null && swinganimation.type() == SwingAnimationType.STAB && guard.swinging) {
                return HumanoidModel.ArmPose.SPEAR;
            } else {
                return handItem.is(ItemTags.SPEARS) ? HumanoidModel.ArmPose.SPEAR : HumanoidModel.ArmPose.ITEM;
            }
        }
    }

    @Override
    public Identifier getTextureLocation(GuardRenderState state) {
        return baseTexture();
    }

    public static class GuardVariantLayer extends RenderLayer<GuardRenderState, HumanoidModel<GuardRenderState>> {

        private final ResourceManager resourceManager;

        public GuardVariantLayer(RenderLayerParent<GuardRenderState, HumanoidModel<GuardRenderState>> parent, ResourceManager resourceManager) {
            super(parent);
            this.resourceManager = resourceManager;
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, GuardRenderState state, float yRot, float xRot) {
            String guardSteve = GuardConfig.CLIENT.GuardSteve.get() ? "_steve" : "";
            Identifier variantTexture = Identifier.fromNamespaceAndPath(
                    GuardVillagers.MODID,
                    "textures/entity/guard/guard_variants/guard" + guardSteve + "_" + state.variant + ".png"
            );
            if (this.resourceManager.getResource(variantTexture).isEmpty()) {
                variantTexture = Identifier.fromNamespaceAndPath(
                        GuardVillagers.MODID,
                        "textures/entity/guard/guard_variants/guard" + guardSteve + "_plains.png"
                );
            }

            renderColoredCutoutModel(this.getParentModel(), variantTexture, poseStack, nodeCollector, packedLight, state, -1, 1);
        }
    }
}