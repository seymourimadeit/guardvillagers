package tallestegg.guardvillagers.entities.ai.goals;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import tallestegg.guardvillagers.entities.Guard;

import java.util.List;

public class VillagerGossipToGuardGoal extends Goal {
    protected Villager villager;
    protected Guard guard;

    public VillagerGossipToGuardGoal(Villager villager) {
        this.villager = villager;
    }

    @Override
    public boolean canUse() {
        List<Guard> list = this.villager.level.getEntitiesOfClass(Guard.class, this.villager.getBoundingBox().inflate(10.0D, 3.0D, 10.0D));
        if (!list.isEmpty()) {
            for (Guard mob : list) {
                long gameTime = mob.getLevel().getGameTime();
                if (mob.getSensing().hasLineOfSight(villager) && (gameTime < mob.lastGossipTime || gameTime >= mob.lastGossipTime + 1200L)) {
                    this.guard = mob;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void start() {
        this.guard.lookAt(villager, 30.0F, 30.0F);
        this.guard.getLookControl().setLookAt(villager, 30.0F, 30.0F);
        this.villager.lookAt(guard, 30.0F, 30.0F);
        this.villager.getLookControl().setLookAt(guard, 30.0F, 30.0F);
        BehaviorUtils.lookAtEntity(villager, guard);
        villager.getBrain().setMemory(MemoryModuleType.INTERACTION_TARGET, guard);
    }

    @Override
    public void tick() {
        if (this.villager.distanceTo(guard) > 5.0D) {
            this.villager.getNavigation().moveTo(guard, 0.5D);
        } else {
            this.villager.getNavigation().stop();
            guard.gossip((ServerLevel) guard.getLevel(), villager, guard.getLevel().getGameTime());
        }
        this.guard.getNavigation().stop();
        BehaviorUtils.lookAtEntity(villager, guard);
        this.guard.lookAt(villager, 30.0F, 30.0F);
        this.guard.getLookControl().setLookAt(villager, 30.0F, 30.0F);
        this.villager.lookAt(guard, 30.0F, 30.0F);
        this.villager.getLookControl().setLookAt(guard, 30.0F, 30.0F);
    }

    @Override
    public void stop() {
        villager.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
    }
}
