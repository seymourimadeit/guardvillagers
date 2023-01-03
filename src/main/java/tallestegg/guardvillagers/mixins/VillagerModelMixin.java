package tallestegg.guardvillagers.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.VillagerHeadModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tallestegg.guardvillagers.configuration.GuardConfig;

@Mixin(VillagerModel.class)
public abstract class VillagerModelMixin<T extends Entity> extends HierarchicalModel<T> implements HeadedModel, VillagerHeadModel {
    @Shadow
    @Final
    private ModelPart head;

    @Override
    public void renderToBuffer(PoseStack p_170625_, VertexConsumer p_170626_, int p_170627_, int p_170628_, float p_170629_, float p_170630_, float p_170631_, float p_170632_) {
        if (this.young && GuardConfig.CLIENT.bigHeadBabyVillager.get()) {
            float f = 1.5F;
            this.head.xScale = f;
            this.head.yScale = f;
            this.head.zScale = f;
        } else {
            this.head.resetPose();
        }
        super.renderToBuffer(p_170625_, p_170626_, p_170627_, p_170628_, p_170629_, p_170630_, p_170631_, p_170632_);
    }
}
