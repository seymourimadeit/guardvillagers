package tallestegg.guardvillagers.common.entities.ai.tasks;

import java.util.List;

import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;

public class RepairGolem extends Behavior<Villager> {
    private LivingEntity golem;
    private boolean hasStartedHealing;
    private long lastTimeSinceGolemHeal = 0;

    public RepairGolem() {
        super(ImmutableMap.of(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, Villager owner) {
        if (owner.getVillagerData().getProfession() != VillagerProfession.WEAPONSMITH && (owner.getVillagerData().getProfession() != VillagerProfession.TOOLSMITH)
                && (owner.getVillagerData().getProfession() != VillagerProfession.ARMORER) || owner.isSleeping()) {
            return false;
        }
        long gameTime = worldIn.getGameTime();
        if (gameTime - this.lastTimeSinceGolemHeal < 24000L) {
            return false;
        } else {
            List<LivingEntity> list = owner.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).get();
            if (!list.isEmpty()) {
                for (LivingEntity golem : list) {
                    if (!golem.isInvisible() && golem.isAlive() && golem.getType() == EntityType.IRON_GOLEM) { // Check only for iron golems and if a day has passed since the last time a golem was healed
                        if (golem.getHealth() <= (golem.getMaxHealth() * 0.75F) || this.hasStartedHealing && golem.getHealth() < golem.getMaxHealth()) {
                            this.golem = golem;
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void stop(ServerLevel worldIn, Villager entityIn, long gameTimeIn) {
        if (golem.getHealth() == golem.getMaxHealth()) {
            this.hasStartedHealing = false;
            entityIn.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            this.lastTimeSinceGolemHeal = entityIn.level().getGameTime();
            entityIn.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            entityIn.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        }
    }

    @Override
    protected void start(ServerLevel worldIn, Villager entityIn, long gameTimeIn) {
        if (golem == null)
            return;
        entityIn.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_INGOT));
        this.healGolem(entityIn);
    }

    @Override
    protected void tick(ServerLevel worldIn, Villager entityIn, long gameTimeIn) {
        if (golem.getHealth() < golem.getMaxHealth())
            this.healGolem(entityIn);
    }

    public void healGolem(Villager healer) {
        BehaviorUtils.setWalkAndLookTargetMemories(healer, golem, 0.5F, 0);
        if (healer.distanceTo(golem) <= 2.0D) {
            this.hasStartedHealing = true;
            healer.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_INGOT));
            healer.swing(InteractionHand.MAIN_HAND);
            golem.heal(15.0F);
            float pitch = 1.0F + (golem.getRandom().nextFloat() - golem.getRandom().nextFloat()) * 0.2F;
            golem.playSound(SoundEvents.IRON_GOLEM_REPAIR, 1.0F, pitch);
        }
    }
}
