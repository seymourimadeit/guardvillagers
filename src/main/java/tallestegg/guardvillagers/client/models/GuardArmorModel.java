package tallestegg.guardvillagers.client.models;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.ArmorModelSet;

public class GuardArmorModel extends GuardModel {
    public GuardArmorModel(ModelPart part) {
        super(part);
    }

    public static ArmorModelSet<MeshDefinition> createArmorMeshSet(CubeDeformation innerCubeDeformation, CubeDeformation outerCubeDeformation) {
        return createArmorMeshSet(GuardArmorModel::createBaseMesh, ADULT_ARMOR_PARTS_PER_SLOT, innerCubeDeformation, outerCubeDeformation);
    }

    private static MeshDefinition createBaseMesh(CubeDeformation cubeDeformation) {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(cubeDeformation, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition head = partdefinition.addOrReplaceChild(
                "head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 8.0F, 8.0F, cubeDeformation), PartPose.ZERO
        );
        head.addOrReplaceChild("nose",
                CubeListBuilder.create().texOffs(54, 0).addBox(-1.0F, -1.0F, -2.0F, 2, 4, 2, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -3.0F, -4.0F));
        partdefinition.addOrReplaceChild(
                "hat",
                CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 8.0F, 8.0F, cubeDeformation.extend(0.5F)),
                PartPose.offset(0.0F, 1.0F, 0.0F)
        );
        partdefinition.getChild("body").addOrReplaceChild("quiver", CubeListBuilder.create().texOffs(100, 0).addBox(-2.5F, -2.0F, 0.0F, 5, 10, 5,
                new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 3.0F, 2.3F, 0.0F, 0.0F, 0.2617993877991494F));
        partdefinition.getChild("left_arm").addOrReplaceChild("shoulderPad_right",
                CubeListBuilder.create().texOffs(72, 33).mirror().addBox(0.0F, 0.0F, -3.0F, 5, 3, 6, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.5F, -3.5F, 0.0F, 0.0F, 0.0F, 0.3490658503988659F));
        partdefinition.getChild("right_arm").addOrReplaceChild("shoulderPad_left",
                CubeListBuilder.create().texOffs(72, 33).addBox(-5.0F, 0.0F, -3.0F, 5, 3, 6, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.5F, -3.5F, 0.0F, 0.0F, 0.0F, -0.3490658503988659F));
        return meshdefinition;
    }

}