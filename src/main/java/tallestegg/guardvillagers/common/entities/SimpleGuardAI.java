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
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 * Lightweight Guard AI for the 26.2 experimental branch.
 *
 * The Guard keeps its equipment/inventory implementation, but its runtime
 * decision loop is intentionally small: acquire a nearby hostile mob,
 * navigate to it, and attack it. There is deliberately no patrol, formation,
 * follow, food-seeking, or "which villager needs help" scan here.
 *
 * This class is installed by GuardVillagers on the NeoForge event bus.
 */
public final class SimpleGuardAI {
    private SimpleGuardAI() {
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Guard guard)) {
            return;
        }

        // Guard.registerGoals() still contains the original feature set in
        // this experimental branch. Replace those selectors once when the
        // entity enters a level, before its first normal server AI cycle.
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
