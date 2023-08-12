package tallestegg.guardvillagers.entities.ai.tasks;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import tallestegg.guardvillagers.GuardEntityType;
import tallestegg.guardvillagers.entities.Guard;

import java.util.Set;

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
            guard.gossip(pLevel, pOwner, pGameTime);
        }
        if (pOwner.hasExcessFood() && guard.getOffhandItem().isEmpty()) {
            throwHalfStack(pOwner, Villager.FOOD_POINTS.keySet(), guard);
        }
    }

    @Override
    protected void stop(ServerLevel pLevel, Villager pEntity, long pGameTime) {
        pEntity.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
    }

    // From the TradeWithVillager class
    private static void throwHalfStack(Villager pVillager, Set<Item> pStack, LivingEntity pEntity) {
        SimpleContainer simplecontainer = pVillager.getInventory();
        ItemStack itemstack = ItemStack.EMPTY;
        int i = 0;

        while(i < simplecontainer.getContainerSize()) {
            ItemStack itemstack1;
            Item item;
            int j;
            label28: {
                itemstack1 = simplecontainer.getItem(i);
                if (!itemstack1.isEmpty()) {
                    item = itemstack1.getItem();
                    if (pStack.contains(item)) {
                        if (itemstack1.getCount() > itemstack1.getMaxStackSize() / 2) {
                            j = itemstack1.getCount() / 2;
                            break label28;
                        }

                        if (itemstack1.getCount() > 24) {
                            j = itemstack1.getCount() - 24;
                            break label28;
                        }
                    }
                }

                ++i;
                continue;
            }

            itemstack1.shrink(j);
            itemstack = new ItemStack(item, j);
            break;
        }

        if (!itemstack.isEmpty()) {
            pEntity.setItemSlot(EquipmentSlot.OFFHAND, itemstack);
        }
    }
}
