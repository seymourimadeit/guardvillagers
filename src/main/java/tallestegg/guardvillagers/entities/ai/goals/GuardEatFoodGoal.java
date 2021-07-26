package tallestegg.guardvillagers.entities.ai.goals;

import java.util.List;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SplashPotionItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.InteractionHand;
import tallestegg.guardvillagers.entities.Guard;

public class GuardEatFoodGoal extends Goal {
    public final Guard guard;

    public GuardEatFoodGoal(Guard guard) {
        this.guard = guard;
    }

    @Override
    public boolean canUse() {
        return !guard.isRunningToEat() && guard.getHealth() < guard.getMaxHealth() && GuardEatFoodGoal.isConsumable(guard.getOffhandItem()) && guard.isEating() || guard.getHealth() < guard.getMaxHealth() && GuardEatFoodGoal.isConsumable(guard.getOffhandItem()) && guard.getTarget() == null && !guard.isAggressive();
    }

    public static boolean isConsumable(ItemStack stack) {
        return stack.getUseAnimation() == UseAnim.EAT  && stack.getCount() > 0 || stack.getUseAnimation() == UseAnim.DRINK && !(stack.getItem() instanceof SplashPotionItem) && stack.getCount() > 0;
    }
    
    @Override
    public boolean canContinueToUse() {
        List<LivingEntity> list = this.guard.level.getEntitiesOfClass(LivingEntity.class, this.guard.getBoundingBox().inflate(5.0D, 3.0D, 5.0D));
        if (!list.isEmpty()) {
            for (LivingEntity mob : list) {
                if (mob != null) {
                    if (mob instanceof Mob && ((Mob) mob).getTarget() instanceof Guard) {
                        return false;
                    }  
                }
            }
        }
        return guard.isUsingItem() && guard.getTarget() == null && guard.getHealth() < guard.getMaxHealth() || guard.getTarget() != null && guard.getHealth() < guard.getMaxHealth() / 2 + 2 && guard.isEating(); 
        // Guards will only keep eating until they're up to full health if they're not aggroed, otherwise they will just heal back above half health and then join back the fight.
    }

    @Override
    public void start() {
        if (guard.getTarget() == null)
            guard.setEating(true);
        guard.startUsingItem(InteractionHand.OFF_HAND);
    }

    @Override
    public void stop() {
        guard.setEating(false);
        guard.stopUsingItem();
    }
}
