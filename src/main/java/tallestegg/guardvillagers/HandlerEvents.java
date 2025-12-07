package tallestegg.guardvillagers;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableWitchTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tallestegg.guardvillagers.client.GuardSounds;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.entities.Guard;
import tallestegg.guardvillagers.entities.ai.goals.*;

import java.util.List;
import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = GuardVillagers.MODID)
public class HandlerEvents {
    private static final Predicate<LivingEntity> ISNT_BABY = mob -> !mob.isBaby();

    @SubscribeEvent
    public static void onEntityTarget(LivingChangeTargetEvent event) {
        LivingEntity entity = event.getEntity();
        LivingEntity target = event.getNewTarget();
        if (target == null || entity.getType() == GuardEntityType.GUARD.get() || entity instanceof IronGolem) return;
        boolean isVillager = GuardConfig.COMMON.mobsGuardsProtectTargeted.get().contains(target.getEncodeId());
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
        boolean isVillager = GuardConfig.COMMON.mobsGuardsProtectHurt.get().contains(entity.getEncodeId());
        if (isVillager && trueSource.getType() == GuardEntityType.GUARD.get() && !GuardConfig.COMMON.guardArrowsHurtVillagers.get()) {
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
    public static void onEntityInteract(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getHitVec().getBlockPos();
        BlockState originalBlock = player.level().getBlockState(pos);
        if (GuardConfig.COMMON.multiFollow.get()) {
            if (originalBlock.getBlock() instanceof BellBlock && level.getBlockEntity(pos) instanceof BellBlockEntity bellBlockEntity) {
                if (!bellBlockEntity.shaking) {
                    List<Guard> list = player.level().getEntitiesOfClass(Guard.class, player.getBoundingBox().inflate(32.0D, 32.0D, 32.0D));
                    for (Guard guard : list) {
                        if (GuardVillagers.canFollow(player)) {
                            event.setCancellationResult(InteractionResult.SUCCESS);
                            guard.setFollowing(!guard.isFollowing());
                            guard.playSound(GuardSounds.GUARD_YES.get());
                            if (guard.isFollowing()) {
                                guard.setOwnerId(player.getUUID());
                                guard.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 1));
                                level.playSound(null, pos, SoundEvents.BELL_RESONATE, SoundSource.BLOCKS, 1.0F, 1.0F);
                            } else {
                                guard.removeEffect(MobEffects.GLOWING);
                            }
                        }
                    }
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
                if (mob instanceof Enemy && !GuardConfig.MobBlackList.contains(mob.getEncodeId())) {
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

            if (mob instanceof IronGolem golem) {
                HurtByTargetGoal tolerateFriendlyFire = new HurtByTargetGoal(golem, Guard.class).setAlertOthers();
                golem.targetSelector.availableGoals.stream().map(it -> it.goal).filter(it -> it instanceof HurtByTargetGoal).findFirst().ifPresent(angerGoal -> {
                    golem.targetSelector.removeGoal(angerGoal);
                    golem.targetSelector.addGoal(2, tolerateFriendlyFire);
                });
                if (GuardConfig.COMMON.ironGolemFloat.get()) {
                    golem.goalSelector.addGoal(1, new GolemFloatWaterGoal(golem));
                    golem.goalSelector.addGoal(0, new GetOutOfWaterGoal(golem, 1.0D));

                }
            }
            if (mob instanceof Zombie zombie && !(zombie instanceof NeutralMob)) {
                zombie.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(zombie, Guard.class, false));
            }

            if (mob instanceof Ravager ravager) {
                ravager.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(ravager, Guard.class, false));
            }

            if (mob instanceof Witch witch) {
                if (GuardConfig.WitchesVillager) {
                    witch.targetSelector.addGoal(3, new NearestAttackableWitchTargetGoal<>(witch, AbstractVillager.class, 10, true, false, ISNT_BABY));
                    witch.targetSelector.addGoal(3, new NearestAttackableWitchTargetGoal<>(witch, IronGolem.class, 10, true, false, null));
                    witch.targetSelector.addGoal(2, new NearestAttackableWitchTargetGoal<>(witch, Guard.class, 10, true, false, null));
                }
            }

            if (mob instanceof Cat cat) {
                cat.goalSelector.addGoal(1, new AvoidEntityGoal<>(cat, AbstractIllager.class, 12.0F, 1.0D, 1.2D));
            }
        }
    }
}
