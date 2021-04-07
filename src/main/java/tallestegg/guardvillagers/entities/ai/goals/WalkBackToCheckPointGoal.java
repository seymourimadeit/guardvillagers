package tallestegg.guardvillagers.entities.ai.goals;

import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ShootableItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import tallestegg.guardvillagers.entities.GuardEntity;

public class WalkBackToCheckPointGoal extends Goal {
    private final GuardEntity guard;
    private final double speed;

    public WalkBackToCheckPointGoal(GuardEntity guard, double speedIn) {
        this.guard = guard;
        this.speed = speedIn;
    }

    @Override
    public boolean shouldExecute() {
        return guard.getPatrolPos() != null && !this.guard.getPatrolPos().withinDistance(this.guard.getPositionVec(), 1.0D) && !guard.isFollowing() && guard.isPatrolling();
    }

    /*
     * @Override public void startExecuting() { }
     * 
     * @Override public void resetTask() { // this.guard.setGoingHome(false); }
     */

    @Override
    public boolean shouldContinueExecuting() {
        return this.shouldExecute();
    }

    @Override
    public void tick() {
        BlockPos blockpos = this.guard.getPatrolPos();
        if (blockpos != null) {
            Vector3d vector3d = Vector3d.copyCenteredHorizontally(blockpos);
            Vector3d vector3d1 = RandomPositionGenerator.findRandomTargetTowardsScaled(this.guard, 16, 3, vector3d, (double) ((float) Math.PI / 10F));
            if (!this.guard.getPatrolPos().withinDistance(this.guard.getPositionVec(), 1.0D) || blockpos != null)
                if (guard.getHeldItemMainhand().getItem() instanceof ShootableItem) {
                    this.guard.getNavigator().tryMoveToXYZ(blockpos.getX(), blockpos.getY(), blockpos.getZ(), this.speed);
                } else if (vector3d1 != null && guard.getAttackTarget() == null) {
                    this.guard.getNavigator().tryMoveToXYZ(vector3d1.getX(), vector3d1.getY(), vector3d1.getZ(), this.speed);
                }
        }
    }
}
