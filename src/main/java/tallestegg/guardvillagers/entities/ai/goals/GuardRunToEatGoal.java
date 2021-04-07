package tallestegg.guardvillagers.entities.ai.goals;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.RandomWalkingGoal;
import net.minecraft.util.math.vector.Vector3d;
import tallestegg.guardvillagers.entities.GuardEntity;

public class GuardRunToEatGoal extends RandomWalkingGoal {
    private final GuardEntity guard;
    private int walkTimer;
    private boolean startedRunning;

    public GuardRunToEatGoal(GuardEntity guard) {
        super(guard, 1.0D);
        this.guard = guard;
        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.TARGET, Goal.Flag.LOOK));
    }

    @Override
    public boolean shouldExecute() {
        return this.guard.isRunningToEat() && this.getPosition() != null;
    }

    @Override
    public void startExecuting() {
        super.startExecuting();
        if (this.walkTimer <= 0 && !startedRunning) {
            this.walkTimer = 20;
            startedRunning = true;
        }
    }

    @Override
    public void tick() {
        if (--walkTimer <= 0 && guard.isRunningToEat()) {
            this.guard.setRunningToEat(false);
            this.guard.setEating(true);
            startedRunning = false;
            this.guard.getNavigator().clearPath();
        }
        List<LivingEntity> list = this.guard.world.getEntitiesWithinAABB(LivingEntity.class, this.guard.getBoundingBox().grow(5.0D, 3.0D, 5.0D));
        if (!list.isEmpty()) {
            for (LivingEntity mob : list) {
                if (mob != null) {
                    if (mob.getLastAttackedEntity() instanceof GuardEntity || mob instanceof MobEntity && ((MobEntity) mob).getAttackTarget() instanceof GuardEntity) {
                        if (walkTimer < 20)
                            this.walkTimer += 5;
                    }
                }
            }
        }
    }

    @Override
    protected Vector3d getPosition() {
        List<LivingEntity> list = this.guard.world.getEntitiesWithinAABB(LivingEntity.class, this.guard.getBoundingBox().grow(5.0D, 3.0D, 5.0D));
        if (!list.isEmpty()) {
            for (LivingEntity mob : list) {
                if (mob != null) {
                    if (mob.getLastAttackedEntity() instanceof GuardEntity || mob instanceof MobEntity && ((MobEntity) mob).getAttackTarget() instanceof GuardEntity) {
                        return RandomPositionGenerator.findRandomTargetBlockAwayFrom(guard, 16, 7, mob.getPositionVec());
                    }
                }
            }
        }
        return super.getPosition();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return super.shouldContinueExecuting() && this.walkTimer > 0 && this.guard.isRunningToEat() && !guard.isEating() && startedRunning;
    }
}
