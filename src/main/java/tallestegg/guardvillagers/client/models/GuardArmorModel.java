package tallestegg.guardvillagers.client.models;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import tallestegg.guardvillagers.entities.Guard;

public class GuardArmorModel extends HumanoidModel<Guard> {
    public GuardArmorModel(ModelPart part) {
        super(part);
    }

    public static LayerDefinition createOuterArmorLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(new CubeDeformation(1.0F), 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(1.0F)),
                PartPose.offset(0.0F, 1.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 64, 32);
    }
    public static LayerDefinition createInnerArmorLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(new CubeDeformation(0.5F), 0.0F);
        return LayerDefinition.create(meshdefinition, 64, 32);
    }
}
