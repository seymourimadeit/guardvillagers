package tallestegg.guardvillagers.entities.ai.goals;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import tallestegg.guardvillagers.entities.Guard;

public class GuardRunToEatGoal extends RandomStrollGoal {
    private final Guard guard;
    private int walkTimer;
    private boolean startedRunning;

    public GuardRunToEatGoal(Guard guard) {
        super(guard, 1.0D);
        this.guard = guard;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.TARGET, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.guard.isRunningToEat() && this.getPosition() != null;
    }

    @Override
    public void start() {
        super.start();
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
            this.guard.getNavigation().stop();
        }
        List<LivingEntity> list = this.guard.level.getEntitiesOfClass(LivingEntity.class, this.guard.getBoundingBox().inflate(5.0D, 3.0D, 5.0D));
        if (!list.isEmpty()) {
            for (LivingEntity mob : list) {
                if (mob != null) {
                    if (mob.getLastHurtMob() instanceof Guard || mob instanceof Mob && ((Mob) mob).getTarget() instanceof Guard) {
                        if (walkTimer < 20)
                            this.walkTimer += 5;
                    }
                }
            }
        }
    }

    @Override
    protected Vec3 getPosition() {
        List<LivingEntity> list = this.guard.level.getEntitiesOfClass(LivingEntity.class, this.guard.getBoundingBox().inflate(5.0D, 3.0D, 5.0D));
        if (!list.isEmpty()) {
            for (LivingEntity mob : list) {
                if (mob != null) {
                    if (mob.getLastHurtMob() instanceof Guard || mob instanceof Mob && ((Mob) mob).getTarget() instanceof Guard) {
                        return DefaultRandomPos.getPosAway(guard, 16, 7, mob.position());
                    }
                }
            }
        }
        return super.getPosition();
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && this.walkTimer > 0 && this.guard.isRunningToEat() && !guard.isEating() && startedRunning;
    }
}
