package tallestegg.guardvillagers.entities.ai.goals;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import tallestegg.guardvillagers.entities.Guard;

import java.util.List;

// Should hopefully address the issues with guards repeatably opening doors
public class GuardInteractDoorGoal extends OpenDoorGoal {
    private Guard guard;

    public GuardInteractDoorGoal(Guard pMob, boolean pCloseDoor) {
        super(pMob, pCloseDoor);
        this.guard = pMob;
    }

    @Override
    public boolean canUse() {
        return super.canUse();
    }

    @Override
    public void start() {
        if (areOtherMobsComingThroughDoor(guard)) {
            super.start();
            guard.swing(InteractionHand.MAIN_HAND);
        }
    }

    private boolean areOtherMobsComingThroughDoor(Guard pEntity) {
        List<? extends PathfinderMob> nearbyEntityList = pEntity.level().getEntitiesOfClass(PathfinderMob.class,
                pEntity.getBoundingBox().inflate(4.0D));
        if (!nearbyEntityList.isEmpty()) {
            for (PathfinderMob mob : nearbyEntityList) {
                if (mob.blockPosition().closerToCenterThan(pEntity.position(), 2.0D))
                    return isMobComingThroughDoor(mob);
            }
        }
        return false;
    }

    private boolean isMobComingThroughDoor(PathfinderMob pEntity) {
        if (pEntity.getNavigation() == null) {
            return false;
        } else {
            Path path = pEntity.getNavigation().getPath();
            if (path == null || path.isDone()) {
                return false;
            } else {
                Node node = path.getPreviousNode();
                if (node == null) {
                    return false;
                } else {
                    Node node1 = path.getNextNode();
                    return pEntity.blockPosition().equals(node.asBlockPos()) || pEntity.blockPosition().equals(node1.asBlockPos());
                }
            }
        }
    }
}
