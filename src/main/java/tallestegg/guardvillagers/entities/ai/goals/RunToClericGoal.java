package tallestegg.guardvillagers.entities.ai.goals;

import java.util.List;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.potion.Effects;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.entities.GuardEntity;

public class RunToClericGoal extends Goal {
    public final GuardEntity guard;
    public VillagerEntity cleric;

    public RunToClericGoal(GuardEntity guard) {
        this.guard = guard;
    }

    @Override
    public boolean shouldExecute() {
        List<VillagerEntity> list = this.guard.world.getEntitiesWithinAABB(VillagerEntity.class, this.guard.getBoundingBox().grow(10.0D, 3.0D, 10.0D));
        if (!list.isEmpty()) {
            for (VillagerEntity mob : list) {
                if (mob != null) {
                    if (mob.getVillagerData().getProfession() == VillagerProfession.CLERIC && guard.getHealth() < guard.getMaxHealth() && guard.getAttackTarget() == null && !guard.isPotionActive(Effects.REGENERATION)) {
                        this.cleric = mob;
                        return GuardConfig.ClericHealing;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void tick() {
        guard.faceEntity(cleric, 30.0F, 30.0F);
        guard.getLookController().setLookPositionWithEntity(cleric, 30.0F, 30.0F);
        if (guard.getDistance(cleric) >= 6.0D) {
            guard.getNavigator().tryMoveToEntityLiving(cleric, 0.5D);
        } else {
            guard.getMoveHelper().strafe(-1.0F, 0.0F);
            guard.getNavigator().clearPath();
        }
    }
}
