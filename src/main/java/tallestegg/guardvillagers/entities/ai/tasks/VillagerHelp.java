package tallestegg.guardvillagers.entities.ai.tasks;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;

import java.util.List;
import java.util.Map;

public class VillagerHelp extends Behavior<Villager> {
    protected final List<? extends String> allowedProfessions;

    public VillagerHelp(Map<MemoryModuleType<?>, MemoryStatus> entryCondition, List<? extends String> allowedProfessions) {
        super(entryCondition);
        this.allowedProfessions = allowedProfessions;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Villager owner) {
        Activity activity = owner.getBrain().getActiveNonCoreActivity().orElse(null);
        if (!checkIfDayHavePassedFromLastActivity(owner))
            return false;
        else
            return this.allowedProfessions.contains(owner.getVillagerData().getProfession().name()) && !owner.isSleeping() && activity != Activity.AVOID && activity != Activity.HIDE && activity != Activity.PANIC;
    }

    protected boolean checkIfDayHavePassedFromLastActivity(LivingEntity owner) {
        long gameTime = owner.level().getDayTime();
        if (timeToCheck(owner) > 0 && gameTime - timeToCheck(owner) < 24000L)
            return false;
        else if (timeToCheck(owner) <= 0)
            return true;
        else
            return true;
    }

    protected long timeToCheck(LivingEntity owner) {
        return 0;
    }
}