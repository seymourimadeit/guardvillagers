package tallestegg.guardvillagers.common.entities;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 * Lightweight Guard AI.
 *
 * The Guard keeps its equipment/inventory implementation, but its runtime AI
 * is intentionally close to the simple Iron Golem model: acquire a nearby
 * hostile target, path to it, and attack it. No patrol, follow, formation,
 * villager-protection scans, food seeking, or custom target propagation is
 * installed here.
 */
public final class SimpleGuardAI {
    private SimpleGuardAI() {
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Guard guard)) {
            return;
        }

        // Guard.registerGoals() installs the original, feature-heavy AI.
        // Replace both selectors once, before the entity receives its first tick.
        guard.goalSelector.removeAllGoals(goal -> true);
        guard.targetSelector.removeAllGoals(goal -> true);

        // Basic movement/look behaviour only.
        guard.goalSelector.addGoal(0, new FloatGoal(guard));
        guard.goalSelector.addGoal(1, new MeleeAttackGoal(guard, 1.0D, true));
        guard.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(guard, 0.5D));
        guard.goalSelector.addGoal(8, new LookAtPlayerGoal(guard, Player.class, 8.0F));
        guard.goalSelector.addGoal(9, new RandomLookAroundGoal(guard));

        // Simple target acquisition, deliberately matching the vanilla
        // hostile-mob style rather than searching for villagers that need help.
        guard.targetSelector.addGoal(1, new HurtByTargetGoal(guard));
        guard.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                guard,
                Mob.class,
                10,
                true,
                false,
                (mob, level) -> mob instanceof Enemy && !(mob instanceof Guard)
        ));
    }
}
