package tallestegg.guardvillagers.common.entities.ai.tasks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import tallestegg.guardvillagers.GuardMemoryTypes;
import tallestegg.guardvillagers.configuration.GuardConfig;

import java.util.List;
import java.util.Optional;

public class RepairGolem extends VillagerHelp {
    private LivingEntity golem;

    public RepairGolem() {
        super(ImmutableMap.of(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT), GuardConfig.COMMON.professionsThatRepairGolems.get());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, Villager owner) {
        List<LivingEntity> list = owner.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).get();
        if (!list.isEmpty()) {
            for (LivingEntity golem : list) {
                if (!golem.isInvisible() && golem.isAlive() && golem.getType() == EntityType.IRON_GOLEM) { // Check only for iron golems and if a day has passed since the last time a golem was healed
                    if (golem.getHealth() <= (golem.getMaxHealth() * 0.75F)) {
                        this.golem = golem;
                        return super.checkExtraStartConditions(worldIn, owner);
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected long timeToCheck(LivingEntity owner) {
        Optional<Long> optional = owner.getBrain().getMemory(GuardMemoryTypes.LAST_REPAIRED_GOLEM.get());
        return optional.isPresent() ? optional.get() : 0;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Villager entity, long gameTime) {
        return entity.getBrain().getMemory(GuardMemoryTypes.TIMES_HEALED_GOLEM.get()).orElse(null) < GuardConfig.COMMON.maxGolemRepair.get() && this.golem.getHealth() <= this.golem.getMaxHealth();
    }

    @Override
    protected void stop(ServerLevel worldIn, Villager entityIn, long gameTimeIn) {
        if (entityIn.getBrain().getMemory(GuardMemoryTypes.TIMES_HEALED_GOLEM.get()).orElse(null) >= GuardConfig.COMMON.maxGolemRepair.get()) {
            entityIn.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            entityIn.getBrain().setMemory(GuardMemoryTypes.LAST_REPAIRED_GOLEM.get(), worldIn.getDayTime());
            entityIn.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            entityIn.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
            entityIn.getBrain().setMemory(GuardMemoryTypes.TIMES_HEALED_GOLEM.get(), 0);
        }
    }

    @Override
    protected void start(ServerLevel worldIn, Villager entityIn, long gameTimeIn) {
        if (golem == null) return;
        if (!entityIn.getBrain().hasMemoryValue(GuardMemoryTypes.TIMES_HEALED_GOLEM.get()))
            entityIn.getBrain().setMemory(GuardMemoryTypes.TIMES_HEALED_GOLEM.get(), 0);
    }

    @Override
    protected void tick(ServerLevel worldIn, Villager entityIn, long gameTimeIn) {
        this.healGolem(entityIn);
    }

    public void healGolem(Villager healer) {
        BehaviorUtils.setWalkAndLookTargetMemories(healer, golem, 0.5F, 0);
        if (healer.distanceTo(golem) <= 2.0D) {
            healer.getBrain().setMemory(GuardMemoryTypes.TIMES_HEALED_GOLEM.get(), healer.getBrain().getMemory(GuardMemoryTypes.TIMES_HEALED_GOLEM.get()).orElse(null) + 1);
            healer.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_INGOT));
            healer.swing(InteractionHand.MAIN_HAND);
            golem.heal(15.0F);
            float pitch = 1.0F + (golem.getRandom().nextFloat() - golem.getRandom().nextFloat()) * 0.2F;
            golem.playSound(SoundEvents.IRON_GOLEM_REPAIR, 1.0F, pitch);
        }
    }
}
