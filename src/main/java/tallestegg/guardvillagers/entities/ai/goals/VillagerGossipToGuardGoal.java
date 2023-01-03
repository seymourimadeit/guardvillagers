package tallestegg.guardvillagers.entities.ai.goals;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import tallestegg.guardvillagers.entities.Guard;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class VillagerGossipToGuardGoal extends Goal {
    protected final Villager villager;
    protected Guard guard;

    public VillagerGossipToGuardGoal(Villager villager) {
        this.villager = villager;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.villager.getBrain().hasMemoryValue(MemoryModuleType.INTERACTION_TARGET) && this.villager.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get() instanceof Guard guard) {
            this.guard = guard;
            long gameTime = guard.getLevel().getGameTime();
            if (!nearbyVillagersInteractingWithGuards() && (gameTime < this.guard.lastGossipTime || gameTime >= this.guard.lastGossipTime + 1200L))
                return this.guard.getTarget() == null && !this.villager.level.isNight(); // Check if no other villager in a 10 block radius is interacting with the guar
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return !nearbyVillagersInteractingWithGuards() && guard.getTarget() == null && this.villager.getBrain().hasMemoryValue(MemoryModuleType.INTERACTION_TARGET) && this.villager.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get().is(guard);
    }

    @Override
    public void start() {
        this.villager.getBrain().setMemory(MemoryModuleType.INTERACTION_TARGET, guard);
    }

    @Override
    public void tick() {
        this.villager.getBrain().setMemory(MemoryModuleType.INTERACTION_TARGET, guard);
        if (!nearbyVillagersInteractingWithGuards() && this.villager.getBrain().hasMemoryValue(MemoryModuleType.INTERACTION_TARGET) && this.villager.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get().is(guard)) {
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
    }

    @Override
    public void stop() {
        this.villager.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
    }

    private boolean nearbyVillagersInteractingWithGuards() {
        if (villager.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_LIVING_ENTITIES)) {
            Optional<List<LivingEntity>> list = villager.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES);
            for (LivingEntity entity : list.get()) {
                if (entity instanceof Villager nearbyVillager) {
                    if (nearbyVillager.getBrain().hasMemoryValue(MemoryModuleType.INTERACTION_TARGET))
                        return nearbyVillager.getBrain().hasMemoryValue(MemoryModuleType.INTERACTION_TARGET) && nearbyVillager.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get().is(guard);
                }
            }
        }
        return false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
