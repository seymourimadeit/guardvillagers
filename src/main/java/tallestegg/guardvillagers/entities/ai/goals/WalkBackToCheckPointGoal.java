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
    private int ticksRan = 0;
    private boolean stop = false;

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
        return this.canUse() && this.guard.getNavigation().isInProgress() && stop;
    }

    @Override
    public void start() {
        if (ticksRan > 200)
            this.ticksRan = 0;
        BlockPos blockpos = this.guard.getPatrolPos();
        if (blockpos != null && !this.guard.blockPosition().equals(this.guard.getPatrolPos())) {
            Path path = this.guard.getNavigation().createPath(blockpos, 0);
            this.guard.getNavigation().moveTo(path, this.speed);
        }
    }

    @Override
    public void tick() {
        if (this.guard.getNavigation().getPath() != null && !this.guard.getNavigation().getPath().canReach())
            this.ticksRan++;
        if (this.guard.getNavigation().getPath() != null && !this.guard.getNavigation().getPath().canReach() && !this.guard.blockPosition().equals(this.guard.getPatrolPos()) && ticksRan > 200)
            this.stop = true;
    }

    @Override
    public void stop() {
        if (stop)
            this.delayTime = this.guard.level().getGameTime();
        this.guard.getNavigation().stop();
        this.stop = false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}