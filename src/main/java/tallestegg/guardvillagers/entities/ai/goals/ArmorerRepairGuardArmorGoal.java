package tallestegg.guardvillagers.entities.ai.goals;

import java.util.List;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import tallestegg.guardvillagers.entities.Guard;

public class ArmorerRepairGuardArmorGoal extends Goal {
    private final Guard guard;
    private Villager villager;

    public ArmorerRepairGuardArmorGoal(Guard guard) {
        this.guard = guard;
    }

    @Override
    public boolean canUse() {
        List<Villager> list = this.guard.level.getEntitiesOfClass(Villager.class, this.guard.getBoundingBox().inflate(10.0D, 3.0D, 10.0D));
        if (!list.isEmpty()) {
            for (Villager mob : list) {
                if (mob != null) {
                    boolean isArmorerOrWeaponSmith = mob.getVillagerData().getProfession() == VillagerProfession.ARMORER || mob.getVillagerData().getProfession() == VillagerProfession.WEAPONSMITH;
                    if (isArmorerOrWeaponSmith && guard.getTarget() == null) {
                        if (mob.getVillagerData().getProfession() == VillagerProfession.ARMORER) {
                            for (int i = 0; i < guard.guardInventory.getContainerSize() - 2; ++i) {
                                ItemStack itemstack = guard.guardInventory.getItem(i);
                                if (itemstack.isDamaged() && itemstack.getItem() instanceof ArmorItem && itemstack.getDamageValue() >= itemstack.getMaxDamage() / 2) {
                                    this.villager = mob;
                                    return true;
                                }
                            }
                        }
                        if (mob.getVillagerData().getProfession() == VillagerProfession.WEAPONSMITH) {
                            for (int i = 4; i < 6; ++i) {
                                ItemStack itemstack = guard.guardInventory.getItem(i);
                                if (itemstack.isDamaged() && itemstack.getDamageValue() >= itemstack.getMaxDamage() / 2) {
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
        guard.getLookControl().setLookAt(villager, 30.0F, 30.0F);
        if (guard.distanceTo(villager) >= 2.0D) {
            guard.getNavigation().moveTo(villager, 0.5D);
            villager.getNavigation().moveTo(guard, 0.5D);
        } else {
            VillagerProfession profession = villager.getVillagerData().getProfession();
            if (profession == VillagerProfession.ARMORER) {
                for (int i = 0; i < guard.guardInventory.getContainerSize() - 2; ++i) {
                    ItemStack itemstack = guard.guardInventory.getItem(i);
                    if (itemstack.isDamaged() && itemstack.getItem() instanceof ArmorItem && itemstack.getDamageValue() >= itemstack.getMaxDamage() / 2 + guard.getRandom().nextInt(5)) {
                        itemstack.setDamageValue(itemstack.getDamageValue() - guard.getRandom().nextInt(5));
                    }
                }
            }
            if (profession == VillagerProfession.WEAPONSMITH) {
                for (int i = 4; i < 6; ++i) {
                    ItemStack itemstack = guard.guardInventory.getItem(i);
                    if (itemstack.isDamaged() && itemstack.getDamageValue() >= itemstack.getMaxDamage() / 2 + guard.getRandom().nextInt(5)) {
                        itemstack.setDamageValue(itemstack.getDamageValue() - guard.getRandom().nextInt(5));
                    }
                }
            }
        }
    }
}
