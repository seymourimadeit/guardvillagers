package tallestegg.guardvillagers.entities.ai.goals;

import java.util.EnumSet;

import javax.annotation.Nullable;

import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.npc.AbstractVillager;
import tallestegg.guardvillagers.entities.Guard;

public class HeroHurtTargetGoal extends TargetGoal {
    private final Guard guard;
    private LivingEntity attacker;
    private int timestamp;

    public HeroHurtTargetGoal(Guard theEntityTameableIn) {
        super(theEntityTameableIn, false);
        this.guard = theEntityTameableIn;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    public boolean canUse() {
        LivingEntity livingentity = this.guard.getOwner();
        if (livingentity == null) {
            return false;
        } else {
            this.attacker = livingentity.getLastHurtMob();
            int i = livingentity.getLastHurtMobTimestamp();
            return i != this.timestamp && this.canAttack(this.attacker, TargetingConditions.DEFAULT);
        }
    }

    @Override
    protected boolean canAttack(@Nullable LivingEntity potentialTarget, TargetingConditions targetPredicate) {
        return super.canAttack(potentialTarget, targetPredicate) && !(potentialTarget instanceof AbstractVillager) && !(potentialTarget instanceof Guard);
    }

    @Override
    public void start() {
        this.mob.setTarget(this.attacker);
        LivingEntity livingentity = this.guard.getOwner();
        if (livingentity != null) {
            this.timestamp = livingentity.getLastHurtMobTimestamp();
        }
        super.start();
    }
}
