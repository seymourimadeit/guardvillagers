package tallestegg.guardvillagers.entities.ai.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import tallestegg.guardvillagers.entities.Guard;

import java.util.EnumSet;

public class WalkBackToCheckPointGoal extends Goal {
    private final Guard guard;
    private final double speed;
    private long delayTime = 0L;

    public WalkBackToCheckPointGoal(Guard guard, double speedIn) {
        this.guard = guard;
        this.speed = speedIn;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return guard.getTarget() == null && this.guard.getPatrolPos() != null && !this.guard.blockPosition().equals(this.guard.getPatrolPos()) && !guard.isFollowing() && guard.isPatrolling() && (guard.level().getGameTime() - delayTime) > 200L;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse() && this.guard.getNavigation().isInProgress() && (!this.guard.getNavigation().isStuck() || ((this.guard.getNavigation().getPath() != null && !(this.guard.getNavigation().getPath().getEndNode().distanceTo(guard.blockPosition()) > 2))));
    }

    @Override
    public void start() {
        BlockPos blockpos = this.guard.getPatrolPos();
        if (blockpos != null) {
            Path path = this.guard.getNavigation().createPath(blockpos, 0);
            this.guard.getNavigation().moveTo(path, this.speed);
        }
    }

    @Override
    public void stop() {
        if (this.guard.getNavigation().getPath() != null && !this.guard.getNavigation().getPath().canReach())
            this.delayTime = this.guard.level().getGameTime();
        this.guard.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}