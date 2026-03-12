package tallestegg.guardvillagers.client.models;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import tallestegg.guardvillagers.client.renderer.state.GuardRenderState;

public class GuardArmorModel extends HumanoidModel<GuardRenderState> {
    public GuardArmorModel(ModelPart part) {
        super(part);
    }

    @Override
    public void setupAnim(GuardRenderState state) {
        super.setupAnim(state);

        syncHatToHead();
    }
    private void syncHatToHead() {
        this.hat.visible = this.head.visible;

        try {
            if (this.head.getChild("hat") != this.hat) {
                copyPart(this.hat, this.head);
            }
        } catch (Exception e) {
            copyPart(this.hat, this.head);
        }
    }

    private static void copyPart(ModelPart dst, ModelPart src) {
        dst.x = src.x;
        dst.y = src.y;
        dst.z = src.z;

        dst.xRot = src.xRot;
        dst.yRot = src.yRot;
        dst.zRot = src.zRot;

        dst.xScale = src.xScale;
        dst.yScale = src.yScale;
        dst.zScale = src.zScale;

        dst.visible = src.visible;
        dst.skipDraw = src.skipDraw;
    }
}