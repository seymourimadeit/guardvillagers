package tallestegg.guardvillagers;

import java.util.List;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.entities.Guard;
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
            List<Mob> list = entity.level.getEntitiesOfClass(Mob.class, entity.getBoundingBox().inflate(GuardConfig.GuardVillagerHelpRange, 5.0D, GuardConfig.GuardVillagerHelpRange));
            for (Mob mob : list) {
                if ((mob.getType() == GuardEntityType.GUARD.get() || mob.getType() == EntityType.IRON_GOLEM) && mob.getTarget() == null) {
                    mob.setTarget(entity);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityHurt(LivingHurtEvent event) {
        LivingEntity entity = (LivingEntity) event.getEntity();
        Entity trueSource = (Entity) event.getSource().getEntity();
        if (entity == null || trueSource == null)
            return;
        boolean isVillager = entity.getType() == EntityType.VILLAGER || entity.getType() == GuardEntityType.GUARD.get();
        boolean isGolem = isVillager || entity.getType() == EntityType.IRON_GOLEM;
        if (isGolem && trueSource.getType() == GuardEntityType.GUARD.get() && !GuardConfig.guardArrowsHurtVillagers) {
            event.setAmount(0.0F);
            event.setCanceled(true);
        }
        if (isVillager && event.getSource().getEntity() instanceof Mob) {
            List<Mob> list = trueSource.level.getEntitiesOfClass(Mob.class, trueSource.getBoundingBox().inflate(GuardConfig.GuardVillagerHelpRange, 5.0D, GuardConfig.GuardVillagerHelpRange));
            for (Mob mob : list) {
                boolean type = mob.getType() == GuardEntityType.GUARD.get() || mob.getType() == EntityType.IRON_GOLEM;
                boolean trueSourceGolem = trueSource.getType() == GuardEntityType.GUARD.get() || trueSource.getType() == EntityType.IRON_GOLEM;
                if (type && mob.getTarget() == null && trueSource.getType() != GuardEntityType.GUARD.get() && !trueSourceGolem) {
                    mob.setTarget((Mob) event.getSource().getEntity());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingSpawned(EntityJoinWorldEvent event) {
        if (GuardConfig.RaidAnimals) {
            if (event.getEntity() instanceof Raider)
                if (((Raider) event.getEntity()).hasActiveRaid()) {
                    ((Raider) event.getEntity()).targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(((Raider) event.getEntity()), Animal.class, false));
                }
        }
        if (GuardConfig.AttackAllMobs) {
            if (event.getEntity() instanceof Enemy && !GuardConfig.MobBlackList.contains(event.getEntity().getEncodeId()) && !(event.getEntity() instanceof Spider)) {
                Mob mob = (Mob) event.getEntity();
                mob.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(mob, Guard.class, false));
            }
            if (event.getEntity() instanceof Enemy && !GuardConfig.MobBlackList.contains(event.getEntity().getEncodeId()) && event.getEntity() instanceof Spider) {
                Spider spider = (Spider) event.getEntity();
                spider.targetSelector.addGoal(3, new AttackEntityDaytimeGoal<>(spider, Guard.class));
            }
        }

        if (event.getEntity() instanceof AbstractIllager) {
            AbstractIllager illager = (AbstractIllager) event.getEntity();
            illager.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(illager, Guard.class, false));
        }

        if (event.getEntity() instanceof Guard) {
            Guard guard = (Guard) event.getEntity();
            guard.setCanPickUpLoot(false); // This will be deleted when I port to 1.17.
        }

        if (event.getEntity() instanceof AbstractVillager) {
            AbstractVillager villager = (AbstractVillager) event.getEntity();
            if (GuardConfig.WitchesVillager)
                villager.goalSelector.addGoal(2, new AvoidEntityGoal<>(villager, Witch.class, 6.0F, 1.0D, 1.2D));
        }

        if (event.getEntity() instanceof Villager) {
            Villager villager = (Villager) event.getEntity();
            if (GuardConfig.BlackSmithHealing)
                villager.goalSelector.addGoal(1, new HealGolemGoal(villager));
            if (GuardConfig.ClericHealing)
                villager.goalSelector.addGoal(1, new HealGuardAndPlayerGoal(villager, 1.0D, 100, 0, 10.0F));
        }

        if (event.getEntity() instanceof IronGolem) {
            IronGolem golem = (IronGolem) event.getEntity();
            HurtByTargetGoal tolerateFriendlyFire = new HurtByTargetGoal(golem, Guard.class).setAlertOthers();
            golem.targetSelector.availableGoals.stream().map(it -> it.goal).filter(it -> it instanceof HurtByTargetGoal).findFirst().ifPresent(angerGoal -> {
                golem.targetSelector.removeGoal(angerGoal);
                golem.targetSelector.addGoal(2, tolerateFriendlyFire);
            });
        }

        if (event.getEntity() instanceof Zombie) {
            Zombie zombie = (Zombie) event.getEntity();
            zombie.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(zombie, Guard.class, false));
        }

        if (event.getEntity() instanceof Ravager) {
            Ravager ravager = (Ravager) event.getEntity();
            ravager.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(ravager, Guard.class, false));
        }

        if (event.getEntity() instanceof Witch) {
            Witch witch = (Witch) event.getEntity();
            if (GuardConfig.WitchesVillager) {
                witch.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(witch, AbstractVillager.class, true));
                witch.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(witch, IronGolem.class, true));
                witch.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(witch, Guard.class, false));
            }
        }

        if (event.getEntity() instanceof Cat) {
            Cat cat = (Cat) event.getEntity();
            cat.goalSelector.addGoal(1, new AvoidEntityGoal<>(cat, AbstractIllager.class, 12.0F, 1.0D, 1.2D));
        }
    }
}
