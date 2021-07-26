package tallestegg.guardvillagers.entities.ai.goals;

import java.util.List;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.effect.MobEffects;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.entities.Guard;

public class RunToClericGoal extends Goal {
    public final Guard guard;
    public Villager cleric;

    public RunToClericGoal(Guard guard) {
        this.guard = guard;
    }

    @Override
    public boolean canUse() {
        List<Villager> list = this.guard.level.getEntitiesOfClass(Villager.class, this.guard.getBoundingBox().inflate(10.0D, 3.0D, 10.0D));
        if (!list.isEmpty()) {
            for (Villager mob : list) {
                if (mob != null) {
                    if (mob.getVillagerData().getProfession() == VillagerProfession.CLERIC && guard.getHealth() < guard.getMaxHealth() && guard.getTarget() == null && !guard.hasEffect(MobEffects.REGENERATION)) {
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
        guard.lookAt(cleric, 30.0F, 30.0F);
        guard.getLookControl().setLookAt(cleric, 30.0F, 30.0F);
        if (guard.distanceTo(cleric) >= 6.0D) {
            guard.getNavigation().moveTo(cleric, 0.5D);
        } else {
            guard.getMoveControl().strafe(-1.0F, 0.0F);
            guard.getNavigation().stop();
        }
    }
}
