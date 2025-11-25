package tallestegg.guardvillagers.entities.ai.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import tallestegg.guardvillagers.entities.Guard;

import java.util.EnumSet;

public class WalkBackToCheckPointGoal extends Goal {
    private final Guard guard;
    private final double speed;
    public WalkBackToCheckPointGoal(Guard guard, double speedIn) {
        this.guard = guard;
        this.speed = speedIn;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return guard.getTarget() == null && guard.getPatrolPos() != null && this.guard.blockPosition() != this.guard.getPatrolPos() && !guard.isFollowing() && guard.isPatrolling();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse() && this.guard.getNavigation().isInProgress();
    }

    @Override
    public void start() {
        BlockPos blockpos = this.guard.getPatrolPos();
        if (blockpos != null) {
            Vec3 vector3d = Vec3.atCenterOf(blockpos);
            this.guard.getNavigation().moveTo(vector3d.x, vector3d.y, vector3d.z, this.speed);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}