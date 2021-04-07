package tallestegg.guardvillagers.entities.ai.goals;

import java.util.List;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import tallestegg.guardvillagers.entities.GuardEntity;

public class ArmorerRepairGuardArmorGoal extends Goal {
    private final GuardEntity guard;
    private VillagerEntity villager;

    public ArmorerRepairGuardArmorGoal(GuardEntity guard) {
        this.guard = guard;
    }

    @Override
    public boolean shouldExecute() {
        List<VillagerEntity> list = this.guard.world.getEntitiesWithinAABB(VillagerEntity.class, this.guard.getBoundingBox().grow(10.0D, 3.0D, 10.0D));
        if (!list.isEmpty()) {
            for (VillagerEntity mob : list) {
                if (mob != null) {
                    boolean isArmorerOrWeaponSmith = mob.getVillagerData().getProfession() == VillagerProfession.ARMORER || mob.getVillagerData().getProfession() == VillagerProfession.WEAPONSMITH;
                    if (isArmorerOrWeaponSmith && guard.getAttackTarget() == null) {
                        if (mob.getVillagerData().getProfession() == VillagerProfession.ARMORER) {
                            for (int i = 0; i < guard.guardInventory.getSizeInventory() - 2; ++i) {
                                ItemStack itemstack = guard.guardInventory.getStackInSlot(i);
                                if (itemstack.isDamaged() && itemstack.getItem() instanceof ArmorItem && itemstack.getDamage() >= itemstack.getMaxDamage() / 2) {
                                    this.villager = mob;
                                    return true;
                                }
                            }
                        }
                        if (mob.getVillagerData().getProfession() == VillagerProfession.WEAPONSMITH) {
                            for (int i = 4; i < 6; ++i) {
                                ItemStack itemstack = guard.guardInventory.getStackInSlot(i);
                                if (itemstack.isDamaged() && itemstack.getDamage() >= itemstack.getMaxDamage() / 2) {
                                    this.villager = mob;
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void tick() {
        guard.getLookController().setLookPositionWithEntity(villager, 30.0F, 30.0F);
        if (guard.getDistance(villager) >= 2.0D) {
            guard.getNavigator().tryMoveToEntityLiving(villager, 0.5D);
            villager.getNavigator().tryMoveToEntityLiving(guard, 0.5D);
        } else {
            VillagerProfession profession = villager.getVillagerData().getProfession();
            if (profession == VillagerProfession.ARMORER) {
                for (int i = 0; i < guard.guardInventory.getSizeInventory() - 2; ++i) {
                    ItemStack itemstack = guard.guardInventory.getStackInSlot(i);
                    if (itemstack.isDamaged() && itemstack.getItem() instanceof ArmorItem && itemstack.getDamage() >= itemstack.getMaxDamage() / 2 + guard.getRNG().nextInt(5)) {
                        itemstack.setDamage(itemstack.getDamage() - guard.getRNG().nextInt(5));
                    }
                }
            }
            if (profession == VillagerProfession.WEAPONSMITH) {
                for (int i = 4; i < 6; ++i) {
                    ItemStack itemstack = guard.guardInventory.getStackInSlot(i);
                    if (itemstack.isDamaged() && itemstack.getDamage() >= itemstack.getMaxDamage() / 2 + guard.getRNG().nextInt(5)) {
                        itemstack.setDamage(itemstack.getDamage() - guard.getRNG().nextInt(5));
                    }
                }
            }
        }
    }
}
