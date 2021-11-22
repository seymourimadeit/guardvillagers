package tallestegg.guardvillagers;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tallestegg.guardvillagers.client.models.GuardArmorModel;
import tallestegg.guardvillagers.client.models.GuardModel;
import tallestegg.guardvillagers.client.models.GuardSteveModel;
import tallestegg.guardvillagers.client.renderer.GuardRenderer;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GuardClientEvents {
    public static ModelLayerLocation GUARD = new ModelLayerLocation(
            new ResourceLocation(GuardVillagers.MODID + "guard"), "guard");
    public static ModelLayerLocation GUARD_STEVE = new ModelLayerLocation(
            new ResourceLocation(GuardVillagers.MODID + "guard_steve"), "guard_steve");
    public static ModelLayerLocation GUARD_ARMOR_OUTER = new ModelLayerLocation(
            new ResourceLocation(GuardVillagers.MODID + "guard_armor_outer"), "guard_armor_outer");
    public static ModelLayerLocation GUARD_ARMOR_INNER = new ModelLayerLocation(
            new ResourceLocation(GuardVillagers.MODID + "guard_armor_inner"), "guard_armor_inner");

    @SubscribeEvent
    public static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(GuardClientEvents.GUARD, GuardModel::createBodyLayer);
        event.registerLayerDefinition(GuardClientEvents.GUARD_STEVE, GuardSteveModel::createMesh);
        event.registerLayerDefinition(GuardClientEvents.GUARD_ARMOR_OUTER, GuardArmorModel::createOuterArmorLayer);
        event.registerLayerDefinition(GuardClientEvents.GUARD_ARMOR_INNER, GuardArmorModel::createInnerArmorLayer);
    }

    @SubscribeEvent
    public static void entityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(GuardEntityType.GUARD.get(), GuardRenderer::new);
    }
}
