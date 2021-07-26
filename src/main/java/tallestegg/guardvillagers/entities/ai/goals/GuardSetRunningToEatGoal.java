package tallestegg.guardvillagers.entities.ai.goals;

import java.util.EnumSet;

import net.minecraft.world.entity.ai.goal.Goal;
import tallestegg.guardvillagers.entities.Guard;

public class GuardSetRunningToEatGoal extends Goal {
    protected final Guard guard;

    public GuardSetRunningToEatGoal(Guard guard, double speedIn) {
        super();
        this.guard = guard;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return !guard.isRunningToEat() && guard.getHealth() < guard.getMaxHealth() / 2 && GuardEatFoodGoal.isConsumable(guard.getOffhandItem()) && !guard.isEating() && guard.getTarget() != null;
    }

    @Override
    public void start() {
        this.guard.setTarget(null);
        if (!guard.isRunningToEat())
            this.guard.setRunningToEat(true);

    }
}
