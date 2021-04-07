package tallestegg.guardvillagers.entities.ai.goals;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;

public class HealGolemGoal extends Goal {
    public final MobEntity healer;
    public IronGolemEntity golem;
    public boolean hasStartedHealing;

    public HealGolemGoal(MobEntity mob) {
        healer = mob;
        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean shouldExecute() {
        if (((VillagerEntity) this.healer).getVillagerData().getProfession() != VillagerProfession.WEAPONSMITH && (((VillagerEntity) this.healer).getVillagerData().getProfession() != VillagerProfession.TOOLSMITH)
                && (((VillagerEntity) this.healer).getVillagerData().getProfession() != VillagerProfession.ARMORER) || this.healer.isSleeping()) {
            return false;
        }
        List<IronGolemEntity> list = this.healer.world.getEntitiesWithinAABB(IronGolemEntity.class, this.healer.getBoundingBox().grow(10.0D));
        if (!list.isEmpty()) {
            for (IronGolemEntity golem : list) {
                if (!golem.isInvisible() && golem.isAlive() && golem.getType() == EntityType.IRON_GOLEM) { // Check if the entity is an Iron Golem, not any other golem.
                    if (golem.getHealth() <= 60.0D || this.hasStartedHealing && golem.getHealth() < golem.getMaxHealth()) {
                        healer.setItemStackToSlot(EquipmentSlotType.MAINHAND, new ItemStack(Items.IRON_INGOT));
                        this.golem = golem;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void resetTask() {
        healer.setItemStackToSlot(EquipmentSlotType.MAINHAND, ItemStack.EMPTY);
        this.hasStartedHealing = false;
        super.resetTask();
    }

    @Override
    public void startExecuting() {
        if (golem == null)
            return;
        healer.setItemStackToSlot(EquipmentSlotType.MAINHAND, new ItemStack(Items.IRON_INGOT));
        this.healGolem();
    }

    @Override
    public void tick() {
        if (golem.getHealth() < golem.getMaxHealth()) {
            this.healGolem();
        }
    }

    public void healGolem() {
        healer.setItemStackToSlot(EquipmentSlotType.MAINHAND, new ItemStack(Items.IRON_INGOT));
        healer.getNavigator().tryMoveToEntityLiving(golem, 0.5);
        if (healer.getDistance(golem) <= 2.0D) {
            this.hasStartedHealing = true;
            healer.swingArm(Hand.MAIN_HAND);
            golem.heal(15.0F);
            float f1 = 1.0F + (golem.getRNG().nextFloat() - golem.getRNG().nextFloat()) * 0.2F;
            golem.playSound(SoundEvents.ENTITY_IRON_GOLEM_REPAIR, 1.0F, f1);
        }
    }

}
