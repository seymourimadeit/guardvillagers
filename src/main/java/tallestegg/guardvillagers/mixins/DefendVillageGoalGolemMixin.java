package tallestegg.guardvillagers.mixins;

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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tallestegg.guardvillagers.configuration.GuardConfig;

import java.util.List;

@Mixin(DefendVillageTargetGoal.class)
public abstract class DefendVillageGoalGolemMixin {
    @Final
    @Shadow
    private IronGolem golem;
    @Final
    @Shadow
    private TargetingConditions attackTargeting;

    @Shadow
    private LivingEntity potentialTarget;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getNearbyPlayers(Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"), method = "canUse", cancellable = true)
    public void modifyRep(CallbackInfoReturnable<Boolean> cir) {
        cir.cancel();
        AABB aabb = this.golem.getBoundingBox().inflate(10.0, 8.0, 10.0);
        List<? extends LivingEntity> list = this.golem.level().getNearbyEntities(Villager.class, this.attackTargeting, this.golem, aabb);
        List<Player> list1 = this.golem.level().getNearbyPlayers(this.attackTargeting, this.golem, aabb);

        for (LivingEntity livingentity : list) {
            Villager villager = (Villager) livingentity;

            for (Player player : list1) {
                int i = villager.getPlayerReputation(player);
                if (i <= GuardConfig.COMMON.reputationRequirementToBeAttacked.get()) {
                    this.potentialTarget = player;
                }
            }
        }
        if (this.potentialTarget == null) {
            cir.setReturnValue(false);
        } else {
            cir.setReturnValue(!(this.potentialTarget instanceof Player) || !this.potentialTarget.isSpectator() && !((Player) this.potentialTarget).isCreative());
        }
    }
}
