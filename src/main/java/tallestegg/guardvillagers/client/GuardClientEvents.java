package tallestegg.guardvillagers.client;

import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import tallestegg.guardvillagers.GuardEntityType;
import tallestegg.guardvillagers.GuardVillagers;
import tallestegg.guardvillagers.client.models.GuardArmorModel;
import tallestegg.guardvillagers.client.models.GuardModel;
import tallestegg.guardvillagers.client.models.GuardSteveModel;
import tallestegg.guardvillagers.client.renderer.GuardRenderer;

@EventBusSubscriber(value = Dist.CLIENT)
public class GuardClientEvents {
    public static ModelLayerLocation GUARD = new ModelLayerLocation(
            ResourceLocation.withDefaultNamespace("modded/" + GuardVillagers.MODID + "/" + "guard"), "main");
    public static ModelLayerLocation GUARD_STEVE = new ModelLayerLocation(
            ResourceLocation.withDefaultNamespace("modded/" + GuardVillagers.MODID + "/" + "guard_steve"), "main");
    public static ModelLayerLocation GUARD_ARMOR_OUTER = new ModelLayerLocation(
            ResourceLocation.withDefaultNamespace("modded/" + GuardVillagers.MODID + "/" + "guard"), "armor_outer");
    public static ModelLayerLocation GUARD_ARMOR_INNER = new ModelLayerLocation(
            ResourceLocation.withDefaultNamespace("modded/" + GuardVillagers.MODID + "/" + "guard"), "armor_inner");
    public static ModelLayerLocation GUARD_PLAYER_ARMOR_OUTER = new ModelLayerLocation(
            ResourceLocation.withDefaultNamespace("modded/" + GuardVillagers.MODID + "/" + "guard_steve"), "armor_outer");
    public static ModelLayerLocation GUARD_PLAYER_ARMOR_INNER = new ModelLayerLocation(
            ResourceLocation.withDefaultNamespace("modded/" + GuardVillagers.MODID + "/"+ "guard_steve"), "armor_inner");

    @SubscribeEvent
    public static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(GuardClientEvents.GUARD, GuardModel::createBodyLayer);
        event.registerLayerDefinition(GuardClientEvents.GUARD_STEVE, GuardSteveModel::createMesh);
        event.registerLayerDefinition(GuardClientEvents.GUARD_ARMOR_OUTER, GuardArmorModel::createOuterArmorLayer);
        event.registerLayerDefinition(GuardClientEvents.GUARD_ARMOR_INNER, GuardArmorModel::createInnerArmorLayer);
        event.registerLayerDefinition(GuardClientEvents.GUARD_PLAYER_ARMOR_OUTER, () -> LayerDefinition.create(HumanoidArmorModel.createBodyLayer(new CubeDeformation(1.0F)), 64, 32));
        event.registerLayerDefinition(GuardClientEvents.GUARD_PLAYER_ARMOR_INNER, () -> LayerDefinition.create(HumanoidArmorModel.createBodyLayer(new CubeDeformation(0.5F)), 64, 32));
    }

    @SubscribeEvent
    public static void entityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(GuardEntityType.GUARD.get(), GuardRenderer::new);
    }
}
