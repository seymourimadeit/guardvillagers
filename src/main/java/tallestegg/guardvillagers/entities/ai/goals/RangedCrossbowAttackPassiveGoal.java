package tallestegg.guardvillagers.entities.ai.goals;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import tallestegg.guardvillagers.GuardEntityType;
import tallestegg.guardvillagers.GuardItems;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.entities.Guard;

import javax.annotation.Nullable;

public class RangedCrossbowAttackPassiveGoal<T extends PathfinderMob & RangedAttackMob & CrossbowAttackMob> extends Goal {
    public static final UniformInt PATHFINDING_DELAY_RANGE = TimeUtil.rangeOfSeconds(1, 2);
    private final T mob;
    private final double speedModifier;
    private final float attackRadiusSqr;
    protected double wantedX;
    protected double wantedY;
    protected double wantedZ;
    private CrossbowState crossbowState = CrossbowState.UNCHARGED;
    private int seeTime;
    private int attackDelay;
    private int updatePathDelay;
    private int runTime;

    public RangedCrossbowAttackPassiveGoal(T pMob, double pSpeedModifier, float pAttackRadius) {
        this.mob = pMob;
        this.speedModifier = pSpeedModifier;
        this.attackRadiusSqr = pAttackRadius * pAttackRadius;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.isValidTarget() && this.isHoldingCrossbow();
    }

    private boolean isHoldingCrossbow() {
        return this.mob.getMainHandItem().getItem() instanceof CrossbowItem;
    }

    @Override
    public boolean canContinueToUse() {
        return this.isValidTarget() && (this.canUse() || !this.mob.getNavigation().isDone()) && this.isHoldingCrossbow();
    }

    private boolean isValidTarget() {
        return this.mob.getTarget() != null && this.mob.getTarget().isAlive();
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.setAggressive(false);
        this.mob.setTarget(null);
        this.seeTime = 0;
        if (this.mob.isUsingItem()) {
            this.mob.stopUsingItem();
            this.mob.setChargingCrossbow(false);
        }
        this.mob.setPose(Pose.STANDING);
    }


    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.mob.setAggressive(true);
    }

    @Override
    public void tick() {
        LivingEntity livingentity = this.mob.getTarget();
        if (livingentity != null) {
            boolean canSee = this.mob.getSensing().hasLineOfSight(livingentity);
            boolean hasSeenEntityRecently = this.seeTime > 0;
            if (canSee != hasSeenEntityRecently) {
                this.seeTime = 0;
            }
            if (canSee) {
                ++this.seeTime;
            } else {
                --this.seeTime;
            }
            double d0 = this.mob.distanceToSqr(livingentity);
            double d1 = livingentity.distanceTo(this.mob);
            if (d1 <= 4.0D) {
                this.mob.getMoveControl().strafe(this.mob.isUsingItem() ? -0.5F : -3.0F, 0.0F);
                this.mob.lookAt(livingentity, 30.0F, 30.0F);
            }
            if (this.mob.getRandom().nextInt(50) == 0) {
                if (this.mob.hasPose(Pose.STANDING))
                    this.mob.setPose(Pose.CROUCHING);
                else
                    this.mob.setPose(Pose.STANDING);
            }
            boolean canSee2 = (d0 > (double) this.attackRadiusSqr || this.seeTime < 5) && this.attackDelay == 0;
            if (canSee2) {
                --this.updatePathDelay;
                if (this.updatePathDelay <= 0 && !((Guard)this.mob).isPatrolling()) {
                    this.mob.getNavigation().moveTo(livingentity, this.canRun() ? this.speedModifier : this.speedModifier * 0.5D);
                    this.updatePathDelay = PATHFINDING_DELAY_RANGE.sample(this.mob.getRandom());
                }
            } else {
                this.updatePathDelay = 0;
                this.mob.getNavigation().stop();
            }
            this.mob.lookAt(livingentity, 30.0F, 30.0F);
            this.mob.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
            if (this.friendlyInLineOfSight() && GuardConfig.FriendlyFire)
                this.crossbowState = CrossbowState.FIND_NEW_POSITION;
            if (this.crossbowState == CrossbowState.FIND_NEW_POSITION && GuardConfig.FriendlyFire) {
                this.mob.stopUsingItem();
                this.mob.setChargingCrossbow(false);
                if (this.findPosition())
                    this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.mob.isCrouching() ? 0.5F : 1.2D);
                this.crossbowState = CrossbowState.UNCHARGED;
            } else if (this.crossbowState == CrossbowState.UNCHARGED) {
                if (hasSeenEntityRecently) {
                    this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof CrossbowItem));
                    this.crossbowState = CrossbowState.CHARGING;
                    this.mob.setChargingCrossbow(true);
                }
            } else if (this.crossbowState == CrossbowState.CHARGING) {
                if (!this.mob.isUsingItem()) {
                    this.crossbowState = CrossbowState.UNCHARGED;
                }
                int i = this.mob.getTicksUsingItem();
                ItemStack itemstack = this.mob.getUseItem();
                if (i >= CrossbowItem.getChargeDuration(itemstack) || CrossbowItem.isCharged(itemstack)) {
                    this.mob.releaseUsingItem();
                    this.crossbowState = CrossbowState.CHARGED;
                    this.attackDelay = 10 + this.mob.getRandom().nextInt(5);
                    this.mob.setChargingCrossbow(false);
                }
            } else if (this.crossbowState == CrossbowState.CHARGED) {
                --this.attackDelay;
                if (this.attackDelay == 0) {
                    this.crossbowState = CrossbowState.READY_TO_ATTACK;
                }
            } else if (this.crossbowState == CrossbowState.READY_TO_ATTACK && canSee) {
                this.mob.performRangedAttack(livingentity, 1.0F);
                ItemStack itemstack1 = this.mob.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof CrossbowItem));
                CrossbowItem.setCharged(itemstack1, false);
                this.crossbowState = CrossbowState.UNCHARGED;
            }
        }
    }

    private boolean friendlyInLineOfSight() {
        List<Entity> list = this.mob.level.getEntities(this.mob, this.mob.getBoundingBox().inflate(5.0D));
        for (Entity guard : list) {
            if (guard != this.mob.getTarget()) {
                boolean isVillager = ((Guard)this.mob).getOwner() == guard || guard.getType() == EntityType.VILLAGER || guard.getType() == GuardEntityType.GUARD.get() || guard.getType() == EntityType.IRON_GOLEM;
                if (isVillager) {
                    Vec3 vector3d = this.mob.getLookAngle();
                    Vec3 vector3d1 = guard.position().vectorTo(this.mob.position()).normalize();
                    vector3d1 = new Vec3(vector3d1.x, vector3d1.y, vector3d1.z);
                    if (vector3d1.dot(vector3d) < 1.0D && this.mob.hasLineOfSight(guard) && guard.distanceTo(this.mob) < 4.0D)
                        return true;
                }
            }
        }
        return false;
    }

    public boolean findPosition() {
        Vec3 vector3d = this.getPosition();
        if (vector3d == null) {
            return false;
        } else {
            this.wantedX = vector3d.x;
            this.wantedY = vector3d.y;
            this.wantedZ = vector3d.z;
            return true;
        }
    }

    @Nullable
    protected Vec3 getPosition() {
        if (this.isValidTarget())
            return DefaultRandomPos.getPosAway(this.mob, 16, 7, this.mob.getTarget().position());
        else
            return DefaultRandomPos.getPos(this.mob, 16, 7);
    }

    private boolean canRun() {
        return this.crossbowState == CrossbowState.UNCHARGED;
    }

    public enum CrossbowState {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK,
        FIND_NEW_POSITION
    }
}