package tallestegg.guardvillagers.entities.ai.goals;

import java.util.EnumSet;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.passive.IronGolemEntity;
import tallestegg.guardvillagers.entities.GuardEntity;

public class HeroHurtByTargetGoal extends TargetGoal {
    private final GuardEntity guard;
    private LivingEntity attacker;
    private int timestamp;

    public HeroHurtByTargetGoal(GuardEntity guard) {
        super(guard, false);
        this.guard = guard;
        this.setMutexFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    public boolean shouldExecute() {
        LivingEntity livingentity = this.guard.getOwner();
        if (livingentity == null) {
            return false;
        } else {
            this.attacker = livingentity.getRevengeTarget();
            int i = livingentity.getRevengeTimer();
            return i != this.timestamp && this.isSuitableTarget(this.attacker, EntityPredicate.DEFAULT);
        }
    }

    @Override
    protected boolean isSuitableTarget(@Nullable LivingEntity potentialTarget, EntityPredicate targetPredicate) {
        return super.isSuitableTarget(potentialTarget, targetPredicate) && !(potentialTarget instanceof IronGolemEntity) && !(potentialTarget instanceof GuardEntity);
    }

    public void startExecuting() {
        this.goalOwner.setAttackTarget(this.attacker);
        LivingEntity livingentity = this.guard.getOwner();
        if (livingentity != null) {
            this.timestamp = livingentity.getRevengeTimer();
        }

        super.startExecuting();
    }
}
