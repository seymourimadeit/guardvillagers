package tallestegg.guardvillagers.mixins;

import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tallestegg.guardvillagers.configuration.GuardConfig;

@Mixin(VillagerModel.class)
public abstract class VillagerModelMixin {

    @Shadow
    public abstract ModelPart getHead();

    @Unique
    private boolean guardvillagers$isBaby;

    @Inject(method = "setupAnim", at = @At("HEAD"))
    private void guardvillagers$captureBabyState(VillagerRenderState state, CallbackInfo ci) {
        this.guardvillagers$isBaby = state.isBaby;
    }

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void guardvillagers$applyBigHeadScale(VillagerRenderState state, CallbackInfo ci) {
        ModelPart head = this.getHead();

        float scale = (this.guardvillagers$isBaby && GuardConfig.CLIENT.bigHeadBabyVillager.get()) ? 1.5F : 1.0F;

        head.xScale = scale;
        head.yScale = scale;
        head.zScale = scale;
    }
}