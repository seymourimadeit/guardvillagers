package tallestegg.guardvillagers.entities.ai.goals;

import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;

public class GolemFloatWaterGoal extends FloatGoal {
    private final Mob mob;

    public GolemFloatWaterGoal(Mob pMob) {
        super(pMob);
        this.mob = pMob;
    }

    public boolean canUse() {
        return this.mob.isInWater() && this.mob.getFluidHeight(FluidTags.WATER) > this.mob.getFluidJumpThreshold();
    }
}