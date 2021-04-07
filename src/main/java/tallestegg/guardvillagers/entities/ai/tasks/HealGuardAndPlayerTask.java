package tallestegg.guardvillagers.entities.ai.tasks;

import java.util.List;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.task.SpawnGolemTask;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import tallestegg.guardvillagers.GuardEntityType;
import tallestegg.guardvillagers.configuration.GuardConfig;

public class HealGuardAndPlayerTask extends SpawnGolemTask {
    private LivingEntity entityToHeal;
    private int rangedAttackTime = -1;
    private int seeTime;
    private final int attackIntervalMin;
    private final int maxRangedAttackTime;
    private final float attackRadius;
    private final float maxAttackDistance;

    public HealGuardAndPlayerTask(int p_i1650_4_, int maxAttackTime, float maxAttackDistanceIn) {
        super();
        this.attackIntervalMin = p_i1650_4_;
        this.maxRangedAttackTime = 0;
        this.attackRadius = maxAttackDistanceIn;
        this.maxAttackDistance = maxAttackDistanceIn * maxAttackDistanceIn;
    }

    @Override
    protected boolean shouldExecute(ServerWorld worldIn, VillagerEntity owner) {
        if (!GuardConfig.ClericHealing) {
            return false;
        } else if (owner.getVillagerData().getProfession() != VillagerProfession.CLERIC || owner.isSleeping()) {
            return false;
        } else {
            List<LivingEntity> list = owner.world.getEntitiesWithinAABB(LivingEntity.class, owner.getBoundingBox().grow(10.0D, 3.0D, 10.0D));
            if (!list.isEmpty()) {
                for (LivingEntity entityToHeal : list) {
                    if (entityToHeal != null) {
                        if (entityToHeal.getType() == GuardEntityType.GUARD.get() && entityToHeal != null && entityToHeal.isAlive() && entityToHeal.getHealth() < entityToHeal.getMaxHealth()
                                || entityToHeal instanceof PlayerEntity && entityToHeal.isPotionActive(Effects.HERO_OF_THE_VILLAGE) && !((PlayerEntity) entityToHeal).abilities.isCreativeMode && entityToHeal.getHealth() < entityToHeal.getMaxHealth()) {
                            this.entityToHeal = entityToHeal;
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    @Override
    protected boolean shouldContinueExecuting(ServerWorld worldIn, VillagerEntity entityIn, long gameTimeIn) {
        return entityToHeal.isAlive() && entityToHeal.getHealth() < entityToHeal.getMaxHealth() && this.shouldExecute(worldIn, entityIn);
    }

    @Override
    protected void updateTask(ServerWorld worldIn, VillagerEntity owner, long gameTime) {
        if (entityToHeal == null)
            return;
        double d0 = owner.getDistanceSq(this.entityToHeal.getPosX(), this.entityToHeal.getPosY(), this.entityToHeal.getPosZ());
        boolean flag = owner.getEntitySenses().canSee(this.entityToHeal);
        if (flag) {
            ++this.seeTime;
        } else {
            this.seeTime = 0;
        }
        BrainUtil.lookAt(owner, entityToHeal);
        owner.faceEntity(entityToHeal, 90.0F, 90.0F);
        if (!(d0 > (double) this.maxAttackDistance) && this.seeTime >= 5) {
            owner.getNavigator().clearPath();
        } else {
            owner.getNavigator().tryMoveToEntityLiving(entityToHeal, owner.getAIMoveSpeed());
        }
        if (entityToHeal.getDistance(owner) <= 5.0D) {
            owner.getMoveHelper().strafe(-0.5F, 0);
        }
        if (--this.rangedAttackTime == 0 && entityToHeal.getHealth() < entityToHeal.getMaxHealth() && entityToHeal.isAlive()) {
            if (!flag) {
                return;
            }
            float f =  this.attackRadius;
            float lvt_5_1_ = MathHelper.clamp(f, 0.5F, 0.5F);
            this.throwPotion(owner, entityToHeal, lvt_5_1_);
            this.rangedAttackTime = MathHelper.floor(f * (float) (this.maxRangedAttackTime - this.attackIntervalMin) + (float) this.attackIntervalMin);
        } else if (this.rangedAttackTime < 0 && owner.getDistance(entityToHeal) >= 5.0D) {
            float f2 = MathHelper.sqrt(d0) / this.attackRadius;
            this.rangedAttackTime = MathHelper.floor(f2 * (float) (this.maxRangedAttackTime - this.attackIntervalMin) + (float) this.attackIntervalMin);
        }
    }

    public void throwPotion(VillagerEntity villager, LivingEntity target, float distanceFactor) {
        Vector3d vec3d = target.getMotion();
        double d0 = target.getPosX() + vec3d.x - villager.getPosX();
        double d1 = target.getPosYEye() - (double) 1.1F - villager.getPosY();
        double d2 = target.getPosZ() + vec3d.z - villager.getPosZ();
        float f = MathHelper.sqrt(d0 * d0 + d2 * d2);
        Potion potion = Potions.REGENERATION;
        if (target.getHealth() <= 4.0F) {
            potion = Potions.HEALING;
        } else {
            potion = Potions.REGENERATION;
        }
        BrainUtil.lookAt(villager, target);
        PotionEntity potionentity = new PotionEntity(villager.world, villager);
        potionentity.setItem(PotionUtils.addPotionToItemStack(new ItemStack(Items.SPLASH_POTION), potion));
        potionentity.rotationPitch -= -20.0F;
        potionentity.shoot(d0, d1 + (double) (f * 0.2F), d2, 0.75F, 8.0F);
        villager.world.playSound((PlayerEntity) null, villager.getPosX(), villager.getPosY(), villager.getPosZ(), SoundEvents.ENTITY_SPLASH_POTION_THROW, villager.getSoundCategory(), 1.0F, 0.8F + villager.getRNG().nextFloat() * 0.4F);
        villager.world.addEntity(potionentity);
    }
}
