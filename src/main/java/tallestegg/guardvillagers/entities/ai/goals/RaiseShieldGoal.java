package tallestegg.guardvillagers.entities.ai.goals;

import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.RavagerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.entities.GuardEntity;

public class RaiseShieldGoal extends Goal {

    public final GuardEntity guard;

    public RaiseShieldGoal(GuardEntity guard) {
        this.guard = guard;
    }

    @Override
    public boolean shouldExecute() {
        return !CrossbowItem.isCharged(guard.getHeldItemMainhand()) && guard.getHeldItemOffhand().getItem().isShield(guard.getHeldItemOffhand(), guard) && raiseShield() && guard.shieldCoolDown == 0
                && !guard.getHeldItemOffhand().getItem().equals(ForgeRegistries.ITEMS.getValue(new ResourceLocation("bigbrain:buckler")));
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.shouldExecute();
    }

    @Override
    public void startExecuting() {
        if (guard.getHeldItemOffhand().getItem().isShield(guard.getHeldItemOffhand(), guard))
            guard.setActiveHand(Hand.OFF_HAND);
    }

    @Override
    public void resetTask() {
        if (!GuardConfig.GuardAlwaysShield)
            guard.resetActiveHand();
    }

    protected boolean raiseShield() {
        LivingEntity target = guard.getAttackTarget();
        if (target != null && guard.shieldCoolDown == 0) {
            boolean ranged = guard.getHeldItemMainhand().getItem() instanceof CrossbowItem || guard.getHeldItemMainhand().getItem() instanceof BowItem;
            if (guard.getDistance(target) <= 4.0D || target instanceof CreeperEntity || target instanceof IRangedAttackMob && target.getDistance(guard) >= 5.0D && !ranged || target instanceof RavagerEntity || GuardConfig.GuardAlwaysShield) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
}
