package tallestegg.guardvillagers;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.entities.Guard;
import tallestegg.guardvillagers.entities.ai.goals.AttackEntityDaytimeGoal;
import tallestegg.guardvillagers.entities.ai.goals.GetOutOfWaterGoal;
import tallestegg.guardvillagers.entities.ai.goals.HealGolemGoal;
import tallestegg.guardvillagers.entities.ai.goals.HealGuardAndPlayerGoal;

import java.util.List;

@Mod.EventBusSubscriber(modid = GuardVillagers.MODID)
public class HandlerEvents {
    @SubscribeEvent
    public static void onEntityTarget(LivingChangeTargetEvent event) {
        LivingEntity entity = event.getEntity();
        LivingEntity target = event.getNewTarget();
        if (target == null || entity.getType() == GuardEntityType.GUARD.get()) return;
        boolean isVillager = target.getType() == EntityType.VILLAGER || target.getType() == GuardEntityType.GUARD.get();
        if (isVillager) {
            List<Mob> list = entity.level().getEntitiesOfClass(Mob.class, entity.getBoundingBox().inflate(GuardConfig.GuardVillagerHelpRange, 5.0D, GuardConfig.GuardVillagerHelpRange));
            for (Mob mob : list) {
                if ((mob.getTarget() == null) && (mob.getType() == GuardEntityType.GUARD.get() || mob.getType() == EntityType.IRON_GOLEM)) {
                    if (mob.getTeam() != null && entity.getTeam() != null && entity.getTeam().isAlliedTo(mob.getTeam()))
                        return;
                    else
                        mob.setTarget(entity);
                }
            }
        }

        if (entity instanceof IronGolem golem && target instanceof Guard) golem.setTarget(null);
    }

    @SubscribeEvent
    public static void onEntityHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        Entity trueSource = event.getSource().getEntity();
        if (entity == null || trueSource == null) return;
        boolean isVillager = entity.getType() == EntityType.VILLAGER || entity.getType() == GuardEntityType.GUARD.get();
        boolean isGolem = isVillager || entity.getType() == EntityType.IRON_GOLEM;
        if (isGolem && trueSource.getType() == GuardEntityType.GUARD.get() && !GuardConfig.guardArrowsHurtVillagers) {
            event.setAmount(0.0F);
            event.setCanceled(true);
        }
        if (isVillager && event.getSource().getEntity() instanceof Mob) {
            List<Mob> list = trueSource.level().getEntitiesOfClass(Mob.class, trueSource.getBoundingBox().inflate(GuardConfig.GuardVillagerHelpRange, 5.0D, GuardConfig.GuardVillagerHelpRange));
            for (Mob mob : list) {
                boolean type = mob.getType() == GuardEntityType.GUARD.get() || mob.getType() == EntityType.IRON_GOLEM;
                boolean trueSourceGolem = trueSource.getType() == GuardEntityType.GUARD.get() || trueSource.getType() == EntityType.IRON_GOLEM;
                if (!trueSourceGolem && type && mob.getTarget() == null) {
                    if (mob.getTeam() != null && entity.getTeam() != null && entity.getTeam().isAlliedTo(mob.getTeam()))
                        return;
                    else
                        mob.setTarget((Mob) event.getSource().getEntity());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof AbstractHorse horse) {
            Vec3 vec3 = new Vec3((double) horse.xxa, (double) horse.yya, (double) horse.zza);
            if (horse.hasControllingPassenger() && horse.getControllingPassenger() instanceof Guard guard) {
                horse.setSpeed((float) horse.getAttributeValue(Attributes.MOVEMENT_SPEED));
                horse.travel(vec3);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingSpawned(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Mob mob) {
            if (mob instanceof Raider) {
                if (((Raider) mob).hasActiveRaid() && GuardConfig.RaidAnimals)
                    mob.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(mob, Animal.class, false));
            }
            if (GuardConfig.COMMON.MobsAttackGuards.get()) {
                if (mob instanceof Enemy && !GuardConfig.MobBlackList.contains(mob.getEncodeId()) && !(mob instanceof Spider)) {
                    if (!(mob instanceof Spider))
                        mob.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(mob, Guard.class, false));
                    else
                        mob.targetSelector.addGoal(3, new AttackEntityDaytimeGoal<>((Spider) mob, Guard.class));
                }
            }

            if (mob instanceof AbstractIllager illager) {
                if (GuardConfig.IllagersRunFromPolarBears)
                    illager.goalSelector.addGoal(2, new AvoidEntityGoal<>(illager, PolarBear.class, 6.0F, 1.0D, 1.2D));
                illager.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(illager, Guard.class, false));
            }

            if (mob instanceof AbstractVillager abstractvillager) {
                if (GuardConfig.VillagersRunFromPolarBears)
                    abstractvillager.goalSelector.addGoal(2, new AvoidEntityGoal<>(abstractvillager, PolarBear.class, 6.0F, 1.0D, 1.2D));
                if (GuardConfig.WitchesVillager)
                    abstractvillager.goalSelector.addGoal(2, new AvoidEntityGoal<>(abstractvillager, Witch.class, 6.0F, 1.0D, 1.2D));
            }

            if (mob instanceof Villager villager) {
                if (GuardConfig.BlackSmithHealing) villager.goalSelector.addGoal(1, new HealGolemGoal(villager));
                if (GuardConfig.ClericHealing)
                    villager.goalSelector.addGoal(1, new HealGuardAndPlayerGoal(villager, 1.0D, 100, 0, 10.0F));
            }

            if (mob instanceof IronGolem golem) {
                HurtByTargetGoal tolerateFriendlyFire = new HurtByTargetGoal(golem, Guard.class).setAlertOthers();
                golem.targetSelector.availableGoals.stream().map(it -> it.goal).filter(it -> it instanceof HurtByTargetGoal).findFirst().ifPresent(angerGoal -> {
                    golem.targetSelector.removeGoal(angerGoal);
                    golem.targetSelector.addGoal(2, tolerateFriendlyFire);
                });
                golem.goalSelector.addGoal(1, new FloatGoal(golem));
                golem.goalSelector.addGoal(0, new GetOutOfWaterGoal(golem, 1.0D));
            }

            if (mob instanceof Zombie zombie && !(zombie instanceof ZombifiedPiglin)) {
                zombie.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(zombie, Guard.class, false));
            }

            if (mob instanceof Ravager ravager) {
                ravager.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(ravager, Guard.class, false));
            }

            if (mob instanceof Witch witch) {
                if (GuardConfig.WitchesVillager) {
                    witch.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(witch, AbstractVillager.class, true));
                    witch.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(witch, IronGolem.class, true));
                    witch.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(witch, Guard.class, false));
                }
            }

            if (mob instanceof Cat cat) {
                cat.goalSelector.addGoal(1, new AvoidEntityGoal<>(cat, AbstractIllager.class, 12.0F, 1.0D, 1.2D));
            }
        }
    }
}
