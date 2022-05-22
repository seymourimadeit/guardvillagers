package tallestegg.guardvillagers.entities.ai.goals;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import tallestegg.guardvillagers.GuardEntityType;
import tallestegg.guardvillagers.GuardItems;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.entities.Guard;

public class RangedCrossbowAttackPassiveGoal<T extends PathfinderMob & RangedAttackMob & CrossbowAttackMob> extends Goal {
    private final T entity;
    private RangedCrossbowAttackPassiveGoal.CrossbowState crossbowState = RangedCrossbowAttackPassiveGoal.CrossbowState.UNCHARGED;
    private final double speed;
    private final float distanceMoveToEntity;
    private int seeTicks;
    private int timeUntilStrike;

    public RangedCrossbowAttackPassiveGoal(T entity, double p_i50322_2_, float p_i50322_4_) {
        this.entity = entity;
        this.speed = p_i50322_2_;
        this.distanceMoveToEntity = p_i50322_4_ * p_i50322_4_;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.hasAttackTarget() && this.isHoldingCrossbow() && !((Guard) this.entity).isEating();
    }

    private boolean isHoldingCrossbow() {
        return this.entity.getMainHandItem().getItem() instanceof CrossbowItem;
    }

    @Override
    public boolean canContinueToUse() {
        return this.hasAttackTarget() && (this.canUse() || !this.entity.getNavigation().isDone()) && this.isHoldingCrossbow();
    }

    private boolean hasAttackTarget() {
        return this.entity.getTarget() != null && this.entity.getTarget().isAlive();
    }

    @Override
    public void stop() {
        super.stop();
        this.entity.setAggressive(false);
        this.entity.setTarget((LivingEntity) null);
        ((Guard) this.entity).setKicking(false);
        this.seeTicks = 0;
        if (this.entity.getPose() == Pose.CROUCHING)
            this.entity.setPose(Pose.STANDING);
        if (this.entity.isUsingItem()) {
            this.entity.stopUsingItem();
            ((CrossbowAttackMob) this.entity).setChargingCrossbow(false);
        }
    }

    public boolean checkFriendlyFire() {
        List<LivingEntity> list = this.entity.level.getEntitiesOfClass(LivingEntity.class, this.entity.getBoundingBox().inflate(5.0D, 1.0D, 5.0D));
        for (LivingEntity guard : list) {
            if (entity != guard || guard != entity) {
                if (guard != entity.getTarget()) {
                    boolean isVillager = guard.getType() == EntityType.VILLAGER || guard.getType() == GuardEntityType.GUARD.get() || guard.getType() == EntityType.IRON_GOLEM;
                    if (isVillager) {
                        Vec3 vector3d = entity.getLookAngle();
                        Vec3 vector3d1 = guard.position().vectorTo(entity.position()).normalize();
                        vector3d1 = new Vec3(vector3d1.x, vector3d1.y, vector3d1.z);
                        if (vector3d1.dot(vector3d) < 1.0D && entity.hasLineOfSight(guard))
                            return GuardConfig.FriendlyFire;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void tick() {
        LivingEntity livingentity = this.entity.getTarget();
        if (livingentity != null) {
            this.entity.setAggressive(true);
            boolean flag = this.entity.getSensing().hasLineOfSight(livingentity);
            boolean flag1 = this.seeTicks > 0;
            if (flag != flag1) {
                this.seeTicks = 0;
            }

            if (flag) {
                ++this.seeTicks;
            } else {
                --this.seeTicks;
            }

            if (this.entity.getPose() == Pose.STANDING && this.entity.level.random.nextInt(4) == 0 && entity.tickCount % 50 == 0) {
                this.entity.setPose(Pose.CROUCHING);
            }

            if (this.entity.getPose() == Pose.CROUCHING && this.entity.level.random.nextInt(4) == 0 && entity.tickCount % 100 == 0) {
                this.entity.setPose(Pose.STANDING);
            }

            double d1 = livingentity.distanceTo(entity);
            if (d1 <= 2.0D) {
                this.entity.getMoveControl().strafe(this.entity.isUsingItem() ?- 0.5F : -3.0F, 0.0F);
                this.entity.lookAt(livingentity, 30.0F, 30.0F);
            }

            double d0 = this.entity.distanceToSqr(livingentity);
            boolean flag2 = (d0 > (double) this.distanceMoveToEntity || this.seeTicks < 5) && this.timeUntilStrike == 0;
            if (flag2) {
                this.entity.getNavigation().moveTo(livingentity, this.isCrossbowUncharged() ? this.speed : this.speed * 0.5D);
            } else {
                this.entity.getNavigation().stop();
            }
            this.entity.lookAt(livingentity, 30.0F, 30.0F);
            this.entity.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
            if (this.crossbowState == RangedCrossbowAttackPassiveGoal.CrossbowState.UNCHARGED && !CrossbowItem.isCharged(entity.getUseItem()) && !entity.isBlocking()) {
                if (flag) {
                    this.entity.startUsingItem(GuardItems.getHandWith(entity, item -> item instanceof CrossbowItem));
                    this.crossbowState = RangedCrossbowAttackPassiveGoal.CrossbowState.CHARGING;
                    ((CrossbowAttackMob) this.entity).setChargingCrossbow(true);
                }
            } else if (this.crossbowState == RangedCrossbowAttackPassiveGoal.CrossbowState.CHARGING) {
                if (!this.entity.isUsingItem())
                    this.crossbowState = RangedCrossbowAttackPassiveGoal.CrossbowState.UNCHARGED;
                int i = this.entity.getTicksUsingItem();
                ItemStack itemstack = this.entity.getUseItem();
                if (i >= CrossbowItem.getChargeDuration(itemstack) || CrossbowItem.isCharged(entity.getUseItem())) {
                    this.entity.releaseUsingItem();
                    this.crossbowState = RangedCrossbowAttackPassiveGoal.CrossbowState.CHARGED;
                    this.timeUntilStrike = 20 + this.entity.getRandom().nextInt(20);
                    ((CrossbowAttackMob) this.entity).setChargingCrossbow(false);
                }
            } else if (this.crossbowState == RangedCrossbowAttackPassiveGoal.CrossbowState.CHARGED) {
                --this.timeUntilStrike;
                if (this.timeUntilStrike == 0) {
                    this.crossbowState = RangedCrossbowAttackPassiveGoal.CrossbowState.READY_TO_ATTACK;
                }
            } else if (this.crossbowState == RangedCrossbowAttackPassiveGoal.CrossbowState.READY_TO_ATTACK && flag && !checkFriendlyFire() && !entity.isBlocking()) {
                ((RangedAttackMob) this.entity).performRangedAttack(livingentity, 1.0F);
                ItemStack itemstack1 = this.entity.getItemInHand(GuardItems.getHandWith(entity, item -> item instanceof CrossbowItem));
                CrossbowItem.setCharged(itemstack1, false);
                this.crossbowState = RangedCrossbowAttackPassiveGoal.CrossbowState.UNCHARGED;
            }
        }
    }

    private boolean isCrossbowUncharged() {
        return this.crossbowState == RangedCrossbowAttackPassiveGoal.CrossbowState.UNCHARGED;
    }

    static enum CrossbowState {
        UNCHARGED, CHARGING, CHARGED, READY_TO_ATTACK;
    }
}