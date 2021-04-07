package tallestegg.guardvillagers.entities.ai.goals;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Effects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import tallestegg.guardvillagers.GuardEntityType;
import tallestegg.guardvillagers.entities.GuardEntity;

@SuppressWarnings("unused")
//TODO make this a task instead of a goal.
public class HealGuardAndPlayerGoal extends Goal {
    private final MobEntity healer;
    private LivingEntity mob;
    private int rangedAttackTime = -1;
    private final double entityMoveSpeed;
    private int seeTime;
    private final int attackIntervalMin;
    private final int maxRangedAttackTime;
    private final float attackRadius;
    private final float maxAttackDistance;
    protected final EntityPredicate predicate = (new EntityPredicate()).setDistance(64.0D);

    public HealGuardAndPlayerGoal(MobEntity healer, double movespeed, int attackIntervalMin, int maxAttackTime, float maxAttackDistanceIn) {
        this.healer = healer;
        this.entityMoveSpeed = movespeed;
        this.attackIntervalMin = attackIntervalMin;
        this.maxRangedAttackTime = maxAttackTime;
        this.attackRadius = maxAttackDistanceIn;
        this.maxAttackDistance = maxAttackDistanceIn * maxAttackDistanceIn;
        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean shouldExecute() {
        if (((VillagerEntity) this.healer).getVillagerData().getProfession() != VillagerProfession.CLERIC || this.healer.isSleeping()) {
            return false;
        }
        List<LivingEntity> list = this.healer.world.getEntitiesWithinAABB(LivingEntity.class, this.healer.getBoundingBox().grow(10.0D, 3.0D, 10.0D));
        if (!list.isEmpty()) {
            for (LivingEntity mob : list) {
                if (mob != null) {
                    if (mob.getType() == GuardEntityType.GUARD.get() && mob != null && mob.isAlive() && mob.getHealth() < mob.getMaxHealth() || mob instanceof PlayerEntity && mob.isPotionActive(Effects.HERO_OF_THE_VILLAGE) && !((PlayerEntity)mob).abilities.isCreativeMode && mob.getHealth() < mob.getMaxHealth()) {
                        this.mob = mob;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.shouldExecute() && mob != null && mob.getHealth() < mob.getMaxHealth();
    }

    @Override
    public void resetTask() {
        this.mob = null;
        this.seeTime = 0;
        this.rangedAttackTime = -1;
    }

    @Override
    public void tick() {
        if (mob == null)
            return;
        double d0 = this.healer.getDistanceSq(this.mob.getPosX(), this.mob.getPosY(), this.mob.getPosZ());
        boolean flag = this.healer.getEntitySenses().canSee(this.mob);
        if (flag) {
            ++this.seeTime;
        } else {
            this.seeTime = 0;
        }
        this.healer.faceEntity(mob, 30.0F, 30.0F);
        this.healer.getLookController().setLookPositionWithEntity(this.healer, 30.0F, 30.0F);
        if (!(d0 > (double) this.maxAttackDistance) && this.seeTime >= 5) {
            this.healer.getNavigator().clearPath();
        } else {
            this.healer.getNavigator().tryMoveToEntityLiving(this.healer, this.entityMoveSpeed);
        }
        if (mob.getDistance(healer) <= 3.0D) {
            healer.getMoveHelper().strafe(-0.5F, 0);
        }
        if (--this.rangedAttackTime == 0 && mob.getHealth() < mob.getMaxHealth() && mob.isAlive()) {
            if (!flag) {
                return;
            }
            float f =  this.attackRadius;
            float distanceFactor = MathHelper.clamp(f, 0.5F, 0.5F);
            this.throwPotion(mob, distanceFactor);
            this.rangedAttackTime = MathHelper.floor(f * (float) (this.maxRangedAttackTime - this.attackIntervalMin) + (float) this.attackIntervalMin);
        } else if (this.rangedAttackTime < 0) {
            float f2 = MathHelper.sqrt(d0) / this.attackRadius;
            this.rangedAttackTime = MathHelper.floor(f2 * (float) (this.maxRangedAttackTime - this.attackIntervalMin) + (float) this.attackIntervalMin);
        }
    }

    public void throwPotion(LivingEntity target, float distanceFactor) {
        Vector3d vec3d = target.getMotion();
        double d0 = target.getPosX() + vec3d.x - healer.getPosX();
        double d1 = target.getPosYEye() - (double) 1.1F - healer.getPosY();
        double d2 = target.getPosZ() + vec3d.z - healer.getPosZ();
        float f = MathHelper.sqrt(d0 * d0 + d2 * d2);
        Potion potion = Potions.REGENERATION;
        if (target.getHealth() <= 4.0F) {
            potion = Potions.HEALING;
        } else {
            potion = Potions.REGENERATION;
        }

        this.healer.faceEntity(mob, 30.0F, 30.0F);
        this.healer.getLookController().setLookPositionWithEntity(this.healer, 30.0F, 30.0F);
        PotionEntity potionentity = new PotionEntity(healer.world, healer);
        potionentity.setItem(PotionUtils.addPotionToItemStack(new ItemStack(Items.SPLASH_POTION), potion));
        potionentity.rotationPitch -= -20.0F;
        potionentity.shoot(d0, d1 + (double) (f * 0.2F), d2, 0.75F, 8.0F);
        healer.world.playSound((PlayerEntity) null, healer.getPosX(), healer.getPosY(), healer.getPosZ(), SoundEvents.ENTITY_SPLASH_POTION_THROW, healer.getSoundCategory(), 1.0F, 0.8F + healer.getRNG().nextFloat() * 0.4F);
        healer.world.addEntity(potionentity);
    }
}
