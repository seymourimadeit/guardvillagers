package tallestegg.guardvillagers;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.AbstractIllagerEntity;
import net.minecraft.entity.monster.AbstractRaiderEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.monster.RavagerEntity;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.entity.monster.WitchEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.entities.GuardEntity;
import tallestegg.guardvillagers.entities.ai.goals.AttackEntityDaytimeGoal;
import tallestegg.guardvillagers.entities.ai.goals.HealGolemGoal;
import tallestegg.guardvillagers.entities.ai.goals.HealGuardAndPlayerGoal;

@Mod.EventBusSubscriber(modid = GuardVillagers.MODID)
public class HandlerEvents {
    @SubscribeEvent
    public static void onEntityTarget(LivingSetAttackTargetEvent event) {
        LivingEntity entity = (LivingEntity) event.getEntity();
        LivingEntity target = event.getTarget();
        if (target == null || entity.getType() == GuardEntityType.GUARD.get())
            return;
        boolean isVillager = target.getType() == EntityType.VILLAGER || target.getType() == GuardEntityType.GUARD.get();
        if (isVillager) {
            List<MobEntity> list = entity.world.getEntitiesWithinAABB(MobEntity.class, entity.getBoundingBox().grow(GuardConfig.GuardVillagerHelpRange, 5.0D, GuardConfig.GuardVillagerHelpRange));
            for (MobEntity mob : list) {
                if ((mob.getType() == GuardEntityType.GUARD.get() || mob.getType() == EntityType.IRON_GOLEM) && mob.getAttackTarget() == null) {
                    mob.setAttackTarget(entity);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityHurt(LivingHurtEvent event) {
        LivingEntity entity = (LivingEntity) event.getEntity();
        Entity trueSource = (Entity) event.getSource().getTrueSource();
        if (entity == null || trueSource == null)
            return;
        boolean isVillager = entity.getType() == EntityType.VILLAGER || entity.getType() == GuardEntityType.GUARD.get();
        boolean isGolem = isVillager || entity.getType() == EntityType.IRON_GOLEM;
        if (isGolem && trueSource.getType() == GuardEntityType.GUARD.get() && !GuardConfig.guardArrowsHurtVillagers) {
            event.setAmount(0.0F);
            event.setCanceled(true);
        }
        if (isVillager && event.getSource().getTrueSource() instanceof MobEntity) {
            List<MobEntity> list = trueSource.world.getEntitiesWithinAABB(MobEntity.class, trueSource.getBoundingBox().grow(GuardConfig.GuardVillagerHelpRange, 5.0D, GuardConfig.GuardVillagerHelpRange));
            for (MobEntity mob : list) {
                boolean type = mob.getType() == GuardEntityType.GUARD.get() || mob.getType() == EntityType.IRON_GOLEM;
                boolean trueSourceGolem = trueSource.getType() == GuardEntityType.GUARD.get() || trueSource.getType() == EntityType.IRON_GOLEM;
                if (type && mob.getAttackTarget() == null && trueSource.getType() != GuardEntityType.GUARD.get() && !trueSourceGolem) {
                    mob.setAttackTarget((MobEntity) event.getSource().getTrueSource());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingSpawned(EntityJoinWorldEvent event) {

        if (GuardConfig.RaidAnimals) {
            if (event.getEntity() instanceof AbstractRaiderEntity)
                if (((AbstractRaiderEntity) event.getEntity()).isRaidActive()) {
                    ((AbstractRaiderEntity) event.getEntity()).targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(((AbstractRaiderEntity) event.getEntity()), AnimalEntity.class, false));
                }
        }
        if (GuardConfig.AttackAllMobs) {
            if (event.getEntity() instanceof IMob && !GuardConfig.MobBlackList.contains(event.getEntity().getEntityString()) && !(event.getEntity() instanceof SpiderEntity)) {
                MobEntity mob = (MobEntity) event.getEntity();
                mob.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(mob, GuardEntity.class, false));
            }
            if (event.getEntity() instanceof IMob && !GuardConfig.MobBlackList.contains(event.getEntity().getEntityString()) && event.getEntity() instanceof SpiderEntity) {
                SpiderEntity spider = (SpiderEntity) event.getEntity();
                spider.targetSelector.addGoal(3, new AttackEntityDaytimeGoal<>(spider, GuardEntity.class));
            }
        }

        if (event.getEntity() instanceof AbstractIllagerEntity) {
            AbstractIllagerEntity illager = (AbstractIllagerEntity) event.getEntity();
            illager.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(illager, GuardEntity.class, false));
        }

        if (event.getEntity() instanceof GuardEntity) {
            GuardEntity guard = (GuardEntity) event.getEntity();
            guard.setCanPickUpLoot(false); // This will be deleted when I port to 1.17.
        }

        if (event.getEntity() instanceof AbstractVillagerEntity) {
            AbstractVillagerEntity villager = (AbstractVillagerEntity) event.getEntity();
            if (GuardConfig.WitchesVillager)
                villager.goalSelector.addGoal(2, new AvoidEntityGoal<>(villager, WitchEntity.class, 6.0F, 1.0D, 1.2D));
        }

        if (event.getEntity() instanceof VillagerEntity) {
            VillagerEntity villager = (VillagerEntity) event.getEntity();
            if (GuardConfig.BlackSmithHealing)
                villager.goalSelector.addGoal(1, new HealGolemGoal(villager));
            if (GuardConfig.ClericHealing)
                villager.goalSelector.addGoal(1, new HealGuardAndPlayerGoal(villager, 1.0D, 100, 0, 10.0F));
        }

        if (event.getEntity() instanceof IronGolemEntity) {
            IronGolemEntity golem = (IronGolemEntity) event.getEntity();
            HurtByTargetGoal tolerateFriendlyFire = new HurtByTargetGoal(golem, GuardEntity.class).setCallsForHelp();
            golem.targetSelector.goals.stream().map(it -> it.inner).filter(it -> it instanceof HurtByTargetGoal).findFirst().ifPresent(angerGoal -> {
                golem.targetSelector.removeGoal(angerGoal);
                golem.targetSelector.addGoal(2, tolerateFriendlyFire);
            });
        }

        if (event.getEntity() instanceof ZombieEntity) {
            ZombieEntity zombie = (ZombieEntity) event.getEntity();
            zombie.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(zombie, GuardEntity.class, false));
        }

        if (event.getEntity() instanceof RavagerEntity) {
            RavagerEntity ravager = (RavagerEntity) event.getEntity();
            ravager.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(ravager, GuardEntity.class, false));
        }

        if (event.getEntity() instanceof WitchEntity) {
            WitchEntity witch = (WitchEntity) event.getEntity();
            if (GuardConfig.WitchesVillager) {
                witch.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(witch, AbstractVillagerEntity.class, true));
                witch.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(witch, IronGolemEntity.class, true));
                witch.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(witch, GuardEntity.class, false));
            }
        }

        if (event.getEntity() instanceof CatEntity) {
            CatEntity cat = (CatEntity) event.getEntity();
            cat.goalSelector.addGoal(1, new AvoidEntityGoal<>(cat, AbstractIllagerEntity.class, 12.0F, 1.0D, 1.2D));
        }
    }
}
