package tallestegg.guardvillagers.entities.ai.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import tallestegg.guardvillagers.entities.Guard;

public class WalkBackToCheckPointGoal extends Goal {
    private final Guard guard;
    private final double speed;

    public WalkBackToCheckPointGoal(Guard guard, double speedIn) {
        this.guard = guard;
        this.speed = speedIn;

    }

    @Override
    public boolean canUse() {
        return guard.getPatrolPos() != null && !this.guard.getPatrolPos().closerThan(this.guard.blockPosition(), 1.0D) && !guard.isFollowing() && guard.isPatrolling();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void tick() {
        BlockPos blockpos = this.guard.getPatrolPos();
        if (blockpos != null) {
            Vec3 vector3d = Vec3.atBottomCenterOf(blockpos);
            if (!this.guard.getPatrolPos().closerThan(this.guard.blockPosition(), 1.0D) || blockpos != null)
                this.guard.getNavigation().moveTo(blockpos.getX(), blockpos.getY(), blockpos.getZ(), this.speed);
        }
    }
}
