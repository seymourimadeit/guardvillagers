package tallestegg.guardvillagers.common.entities.ai.tasks;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import tallestegg.guardvillagers.GuardEntityType;
import tallestegg.guardvillagers.common.entities.Guard;

import java.util.Set;
import java.util.function.Predicate;

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
        BehaviorUtils.lockGazeAndWalkToEachOther(pEntity, guard, 0.5F, 2);
    }

    @Override
    protected void tick(ServerLevel pLevel, Villager pOwner, long pGameTime) {
        Guard guard = (Guard) pOwner.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
        if (pOwner.distanceToSqr(guard) < 5.0D) {
            BehaviorUtils.lockGazeAndWalkToEachOther(pOwner, guard, 0.5F, 2);
            guard.gossip(pOwner, pGameTime);
        }
        if (pOwner.hasExcessFood() && guard.getOffhandItem().isEmpty()) {
            throwHalfStack(pOwner, itemStack -> itemStack.has(DataComponents.VILLAGER_FOOD), guard);
        }
    }

    @Override
    protected void stop(ServerLevel pLevel, Villager pEntity, long pGameTime) {
        pEntity.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
    }

    // From the TradeWithVillager class
    private static void throwHalfStack(Villager pVillager, Predicate<ItemStack> predicate, LivingEntity pEntity) {
        SimpleContainer inventory = pVillager.getInventory();
        ItemStack toThrow = ItemStack.EMPTY;
        int i = 0;

        while (i < inventory.getContainerSize()) {
            ItemStack itemStack;
            int count;
            label28: {
                itemStack = inventory.getItem(i);
                if (!itemStack.isEmpty() && predicate.test(itemStack)) {
                    if (itemStack.getCount() > itemStack.getMaxStackSize() / 2) {
                        count = itemStack.getCount() / 2;
                        break label28;
                    }

                    if (itemStack.getCount() > 24) {
                        count = itemStack.getCount() - 24;
                        break label28;
                    }
                }

                i++;
                continue;
            }

            toThrow = itemStack.split(count);
            break;
        }

        if (!toThrow.isEmpty()) {
            BehaviorUtils.throwItem(pVillager, toThrow, pEntity.position());
        }
    }
}
