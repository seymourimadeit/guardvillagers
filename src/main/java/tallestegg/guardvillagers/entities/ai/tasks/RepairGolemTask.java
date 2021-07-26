package tallestegg.guardvillagers.entities.ai.tasks;

import java.util.List;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.behavior.WorkAtPoi;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;

public class RepairGolemTask extends WorkAtPoi {
    private IronGolem golem;
    private boolean hasStartedHealing;

    public RepairGolemTask() {
        super();
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, Villager owner) {
        List<IronGolem> list = owner.level.getEntitiesOfClass(IronGolem.class, owner.getBoundingBox().inflate(10.0D, 5.0D, 10.0D));
        if (!list.isEmpty()) {
            for (IronGolem golem : list) {
                if (!golem.isInvisible() && golem.isAlive() && golem.getType() == EntityType.IRON_GOLEM) { // Check if the entity is an Iron Golem, not any other golem.
                    if (golem.getHealth() <= 60.0D || this.hasStartedHealing && golem.getHealth() < golem.getMaxHealth()) {
                        this.golem = golem;
                        owner.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_INGOT));
                        return true;
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
        healer.getNavigation().moveTo(golem, 0.5);
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
