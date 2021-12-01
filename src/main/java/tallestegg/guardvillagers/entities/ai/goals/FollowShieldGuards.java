package tallestegg.guardvillagers.entities.ai.goals;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import tallestegg.guardvillagers.entities.Guard;

public class FollowShieldGuards extends Goal {
    private static final TargetingConditions NEARBY_GUARDS = TargetingConditions.forNonCombat().range(8.0D)
            .ignoreLineOfSight();
    private final Guard taskOwner;
    private Guard guardtofollow;
    private double x;
    private double y;
    private double z;

    public FollowShieldGuards(Guard taskOwnerIn) {
        this.taskOwner = taskOwnerIn;
    }

    @Override
    public boolean canUse() {
        List<? extends Guard> list = this.taskOwner.level.getEntitiesOfClass(this.taskOwner.getClass(),
                this.taskOwner.getBoundingBox().inflate(8.0D, 8.0D, 8.0D));
        if (!list.isEmpty()) {
            for (Guard guard : list) {
                if (!guard.isInvisible() && guard.getOffhandItem().canPerformAction(net.minecraftforge.common.ToolActions.SHIELD_BLOCK) && guard.isBlocking()
                        && this.taskOwner.level
                                .getNearbyEntities(Guard.class, NEARBY_GUARDS.range(3.0D), guard,
                                        this.taskOwner.getBoundingBox().inflate(5.0D))
                                .size() < 5) {
                    this.guardtofollow = guard;
                    Vec3 vec3d = this.getPosition();
                    if (vec3d == null) {
                        return false;
                    } else {
                        this.x = vec3d.x;
                        this.y = vec3d.y;
                        this.z = vec3d.z;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Nullable
    protected Vec3 getPosition() {
        return DefaultRandomPos.getPosTowards(this.taskOwner, 16, 7, this.guardtofollow.position(), (double)((float)Math.PI / 2F));
    }

    @Override
    public boolean canContinueToUse() {
        return !this.taskOwner.getNavigation().isDone() && !this.taskOwner.isVehicle();
    }

    @Override
    public void stop() {
        this.taskOwner.getNavigation().stop();
        super.stop();
    }

    @Override
    public void start() {
        this.taskOwner.getNavigation().moveTo(x, y, z, 0.4D);
    }
}
