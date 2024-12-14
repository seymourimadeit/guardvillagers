package tallestegg.guardvillagers;

import ewewukek.musketmod.GunItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import tallestegg.guardvillagers.entities.Guard;
import tallestegg.guardvillagers.entities.ai.goals.RangedCrossbowAttackPassiveGoal;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class ModCompat {
    public static HumanoidModel.ArmPose reloadMusketAnim(ItemStack stack, InteractionHand handIn, Guard guard, HumanoidModel.ArmPose bipedmodel$armpose) {
        if (stack.getItem() instanceof GunItem && !GunItem.isLoaded(stack)) {
            if (handIn == guard.getUsedItemHand()) {
                return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
            }
        }
        return bipedmodel$armpose;
    }

    public static boolean isHoldingMusket(ItemStack stack) {
        return stack.getItem() instanceof GunItem;
    }

    public static HumanoidModel.ArmPose holdMusketAnim(ItemStack stack, Guard guard, HumanoidModel.ArmPose bipedmodel$armpose) {
        if (stack.getItem() instanceof GunItem && GunItem.isLoaded(stack) && guard.isAggressive())
            return HumanoidModel.ArmPose.CROSSBOW_HOLD;
        return HumanoidModel.ArmPose.ITEM;
    }

    public static void shootGun(Guard guard) {
        if (guard.getMainHandItem().getItem() instanceof GunItem musketItem) {
            Vec3 front = Vec3.directionFromRotation(guard.getXRot(), guard.getYRot());
            musketItem.fire(guard, front);
            GunItem.setLoaded(guard.getMainHandItem(), false);
            guard.playSound(musketItem.fireSound(), 3.5F, 1);
            guard.damageGuardItem(1, EquipmentSlot.MAINHAND, guard.getMainHandItem());
        }
    }

    public static class UseMusketGoal<T extends PathfinderMob & RangedAttackMob> extends Goal {
        private final float attackRadiusSqr;
        private final T mob;
        private int attackIntervalMin;
        private Path path;
        private int attackTime = -1;
        private int seeTime;
        private int timeUntilShoot = 20;

        public UseMusketGoal(T pMob, int pAttackIntervalMin, float pAttackRadius) {
            this.mob = pMob;
            this.attackIntervalMin = pAttackIntervalMin;
            this.attackRadiusSqr = pAttackRadius * pAttackRadius;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = mob.getTarget();
            return target != null && mob.getMainHandItem().getItem() instanceof GunItem;
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public void start() {
            this.mob.setAggressive(true);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = mob.getTarget();
            if (target != null) {
                double distanceSquared = mob.distanceToSqr(target);
                boolean canSee = mob.getSensing().hasLineOfSight(target);
                boolean seeTimeGreaterThanZero = this.seeTime > 0;
                this.mob.getLookControl().setLookAt(target);
                this.mob.lookAt(target, 30.0F, 30.0F);
                if (!canSee && this.seeTime < -60)
                    mob.stopUsingItem();
                if (GunItem.isLoaded(this.mob.getMainHandItem())) {
                    this.mob.stopUsingItem();
                    if (canSee) {
                        this.timeUntilShoot--;
                        if (timeUntilShoot <= 0) {
                            this.mob.performRangedAttack(target, ((GunItem) this.mob.getMainHandItem().getItem()).bulletSpeed());
                            this.attackTime = this.attackIntervalMin;
                        }
                    }
                } else if (--this.attackTime <= 0 && this.seeTime >= -60 && !GunItem.isLoaded(this.mob.getMainHandItem())) {
                    this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(mob, item -> item instanceof GunItem));
                    this.timeUntilShoot = 20;
                }
                if (canSee != seeTimeGreaterThanZero)
                    this.seeTime = 0;
                if (canSee) {
                    ++this.seeTime;
                } else {
                    --this.seeTime;
                }
                if (distanceSquared <= 6.0D) {
                    this.mob.getMoveControl().strafe(-0.5F, 0.0F);
                }
                if ((distanceSquared > (double) this.attackRadiusSqr) || this.seeTime < 5) {
                    this.mob.getNavigation().moveTo(target, 1.0D);
                } else if (distanceSquared < (double) this.attackRadiusSqr) {
                    this.mob.getNavigation().stop();
                }
                if (RangedCrossbowAttackPassiveGoal.friendlyInLineOfSight(this.mob)) {
                    Vec3 vec3 = this.getPosition(this.mob);
                    if (distanceSquared <= this.attackRadiusSqr) {
                        if (vec3 != null && mob.getNavigation().isDone()) {
                            this.path = mob.getNavigation().createPath(vec3.x, vec3.y, vec3.z, 0);
                            this.mob.getLookControl().setLookAt(vec3.x, mob.getEyeY(), vec3.z);
                            if (this.path != null && this.path.canReach()) {
                                this.mob.getNavigation().moveTo(this.path, 0.9D);
                                this.attackTime = -1;
                                this.mob.stopUsingItem();
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void stop() {
            mob.setAggressive(false);
            this.seeTime = 0;
            this.attackTime = -1;
            mob.stopUsingItem();
            this.timeUntilShoot = 20;
        }

        @Nullable
        protected Vec3 getPosition(T mob) {
            if (mob.getTarget() != null)
                return LandRandomPos.getPosAway(mob, 5, 7, mob.getTarget().position());
            else
                return LandRandomPos.getPos(mob, 5, 7);
        }
    }
}
