package tallestegg.guardvillagers.entities.ai.goals;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import tallestegg.guardvillagers.GuardEntityType;

public class HealGuardAndPlayerGoal extends Goal {
    private final Mob healer;
    private LivingEntity mob;
    private int rangedAttackTime = -1;
    private final double entityMoveSpeed;
    private int seeTime;
    private final int attackIntervalMin;
    private final int maxRangedAttackTime;
    private final float attackRadius;
    private final float maxAttackDistance;
    protected final TargetingConditions predicate = TargetingConditions.forNonCombat().range(64.0D);

    public HealGuardAndPlayerGoal(Mob healer, double movespeed, int attackIntervalMin, int maxAttackTime, float maxAttackDistanceIn) {
        this.healer = healer;
        this.entityMoveSpeed = movespeed;
        this.attackIntervalMin = attackIntervalMin;
        this.maxRangedAttackTime = maxAttackTime;
        this.attackRadius = maxAttackDistanceIn;
        this.maxAttackDistance = maxAttackDistanceIn * maxAttackDistanceIn;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (((Villager) this.healer).getVillagerData().getProfession() != VillagerProfession.CLERIC || this.healer.isSleeping()) {
            return false;
        }
        List<LivingEntity> list = this.healer.level.getEntitiesOfClass(LivingEntity.class, this.healer.getBoundingBox().inflate(10.0D, 3.0D, 10.0D));
        if (!list.isEmpty()) {
            for (LivingEntity mob : list) {
                if (mob != null) {
                    if (mob instanceof Villager && mob.isAlive() && mob.getHealth() < mob.getMaxHealth() && mob != healer|| mob.getType() == GuardEntityType.GUARD.get() && mob != null && mob.isAlive() && mob.getHealth() < mob.getMaxHealth()
                            || mob instanceof Player && mob.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) && !((Player) mob).getAbilities().instabuild && mob.getHealth() < mob.getMaxHealth()) {
                        this.mob = mob;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse() && mob != null && mob.getHealth() < mob.getMaxHealth();
    }

    @Override
    public void stop() {
        this.mob = null;
        this.seeTime = 0;
        this.healer.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        this.rangedAttackTime = 0;
    }

    @Override
    public void tick() {
        if (mob == null)
            return;
        double d0 = this.healer.distanceToSqr(this.mob.getX(), this.mob.getY(), this.mob.getZ());
        boolean flag = this.healer.getSensing().hasLineOfSight(mob);
        if (flag) {
            ++this.seeTime;
        } else {
            this.seeTime = 0;
        }
        BehaviorUtils.lookAtEntity(healer, mob);
        if (!(d0 > (double) this.maxAttackDistance) && this.seeTime >= 5) {
            this.healer.getNavigation().stop();
        } else {
            this.healer.getNavigation().moveTo(this.healer, this.entityMoveSpeed);
        }
        if (mob.distanceTo(healer) <= 3.0D) {
            healer.getMoveControl().strafe(-0.5F, 0);
        }
        if (--this.rangedAttackTime == 0) {
            if (!flag) {
                return;
            }
            float f = this.attackRadius;
            float distanceFactor = Mth.clamp(f, 0.10F, 0.10F);
            this.throwPotion(mob, distanceFactor);
            this.rangedAttackTime = Mth.floor(f * (float) (this.maxRangedAttackTime - this.attackIntervalMin) + (float) this.attackIntervalMin);
        } else if (this.rangedAttackTime < 0) {
            this.rangedAttackTime = Mth.floor(Mth.lerp(Math.sqrt(d0) / (double)this.attackRadius, (double)this.attackIntervalMin, (double)this.maxAttackDistance));
        }
    }

    public void throwPotion(LivingEntity target, float distanceFactor) {
        Vec3 vec3d = target.getDeltaMovement();
        double d0 = target.getX() + vec3d.x - healer.getX();
        double d1 = target.getEyeY() - (double) 1.1F - healer.getY();
        double d2 = target.getZ() + vec3d.z - healer.getZ();
        float f = Mth.sqrt((float) (d0 * d0 + d2 * d2));
        Potion potion = Potions.REGENERATION;
        if (target.getHealth() <= 4.0F) {
            potion = Potions.HEALING;
        } else {
            potion = Potions.REGENERATION;
        }
        ThrownPotion potionentity = new ThrownPotion(healer.level, healer);
        potionentity.setItem(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), potion));
        potionentity.setXRot(-20.0F);
        potionentity.shoot(d0, d1 + (double) (f * 0.2F), d2, 0.75F, 8.0F);
        healer.level.playSound((Player) null, healer.getX(), healer.getY(), healer.getZ(), SoundEvents.SPLASH_POTION_THROW, healer.getSoundSource(), 1.0F, 0.8F + healer.getRandom().nextFloat() * 0.4F);
        healer.level.addFreshEntity(potionentity);
    }
}
