package tallestegg.guardvillagers.entities.ai.goals;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class GetOutOfWaterGoal extends WaterAvoidingRandomStrollGoal {
    public GetOutOfWaterGoal(PathfinderMob p_25987_, double p_25988_) {
        super(p_25987_, p_25988_);
    }

    @Override
    public boolean canUse() {
        return this.mob.isInWaterOrBubble() && this.getPosition() != null;
    }

    @Nullable
    @Override
    protected Vec3 getPosition() {
        if (this.mob.isInWaterOrBubble()) {
            Vec3 vec3 = LandRandomPos.getPos(this.mob, 15, 7);
            return vec3 == null ? super.getPosition() : vec3;
        }
        return null;
    }
}
