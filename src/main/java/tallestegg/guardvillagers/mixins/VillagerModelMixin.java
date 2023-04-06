package tallestegg.guardvillagers.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.VillagerHeadModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tallestegg.guardvillagers.configuration.GuardConfig;

@Mixin(VillagerModel.class)
public abstract class VillagerModelMixin<T extends Entity> extends HierarchicalModel<T> implements HeadedModel, VillagerHeadModel {
    @Shadow
    @Final
    private ModelPart head;

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int packedOverlay, float red, float blue, float green, float alpha) {
        float scale = this.young && GuardConfig.CLIENT.bigHeadBabyVillager.get() ? 1.5F : 1.0F;
        this.head.xScale = scale;
        this.head.yScale = scale;
        this.head.zScale = scale;
        super.renderToBuffer(pose, consumer, light, packedOverlay, red, blue, green, alpha);
    }
}
