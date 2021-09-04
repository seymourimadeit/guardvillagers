package tallestegg.guardvillagers.entities.ai.tasks;

/*public class HealGuardAndPlayerTask extends WorkAtPoi {
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
    protected boolean checkExtraStartConditions(ServerLevel worldIn, Villager owner) {
        if (!GuardConfig.ClericHealing) {
            return false;
        } else if (owner.getVillagerData().getProfession() != VillagerProfession.CLERIC || owner.isSleeping()) {
            return false;
        } else {
            List<LivingEntity> list = owner.level.getEntitiesOfClass(LivingEntity.class, owner.getBoundingBox().inflate(10.0D, 3.0D, 10.0D));
            if (!list.isEmpty()) {
                for (LivingEntity entityToHeal : list) {
                    if (entityToHeal != null) {
                        if (entityToHeal.getType() == GuardEntityType.GUARD.get() && entityToHeal != null && entityToHeal.isAlive() && entityToHeal.getHealth() < entityToHeal.getMaxHealth()
                                || entityToHeal instanceof Player && entityToHeal.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) && !((Player) entityToHeal).getAbilities().instabuild && entityToHeal.getHealth() < entityToHeal.getMaxHealth()) {
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
    protected boolean canStillUse(ServerLevel worldIn, Villager entityIn, long gameTimeIn) {
        return entityToHeal.isAlive() && entityToHeal.getHealth() < entityToHeal.getMaxHealth() && this.checkExtraStartConditions(worldIn, entityIn);
    }

    @Override
    protected void tick(ServerLevel worldIn, Villager owner, long gameTime) {
        if (entityToHeal == null)
            return;
        double d0 = owner.distanceToSqr(this.entityToHeal.getX(), this.entityToHeal.getY(), this.entityToHeal.getZ());
        boolean flag = owner.getSensing().canSee(this.entityToHeal);
        if (flag) {
            ++this.seeTime;
        } else {
            this.seeTime = 0;
        }
        BehaviorUtils.lookAtEntity(owner, entityToHeal);
        owner.lookAt(entityToHeal, 90.0F, 90.0F);
        if (!(d0 > (double) this.maxAttackDistance) && this.seeTime >= 5) {
            owner.getNavigation().stop();
        } else {
            owner.getNavigation().moveTo(entityToHeal, owner.getSpeed());
        }
        if (entityToHeal.distanceTo(owner) <= 5.0D) {
            owner.getMoveControl().strafe(-0.5F, 0);
        }
        if (--this.rangedAttackTime == 0 && entityToHeal.getHealth() < entityToHeal.getMaxHealth() && entityToHeal.isAlive()) {
            if (!flag) {
                return;
            }
            float f =  this.attackRadius;
            float lvt_5_1_ = Mth.clamp(f, 0.5F, 0.5F);
            this.throwPotion(owner, entityToHeal, lvt_5_1_);
            this.rangedAttackTime = Mth.floor(f * (float) (this.maxRangedAttackTime - this.attackIntervalMin) + (float) this.attackIntervalMin);
        } else if (this.rangedAttackTime < 0 && owner.distanceTo(entityToHeal) >= 5.0D) {
            float f2 = Mth.sqrt(d0) / this.attackRadius;
            this.rangedAttackTime = Mth.floor(f2 * (float) (this.maxRangedAttackTime - this.attackIntervalMin) + (float) this.attackIntervalMin);
        }
    }

    public void throwPotion(Villager villager, LivingEntity target, float distanceFactor) {
        Vec3 vec3d = target.getDeltaMovement();
        double d0 = target.getX() + vec3d.x - villager.getX();
        double d1 = target.getEyeY() - (double) 1.1F - villager.getY();
        double d2 = target.getZ() + vec3d.z - villager.getZ();
        float f = Mth.sqrt(d0 * d0 + d2 * d2);
        Potion potion = Potions.REGENERATION;
        if (target.getHealth() <= 4.0F) {
            potion = Potions.HEALING;
        } else {
            potion = Potions.REGENERATION;
        }
        BehaviorUtils.lookAtEntity(villager, target);
        ThrownPotion potionentity = new ThrownPotion(villager.level, villager);
        potionentity.setItem(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), potion));
        potionentity.xRot -= -20.0F;
        potionentity.shoot(d0, d1 + (double) (f * 0.2F), d2, 0.75F, 8.0F);
        villager.level.playSound((Player) null, villager.getX(), villager.getY(), villager.getZ(), SoundEvents.SPLASH_POTION_THROW, villager.getSoundSource(), 1.0F, 0.8F + villager.getRandom().nextFloat() * 0.4F);
        villager.level.addFreshEntity(potionentity);
    }
}*/
