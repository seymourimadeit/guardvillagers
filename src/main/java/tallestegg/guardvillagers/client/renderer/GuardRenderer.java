package tallestegg.guardvillagers.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.equipment.Equippable;
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

public class GuardRenderer extends HumanoidMobRenderer<Guard, GuardRenderState, HumanoidModel<GuardRenderState>> {

    private static Identifier baseTexture() {
        return Identifier.fromNamespaceAndPath(
                GuardVillagers.MODID,
                "textures/entity/guard/guard" + (GuardConfig.CLIENT.GuardSteve.get() ? "_steve" : "") + ".png"
        );
    }

    // - LEGS -> INNER,   - HEAD/CHEST/FEET -> OUTER
    private static final ArmorModelSet<ModelLayerLocation> GUARD_ARMOR_LAYERS =
            new ArmorModelSet<>(
                    GuardClientEvents.GUARD_ARMOR_HEAD, // head
                    GuardClientEvents.GUARD_ARMOR_CHEST, // chest
                    GuardClientEvents.GUARD_ARMOR_LEGS, // legs
                    GuardClientEvents.GUARD_ARMOR_FEET  // feet
            );

    private static final ArmorModelSet<ModelLayerLocation> PLAYER_ARMOR_LAYERS =
            new ArmorModelSet<>(
                    GuardClientEvents.GUARD_STEVE_ARMOR_HEAD, // head
                    GuardClientEvents.GUARD_STEVE_ARMOR_CHEST, // chest
                    GuardClientEvents.GUARD_STEVE_ARMOR_LEGS, // legs
                    GuardClientEvents.GUARD_STEVE_ARMOR_FEET  // feet
            );

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

        boolean holdingShootable = main.getItem() instanceof ProjectileWeaponItem
                || (ModList.get().isLoaded("musketmod") && ModCompat.isHoldingMusket(main));

        state.kickTicks = entity.getKickTicks();
        state.aggressive = entity.isAggressive();
        state.eating = entity.isEating();
        state.blocking = entity.isBlocking();
        state.horizontalSpeedSqr = entity.getDeltaMovement().horizontalDistanceSqr();
        state.holdingShootable = holdingShootable;
        state.showQuiver = holdingShootable;
        Equippable chestEq = entity.getItemBySlot(EquipmentSlot.CHEST).get(DataComponents.EQUIPPABLE);
        state.showShoulderPads = chestEq == null || chestEq.slot() != EquipmentSlot.CHEST;
        state.mainHandEmpty = main.isEmpty();
        state.offHandEmpty = off.isEmpty();
        state.mainHandUseAnimation = main.getUseAnimation();
        state.offHandUseAnimation = off.getUseAnimation();
        state.variant = entity.getVariant();

        state.rightArmPose = getArmPose(entity, main, off, InteractionHand.MAIN_HAND);
        state.leftArmPose  = getArmPose(entity, main, off, InteractionHand.OFF_HAND);

        state.mainArm = entity.getMainArm();
        if (entity.getMainArm() == HumanoidArm.LEFT) {
            var tmp = state.rightArmPose;
            state.rightArmPose = state.leftArmPose;
            state.leftArmPose = tmp;
        }

        state.isCrouching = entity.getPose() == net.minecraft.world.entity.Pose.CROUCHING;
    }

    private static HumanoidModel.ArmPose getArmPose(Guard entity, ItemStack itemStackMain, ItemStack itemStackOff, InteractionHand handIn) {
        HumanoidModel.ArmPose armPose = HumanoidModel.ArmPose.EMPTY;
        ItemStack itemstack = handIn == InteractionHand.MAIN_HAND ? itemStackMain : itemStackOff;

        if (!itemstack.isEmpty()) {
            armPose = HumanoidModel.ArmPose.ITEM;

            if (entity.getUseItemRemainingTicks() > 0) {
                ItemUseAnimation useAction = itemstack.getUseAnimation();
                switch (useAction) {
                    case BLOCK -> armPose = HumanoidModel.ArmPose.BLOCK;
                    case BOW -> armPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
                    case SPEAR -> armPose = HumanoidModel.ArmPose.SPEAR;
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

            this.renderColoredCutoutModel(this.getParentModel(), variantTexture, poseStack, nodeCollector, packedLight, state, -1, state.outlineColor);
        }
    }
}