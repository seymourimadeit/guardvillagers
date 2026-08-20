package tallestegg.guardvillagers.common.entities;

import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import tallestegg.guardvillagers.GuardVillagers;

/** Lightweight Guard AI for the 26.2 experimental branch. */
@EventBusSubscriber(modid = GuardVillagers.MODID)
public final class SimpleGuardAI {
    private SimpleGuardAI() {
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Guard guard)) {
            return;
        }

        guard.goalSelector.removeAllGoals(goal -> true);
        guard.targetSelector.removeAllGoals(goal -> true);

        guard.goalSelector.addGoal(0, new FloatGoal(guard));
        guard.goalSelector.addGoal(1, new MeleeAttackGoal(guard, 1.0D, true));
        guard.goalSelector.addGoal(2, new RangedCrossbowAttackGoal<>(guard, 1.0D, 8.0F));
        guard.goalSelector.addGoal(2, new RangedBowAttackGoal<>(guard, 1.0D, 20, 15.0F));
        guard.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(guard, 0.5D));
        guard.goalSelector.addGoal(8, new LookAtPlayerGoal(guard, Player.class, 8.0F));
        guard.goalSelector.addGoal(9, new RandomLookAroundGoal(guard));

        guard.targetSelector.addGoal(1, new HurtByTargetGoal(guard));
        guard.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(guard, Monster.class, true));
    }
}
