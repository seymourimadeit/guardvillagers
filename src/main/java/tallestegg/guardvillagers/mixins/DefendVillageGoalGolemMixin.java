package tallestegg.guardvillagers.mixins;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.DefendVillageTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tallestegg.guardvillagers.configuration.GuardConfig;

import java.util.List;

@Mixin(DefendVillageTargetGoal.class)
public abstract class DefendVillageGoalGolemMixin {

    @Shadow @Final
    private IronGolem golem;

    @Shadow @Final
    private TargetingConditions attackTargeting;

    @Shadow
    private LivingEntity potentialTarget;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void guardvillagers$modifyRep(CallbackInfoReturnable<Boolean> cir) {
        if (!(this.golem.level() instanceof ServerLevel serverLevel)) {
            cir.setReturnValue(false);
            return;
        }

        this.potentialTarget = null;

        AABB aabb = this.golem.getBoundingBox().inflate(10.0D, 8.0D, 10.0D);

        // 1.21.3: getNearbyEntities/getNearbyPlayers 대신 getEntitiesOfClass + TargetingConditions.test 사용
        List<Villager> villagers = serverLevel.getEntitiesOfClass(
                Villager.class,
                aabb,
                villager -> this.attackTargeting.test(serverLevel, this.golem, villager)
        );

        List<Player> players = serverLevel.getEntitiesOfClass(
                Player.class,
                aabb,
                player -> this.attackTargeting.test(serverLevel, this.golem, player)
        );

        for (Villager villager : villagers) {
            for (Player player : players) {
                int rep = villager.getPlayerReputation(player);
                if (rep <= GuardConfig.COMMON.reputationRequirementToBeAttacked.get()) {
                    this.potentialTarget = player;
                    break;
                }
            }
            if (this.potentialTarget != null) {
                break;
            }
        }

        if (this.potentialTarget == null) {
            cir.setReturnValue(false);
            return;
        }

        if (this.potentialTarget instanceof Player player) {
            cir.setReturnValue(!player.isSpectator() && !player.isCreative());
        } else {
            cir.setReturnValue(true);
        }
    }
}