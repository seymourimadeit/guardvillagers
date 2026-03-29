package tallestegg.guardvillagers.client.models;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.object.armorstand.ArmorStandArmorModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import tallestegg.guardvillagers.client.renderer.state.GuardRenderState;

public class GuardArmorModel extends HumanoidModel<GuardRenderState> {
    public GuardArmorModel(ModelPart part) {
        super(part);
    }

    public static ArmorModelSet<MeshDefinition> createArmorMeshSet(CubeDeformation innerCubeDeformation, CubeDeformation outerCubeDeformation) {
        return createArmorMeshSet(GuardArmorModel::createBaseMesh, ADULT_ARMOR_PARTS_PER_SLOT, innerCubeDeformation, outerCubeDeformation);
    }

    private static MeshDefinition createBaseMesh(CubeDeformation cubeDeformation) {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(cubeDeformation, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild(
                "head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 8.0F, 8.0F, cubeDeformation), PartPose.ZERO
        );
        partdefinition.addOrReplaceChild(
                "hat",
                CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 8.0F, 8.0F, cubeDeformation.extend(0.5F)),
                PartPose.offset(0.0F, 1.0F, 0.0F)
        );
        return meshdefinition;
    }

}