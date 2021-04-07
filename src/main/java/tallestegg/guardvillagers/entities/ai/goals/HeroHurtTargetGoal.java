package tallestegg.guardvillagers.entities.ai.goals;

import java.util.EnumSet;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import tallestegg.guardvillagers.entities.GuardEntity;

public class HeroHurtTargetGoal extends TargetGoal {
    private final GuardEntity guard;
    private LivingEntity attacker;
    private int timestamp;

    public HeroHurtTargetGoal(GuardEntity theEntityTameableIn) {
        super(theEntityTameableIn, false);
        this.guard = theEntityTameableIn;
        this.setMutexFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    public boolean shouldExecute() {
        LivingEntity livingentity = this.guard.getOwner();
        if (livingentity == null) {
            return false;
        } else {
            this.attacker = livingentity.getLastAttackedEntity();
            int i = livingentity.getLastAttackedEntityTime();
            return i != this.timestamp && this.isSuitableTarget(this.attacker, EntityPredicate.DEFAULT);
        }
    }

    @Override
    protected boolean isSuitableTarget(@Nullable LivingEntity potentialTarget, EntityPredicate targetPredicate) {
        return super.isSuitableTarget(potentialTarget, targetPredicate) && !(potentialTarget instanceof AbstractVillagerEntity) && !(potentialTarget instanceof GuardEntity);
    }

    @Override
    public void startExecuting() {
        this.goalOwner.setAttackTarget(this.attacker);
        LivingEntity livingentity = this.guard.getOwner();
        if (livingentity != null) {
            this.timestamp = livingentity.getLastAttackedEntityTime();
        }
        super.startExecuting();
    }
}
