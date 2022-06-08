package tallestegg.guardvillagers.entities.ai.goals;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Spider;

//The spiders goal was private, so this needed to be done.
public class AttackEntityDaytimeGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    public AttackEntityDaytimeGoal(Spider spider, Class<T> classTarget) {
        super(spider, classTarget, true);
    }

    @Override
    public boolean canUse() {
        float f = this.mob.getLightLevelDependentMagicValue();
        return f >= 0.5F ? false : super.canUse();
    }
}
