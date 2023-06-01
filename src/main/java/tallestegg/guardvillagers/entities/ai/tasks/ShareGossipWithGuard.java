package tallestegg.guardvillagers.entities.ai.tasks;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import tallestegg.guardvillagers.GuardEntityType;
import tallestegg.guardvillagers.entities.Guard;

public class ShareGossipWithGuard extends Behavior<Villager> {
    public ShareGossipWithGuard() {
        super(ImmutableMap.of(MemoryModuleType.INTERACTION_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel pLevel, Villager pOwner) {
        return BehaviorUtils.targetIsValid(pOwner.getBrain(), MemoryModuleType.INTERACTION_TARGET, GuardEntityType.GUARD.get());
    }

    @Override
    protected boolean canStillUse(ServerLevel pLevel, Villager pEntity, long pGameTime) {
        return this.checkExtraStartConditions(pLevel, pEntity);
    }

    @Override
    protected void start(ServerLevel pLevel, Villager pEntity, long pGameTime) {
        Guard guard = (Guard) pEntity.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
        BehaviorUtils.lockGazeAndWalkToEachOther(pEntity, guard, 0.5F);
    }

    @Override
    protected void tick(ServerLevel pLevel, Villager pOwner, long pGameTime) {
        Guard guard = (Guard) pOwner.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
        if (pOwner.distanceToSqr(guard) < 5.0D) {
            BehaviorUtils.lockGazeAndWalkToEachOther(pOwner, guard, 0.5F);
            guard.gossip(pOwner, pGameTime);
        }
    }

    @Override
    protected void stop(ServerLevel pLevel, Villager pEntity, long pGameTime) {
        pEntity.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
    }
}
