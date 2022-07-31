package tallestegg.guardvillagers.entities.ai.goals;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import tallestegg.guardvillagers.entities.Guard;

import java.util.EnumSet;
import java.util.List;

public class GuardLookAtAndStopMovingWhenBeingTheInteractionTarget extends Goal {
    private final Guard guard;
    private Villager villager;

    public GuardLookAtAndStopMovingWhenBeingTheInteractionTarget(Guard guard) {
        this.guard = guard;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        List<Villager> list = this.guard.level.getEntitiesOfClass(Villager.class, guard.getBoundingBox().inflate(10.0D));
        if (!list.isEmpty()) {
            for (Villager villager : list) {
                if (villager.getBrain().hasMemoryValue(MemoryModuleType.INTERACTION_TARGET) && villager.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get().is(guard)) {
                    this.villager = villager;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        guard.getNavigation().stop();
        guard.lookAt(villager, 30.0F, 30.0F);
        guard.getLookControl().setLookAt(villager);
    }
}
