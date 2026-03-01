package tallestegg.guardvillagers.client.renderer.state;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.item.ItemUseAnimation;

public class GuardRenderState extends HumanoidRenderState {
    public int kickTicks;
    public boolean aggressive;
    public boolean eating;
    public boolean blocking;
    public double horizontalSpeedSqr;
    public boolean holdingShootable;
    public boolean showQuiver;
    public boolean showShoulderPads;
    public boolean mainHandEmpty;
    public boolean offHandEmpty;
    public ItemUseAnimation mainHandUseAnimation = ItemUseAnimation.NONE;
    public ItemUseAnimation offHandUseAnimation  = ItemUseAnimation.NONE;
    public String variant = "plains";

    public HumanoidModel.ArmPose rightArmPose = HumanoidModel.ArmPose.EMPTY;
    public HumanoidModel.ArmPose leftArmPose  = HumanoidModel.ArmPose.EMPTY;
}