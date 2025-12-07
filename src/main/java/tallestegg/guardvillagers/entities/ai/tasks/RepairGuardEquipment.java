package tallestegg.guardvillagers.entities.ai.tasks;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.entities.Guard;
import tallestegg.guardvillagers.entities.GuardVillagersVillagerData;

import java.util.List;
import java.util.Optional;

public class RepairGuardEquipment extends VillagerHelp {
    private Guard guard;

    public RepairGuardEquipment() {
        super(ImmutableMap.of(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT), GuardConfig.COMMON.professionsThatRepairGuards.get());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, Villager owner) {
        List<LivingEntity> list = owner.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).get();
        if (!list.isEmpty()) {
            for (LivingEntity livingEntity : list) {
                if (!livingEntity.isInvisible() && livingEntity.isAlive() && livingEntity instanceof Guard guard) { // Check only for iron golems and if a day has passed since the last time a golem was healed
                    if (owner.getVillagerData().getProfession() == VillagerProfession.ARMORER) {
                        for (int i = 0; i < guard.guardInventory.getContainerSize() - 2; ++i) {
                            ItemStack itemstack = guard.guardInventory.getItem(i);
                            if (itemstack.isDamaged() && itemstack.getItem() instanceof ArmorItem && itemstack.getDamageValue() >= (itemstack.getMaxDamage() / 2)) {
                                this.guard = guard;
                                return super.checkExtraStartConditions(worldIn, owner);
                            }
                        }
                    } else {
                        for (int i = 4; i < 6; ++i) {
                            ItemStack itemstack = guard.guardInventory.getItem(i);
                            if (itemstack.isDamaged() && itemstack.getDamageValue() >= (itemstack.getMaxDamage() / 2)) {
                                this.guard = guard;
                                return super.checkExtraStartConditions(worldIn, owner);
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected long timeToCheck(LivingEntity owner) {
        return ((GuardVillagersVillagerData) owner).getLastRepairedGuard();
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Villager entity, long gameTime) {
        return ((GuardVillagersVillagerData) entity).getTimesRepairedGuard() < GuardConfig.COMMON.maxVillageRepair.get();
    }

    @Override
    protected void stop(ServerLevel worldIn, Villager entityIn, long gameTimeIn) {
        if (((GuardVillagersVillagerData) entityIn).getTimesRepairedGuard() >= GuardConfig.COMMON.maxVillageRepair.get()) {
            ((GuardVillagersVillagerData) entityIn).setLastRepairedGuard(worldIn.getDayTime());
            ((GuardVillagersVillagerData) entityIn).setTimesRepairedGuard(0);
            entityIn.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            entityIn.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
            float pitch = 1.0F + (guard.getRandom().nextFloat() - guard.getRandom().nextFloat()) * 0.2F;
            guard.playSound(SoundEvents.ANVIL_USE, 1.0F, pitch);
        }
    }

    @Override
    protected void start(ServerLevel worldIn, Villager entityIn, long gameTimeIn) {
        if (guard == null) return;
    }

    @Override
    protected void tick(ServerLevel worldIn, Villager entityIn, long gameTimeIn) {
        this.repairGuardEquipment(entityIn);
    }

    public void repairGuardEquipment(Villager healer) {
        BehaviorUtils.setWalkAndLookTargetMemories(healer, guard, 0.5F, 0);
        if (healer.distanceTo(guard) <= 2.0D) {
            ((GuardVillagersVillagerData) healer).setTimesRepairedGuard(((GuardVillagersVillagerData) healer).getTimesRepairedGuard() + 1);
            VillagerProfession profession = healer.getVillagerData().getProfession();
            if (profession == VillagerProfession.ARMORER) {
                for (int i = 0; i < guard.guardInventory.getContainerSize() - 2; ++i) {
                    ItemStack itemstack = guard.guardInventory.getItem(i);
                    if (itemstack.isDamaged() && itemstack.getItem() instanceof ArmorItem && itemstack.getDamageValue() >= (itemstack.getMaxDamage() / 2) + guard.getRandom().nextInt(5)) {
                        itemstack.setDamageValue(itemstack.getDamageValue() - guard.getRandom().nextInt(5));
                    }
                }
            } else {
                for (int i = 4; i < 6; ++i) {
                    ItemStack itemstack = guard.guardInventory.getItem(i);
                    if (itemstack.isDamaged() && itemstack.getDamageValue() >= (itemstack.getMaxDamage() / 2) + guard.getRandom().nextInt(5)) {
                        itemstack.setDamageValue(itemstack.getDamageValue() - guard.getRandom().nextInt(5));
                    }
                }
            }
        }
    }
}