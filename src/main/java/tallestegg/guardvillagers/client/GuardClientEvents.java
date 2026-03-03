// GuardClientEvents.java (MC 1.21.11 / NeoForge 21.11.x)

package tallestegg.guardvillagers.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import tallestegg.guardvillagers.GuardEntityType;
import tallestegg.guardvillagers.GuardVillagers;
import tallestegg.guardvillagers.client.models.GuardModel;
import tallestegg.guardvillagers.client.models.GuardSteveModel;
import tallestegg.guardvillagers.client.renderer.GuardRenderer;

@EventBusSubscriber(modid = GuardVillagers.MODID, value = Dist.CLIENT)
public final class GuardClientEvents {
    private GuardClientEvents() {}
    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(GuardVillagers.MODID, path);
    }
    public static final ModelLayerLocation GUARD = new ModelLayerLocation(id("guard"), "main");
    public static final ModelLayerLocation GUARD_STEVE = new ModelLayerLocation(id("guard_steve"), "main");
    public static final ModelLayerLocation GUARD_ARMOR_HEAD = new ModelLayerLocation(id("guard_armor/head"), "main");
    public static final ModelLayerLocation GUARD_ARMOR_CHEST = new ModelLayerLocation(id("guard_armor/chest"), "main");
    public static final ModelLayerLocation GUARD_ARMOR_LEGS = new ModelLayerLocation(id("guard_armor/legs"), "main");
    public static final ModelLayerLocation GUARD_ARMOR_FEET = new ModelLayerLocation(id("guard_armor/feet"), "main");
    public static final ArmorModelSet<ModelLayerLocation> GUARD_ARMOR = new ArmorModelSet<>(GUARD_ARMOR_HEAD, GUARD_ARMOR_CHEST, GUARD_ARMOR_LEGS, GUARD_ARMOR_FEET);
    public static final ModelLayerLocation GUARD_STEVE_ARMOR_HEAD = new ModelLayerLocation(id("guard_steve_armor/head"), "main");
    public static final ModelLayerLocation GUARD_STEVE_ARMOR_CHEST = new ModelLayerLocation(id("guard_steve_armor/chest"), "main");
    public static final ModelLayerLocation GUARD_STEVE_ARMOR_LEGS = new ModelLayerLocation(id("guard_steve_armor/legs"), "main");
    public static final ModelLayerLocation GUARD_STEVE_ARMOR_FEET = new ModelLayerLocation(id("guard_steve_armor/feet"), "main");
    public static final ArmorModelSet<ModelLayerLocation> GUARD_STEVE_ARMOR = new ArmorModelSet<>(GUARD_STEVE_ARMOR_HEAD, GUARD_STEVE_ARMOR_CHEST, GUARD_STEVE_ARMOR_LEGS, GUARD_STEVE_ARMOR_FEET);

    @SubscribeEvent
    public static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(GuardClientEvents.GUARD, GuardModel::createBodyLayer);
        event.registerLayerDefinition(GuardClientEvents.GUARD_STEVE, GuardSteveModel::createMesh);
        ArmorModelSet<MeshDefinition> armorMeshes = HumanoidModel.createArmorMeshSet(new CubeDeformation(0.5F), new CubeDeformation(1.0F));
        registerArmorLayerDefs(event, GUARD_ARMOR, armorMeshes);
        registerArmorLayerDefs(event, GUARD_STEVE_ARMOR, armorMeshes);
    }

    private static void registerArmorLayerDefs(EntityRenderersEvent.RegisterLayerDefinitions event, ArmorModelSet<ModelLayerLocation> targets, ArmorModelSet<MeshDefinition> meshes) {
        event.registerLayerDefinition(targets.head(),  () -> LayerDefinition.create(meshes.head(),  64, 32));
        event.registerLayerDefinition(targets.chest(), () -> LayerDefinition.create(meshes.chest(), 64, 32));
        event.registerLayerDefinition(targets.legs(),  () -> LayerDefinition.create(meshes.legs(),  64, 32));
        event.registerLayerDefinition(targets.feet(),  () -> LayerDefinition.create(meshes.feet(),  64, 32));
    }

    @SubscribeEvent
    public static void entityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(GuardEntityType.GUARD.get(), GuardRenderer::new);
    }
}