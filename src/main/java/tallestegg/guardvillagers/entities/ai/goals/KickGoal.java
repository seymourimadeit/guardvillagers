package tallestegg.guardvillagers.entities.ai.goals;

import net.minecraft.world.entity.ai.goal.Goal;
import tallestegg.guardvillagers.entities.Guard;

public class KickGoal extends Goal {

    public final Guard guard;

    public KickGoal(Guard guard) {
        this.guard = guard;
    }

    @Override
    public boolean canUse() {
        return guard.getTarget() != null && guard.getTarget().distanceTo(guard) <= 2.5D && guard.getMainHandItem().getItem().useOnRelease(guard.getMainHandItem()) && !guard.isBlocking() && guard.kickCoolDown == 0;
    }

    @Override
    public void start() {
        guard.setKicking(true);
        if (guard.kickTicks <= 0) {
            guard.kickTicks = 10;
        }
        guard.doHurtTarget(guard.getTarget());
    }

    @Override
    public void stop() {
        guard.setKicking(false);
        guard.kickCoolDown = 50;
    }
}
