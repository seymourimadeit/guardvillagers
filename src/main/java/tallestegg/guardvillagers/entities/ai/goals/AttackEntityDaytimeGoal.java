package tallestegg.guardvillagers.entities.ai.goals;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.monster.SpiderEntity;

//The spiders goal was private, so this needed to be done.
public class AttackEntityDaytimeGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    public AttackEntityDaytimeGoal(SpiderEntity spider, Class<T> classTarget) {
        super(spider, classTarget, true);
    }

    @Override
    public boolean shouldExecute() {
        float f = this.goalOwner.getBrightness();
        return f >= 0.5F ? false : super.shouldExecute();
    }
}
