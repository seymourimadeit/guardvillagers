package tallestegg.guardvillagers.entities.ai.tasks;

import java.util.List;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.task.SpawnGolemTask;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.server.ServerWorld;

public class RepairGolemTask extends SpawnGolemTask {
    private IronGolemEntity golem;
    private boolean hasStartedHealing;

    public RepairGolemTask() {
        super();
    }

    @Override
    protected boolean shouldExecute(ServerWorld worldIn, VillagerEntity owner) {
        List<IronGolemEntity> list = owner.world.getEntitiesWithinAABB(IronGolemEntity.class, owner.getBoundingBox().grow(10.0D, 5.0D, 10.0D));
        if (!list.isEmpty()) {
            for (IronGolemEntity golem : list) {
                if (!golem.isInvisible() && golem.isAlive() && golem.getType() == EntityType.IRON_GOLEM) { // Check if the entity is an Iron Golem, not any other golem.
                    if (golem.getHealth() <= 60.0D || this.hasStartedHealing && golem.getHealth() < golem.getMaxHealth()) {
                        this.golem = golem;
                        owner.setItemStackToSlot(EquipmentSlotType.MAINHAND, new ItemStack(Items.IRON_INGOT));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void resetTask(ServerWorld worldIn, VillagerEntity entityIn, long gameTimeIn) {
        if (golem.getHealth() == golem.getMaxHealth()) {
            this.hasStartedHealing = false;
            entityIn.setItemStackToSlot(EquipmentSlotType.MAINHAND, ItemStack.EMPTY);
        }
    }

    @Override
    protected void startExecuting(ServerWorld worldIn, VillagerEntity entityIn, long gameTimeIn) {
        if (golem == null)
            return;
        entityIn.setItemStackToSlot(EquipmentSlotType.MAINHAND, new ItemStack(Items.IRON_INGOT));
        this.healGolem(entityIn);
    }

    @Override
    protected void updateTask(ServerWorld worldIn, VillagerEntity entityIn, long gameTimeIn) {
        if (golem.getHealth() < golem.getMaxHealth())
            this.healGolem(entityIn);
    }

    public void healGolem(VillagerEntity healer) {
        healer.getNavigator().tryMoveToEntityLiving(golem, 0.5);
        if (healer.getDistance(golem) <= 2.0D) {
            this.hasStartedHealing = true;
            healer.setItemStackToSlot(EquipmentSlotType.MAINHAND, new ItemStack(Items.IRON_INGOT));
            healer.swingArm(Hand.MAIN_HAND);
            golem.heal(15.0F);
            float pitch = 1.0F + (golem.getRNG().nextFloat() - golem.getRNG().nextFloat()) * 0.2F;
            golem.playSound(SoundEvents.ENTITY_IRON_GOLEM_REPAIR, 1.0F, pitch);
        }
    }
}
