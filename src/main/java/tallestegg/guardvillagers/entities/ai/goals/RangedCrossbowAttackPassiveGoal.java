package tallestegg.guardvillagers.entities.ai.goals;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ICrossbowUser;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;
import tallestegg.guardvillagers.GuardEntityType;
import tallestegg.guardvillagers.GuardItems;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.entities.GuardEntity;

public class RangedCrossbowAttackPassiveGoal<T extends CreatureEntity & IRangedAttackMob & ICrossbowUser> extends Goal {
    private final T entity;
    private RangedCrossbowAttackPassiveGoal.CrossbowState field_220749_b = RangedCrossbowAttackPassiveGoal.CrossbowState.UNCHARGED;
    private final double field_220750_c;
    private final float field_220751_d;
    private int field_220752_e;
    private int field_220753_f;

    public RangedCrossbowAttackPassiveGoal(T p_i50322_1_, double p_i50322_2_, float p_i50322_4_) {
        this.entity = p_i50322_1_;
        this.field_220750_c = p_i50322_2_;
        this.field_220751_d = p_i50322_4_ * p_i50322_4_;
        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public boolean shouldExecute() {
        return this.func_220746_h() && this.isHoldingCrossbow() && !((GuardEntity) this.entity).isEating();
    }

    private boolean isHoldingCrossbow() {
        return this.entity.getHeldItemMainhand().getItem() instanceof CrossbowItem;
    }

    public boolean shouldContinueExecuting() {
        return this.func_220746_h() && (this.shouldExecute() || !this.entity.getNavigator().noPath()) && this.isHoldingCrossbow();
    }

    private boolean func_220746_h() {
        return this.entity.getAttackTarget() != null && this.entity.getAttackTarget().isAlive();
    }

    public void resetTask() {
        super.resetTask();
        this.entity.setAggroed(false);
        this.entity.setAttackTarget((LivingEntity) null);
        ((GuardEntity) this.entity).setKicking(false);
        this.field_220752_e = 0;
        if (this.entity.getPose() == Pose.CROUCHING) {
            this.entity.setPose(Pose.STANDING);
        }
        if (this.entity.isHandActive()) {
            this.entity.resetActiveHand();
            ((ICrossbowUser) this.entity).setCharging(false);
        }
    }

    public boolean checkFriendlyFire() {
        List<LivingEntity> list = this.entity.world.getEntitiesWithinAABB(LivingEntity.class, this.entity.getBoundingBox().grow(4.0D, 1.0D, 4.0D));
        for (LivingEntity guard : list) {
            if (entity != guard || guard != entity) {
                if (guard != entity.getAttackTarget()) {
                    boolean isVillager = guard.getType() == EntityType.VILLAGER || guard.getType() == GuardEntityType.GUARD.get() || guard.getType() == EntityType.IRON_GOLEM;
                    if (isVillager) {
                        Vector3d vector3d = entity.getLookVec();
                        Vector3d vector3d1 = guard.getPositionVec().subtractReverse(entity.getPositionVec()).normalize();
                        vector3d1 = new Vector3d(vector3d1.x, vector3d1.y, vector3d1.z);
                        if (vector3d1.dotProduct(vector3d) < 0.0D && entity.canEntityBeSeen(guard))
                            return GuardConfig.FriendlyFire;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void tick() {
        LivingEntity livingentity = this.entity.getAttackTarget();
        if (livingentity != null) {
            this.entity.setAggroed(true);
            boolean flag = this.entity.getEntitySenses().canSee(livingentity);
            boolean flag1 = this.field_220752_e > 0;
            if (flag != flag1) {
                this.field_220752_e = 0;
            }

            if (flag) {
                ++this.field_220752_e;
            } else {
                --this.field_220752_e;
            }

            if (this.entity.getPose() == Pose.STANDING && this.entity.world.rand.nextInt(4) == 0 && entity.ticksExisted % 50 == 0) {
                this.entity.setPose(Pose.CROUCHING);
            }

            if (this.entity.getPose() == Pose.CROUCHING && this.entity.world.rand.nextInt(4) == 0 && entity.ticksExisted % 100 == 0) {
                this.entity.setPose(Pose.STANDING);
            }

            double d1 = livingentity.getDistance(entity);
            if (d1 <= 5.0D) {
                this.entity.getMoveHelper().strafe(-3.0F, 0.0F);
                this.entity.faceEntity(livingentity, 30.0F, 30.0F);
            }

            double d0 = this.entity.getDistanceSq(livingentity);
            boolean flag2 = (d0 > (double) this.field_220751_d || this.field_220752_e < 5) && this.field_220753_f == 0;
            if (flag2) {
                this.entity.getNavigator().tryMoveToEntityLiving(livingentity, this.func_220747_j() ? this.field_220750_c : this.field_220750_c * 0.5D);
            } else {
                this.entity.getNavigator().clearPath();
            }
            this.entity.faceEntity(livingentity, 30.0F, 30.0F);
            this.entity.getLookController().setLookPositionWithEntity(livingentity, 30.0F, 30.0F);
            if (this.field_220749_b == RangedCrossbowAttackPassiveGoal.CrossbowState.UNCHARGED && !CrossbowItem.isCharged(entity.getActiveItemStack()) && !entity.isActiveItemStackBlocking()) {
                if (flag) {
                    this.entity.setActiveHand(GuardItems.getHandWith(entity, item -> item instanceof CrossbowItem));
                    this.field_220749_b = RangedCrossbowAttackPassiveGoal.CrossbowState.CHARGING;
                    ((ICrossbowUser) this.entity).setCharging(true);
                }
            } else if (this.field_220749_b == RangedCrossbowAttackPassiveGoal.CrossbowState.CHARGING) {
                if (!this.entity.isHandActive()) {
                    this.field_220749_b = RangedCrossbowAttackPassiveGoal.CrossbowState.UNCHARGED;
                }

                int i = this.entity.getItemInUseMaxCount();
                ItemStack itemstack = this.entity.getActiveItemStack();
                if (i >= CrossbowItem.getChargeTime(itemstack) || CrossbowItem.isCharged(entity.getActiveItemStack())) {
                    this.entity.stopActiveHand();
                    this.field_220749_b = RangedCrossbowAttackPassiveGoal.CrossbowState.CHARGED;
                    this.field_220753_f = 20 + this.entity.getRNG().nextInt(20);
                    ((ICrossbowUser) this.entity).setCharging(false);
                }
            } else if (this.field_220749_b == RangedCrossbowAttackPassiveGoal.CrossbowState.CHARGED) {
                --this.field_220753_f;
                if (this.field_220753_f == 0) {
                    this.field_220749_b = RangedCrossbowAttackPassiveGoal.CrossbowState.READY_TO_ATTACK;
                }
            } else if (this.field_220749_b == RangedCrossbowAttackPassiveGoal.CrossbowState.READY_TO_ATTACK && flag && !checkFriendlyFire() && !entity.isActiveItemStackBlocking()) {
                ((IRangedAttackMob) this.entity).attackEntityWithRangedAttack(livingentity, 1.0F);
                ItemStack itemstack1 = this.entity.getHeldItem(GuardItems.getHandWith(entity, item -> item instanceof CrossbowItem));
                CrossbowItem.setCharged(itemstack1, false);
                this.field_220749_b = RangedCrossbowAttackPassiveGoal.CrossbowState.UNCHARGED;
            }
        }
    }

    private boolean func_220747_j() {
        return this.field_220749_b == RangedCrossbowAttackPassiveGoal.CrossbowState.UNCHARGED;
    }

    static enum CrossbowState {
        UNCHARGED, CHARGING, CHARGED, READY_TO_ATTACK;
    }
}