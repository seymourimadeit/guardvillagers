package tallestegg.guardvillagers.entities.ai.goals;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.npc.Villager;
import tallestegg.guardvillagers.entities.Guard;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class VillagerGossipToGuardGoal extends Goal {
    protected Villager villager;
    protected Guard guard;

    public VillagerGossipToGuardGoal(Villager villager) {
        this.villager = villager;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        List<Guard> list = this.villager.level.getEntitiesOfClass(Guard.class, this.villager.getBoundingBox().inflate(10.0D, 3.0D, 10.0D));
        if (!list.isEmpty()) {
            for (Guard mob : list) {
                long gameTime = mob.getLevel().getGameTime();
                if (mob.getSensing().hasLineOfSight(this.villager) && (gameTime < mob.lastGossipTime || gameTime >= mob.lastGossipTime + 1200L)) {
                    this.guard = mob;
                    return !nearbyVillagersInteractingWithGuards() && mob.getTarget() == null && !this.villager.getBrain().hasMemoryValue(MemoryModuleType.INTERACTION_TARGET); // Check if no other villager in a 10 block radius is interacting with the guard
                }
            }
        }
        return false;
    }

    @Override
    public void start() {
        this.villager.lookAt(guard, 30.0F, 30.0F);
        this.villager.getLookControl().setLookAt(guard, 30.0F, 30.0F);
        BehaviorUtils.lookAtEntity(villager, guard);
        villager.getBrain().setMemory(MemoryModuleType.INTERACTION_TARGET, guard);
    }

    @Override
    public void tick() {
        BehaviorUtils.lookAtEntity(villager, guard);
        if (this.villager.distanceTo(guard) > 2.0D) {
            this.villager.getNavigation().moveTo(guard, 0.5D);
        } else {
            this.villager.getNavigation().stop();
            guard.gossip((ServerLevel) guard.getLevel(), villager, guard.getLevel().getGameTime());
        }
        this.villager.lookAt(guard, 30.0F, 30.0F);
        this.villager.getLookControl().setLookAt(guard, 30.0F, 30.0F);
    }

    @Override
    public void stop() {
        villager.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
    }

    private boolean nearbyVillagersInteractingWithGuards() {
        if (villager.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)) {
            Optional<NearestVisibleLivingEntities> nearbyEntities = villager.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
            nearbyEntities.get().findClosest((otherVillager) -> {
                if (otherVillager.getType() == EntityType.VILLAGER)
                    return otherVillager.getBrain().hasMemoryValue(MemoryModuleType.INTERACTION_TARGET) && !(otherVillager.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get().is(guard));
                else
                    return false;
            });
        }
        return false;
    }
}
