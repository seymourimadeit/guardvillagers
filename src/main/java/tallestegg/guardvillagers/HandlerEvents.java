package tallestegg.guardvillagers;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableWitchTargetGoal;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import tallestegg.guardvillagers.client.GuardSounds;
import tallestegg.guardvillagers.common.entities.Guard;
import tallestegg.guardvillagers.common.entities.ai.goals.*;
import tallestegg.guardvillagers.configuration.GuardConfig;

import java.util.List;
import java.util.function.Predicate;

@EventBusSubscriber(modid = GuardVillagers.MODID)
public class HandlerEvents {
    private static final Predicate<LivingEntity> ISNT_BABY = mob -> !mob.isBaby();
    @SubscribeEvent
    public static void onEntityTarget(LivingChangeTargetEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Raider raider && raider.hasActiveRaid()) {
            return;
        }
        LivingEntity target = event.getNewAboutToBeSetTarget();
        if (target == null || entity.getType() == GuardEntityType.GUARD.get() || entity instanceof IronGolem) return;
        boolean isVillager = GuardConfig.COMMON.mobsGuardsProtectTargeted.get().contains(target.getEncodeId());
        if (isVillager) {
            List<Mob> list = entity.level().getEntitiesOfClass(Mob.class, entity.getBoundingBox().inflate(GuardConfig.COMMON.GuardVillagerHelpRange.get(), 5.0D, GuardConfig.COMMON.GuardVillagerHelpRange.get()));
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
    public static void onEntityHurt(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        Entity trueSource = event.getContainer().getSource().getEntity();
        if (entity instanceof Raider raider && raider.hasActiveRaid()) {
            return;
        }

        if (trueSource instanceof Raider raider && raider.hasActiveRaid()) {
            return;
        }
        if (entity == null || trueSource == null) return;
        boolean isVillager = GuardConfig.COMMON.mobsGuardsProtectHurt.get().contains(entity.getEncodeId());
        if (isVillager && trueSource.getType() == GuardEntityType.GUARD.get() && !GuardConfig.COMMON.guardArrowsHurtVillagers.get()) {
            event.getContainer().setNewDamage(0.0F);
        }
        if (isVillager && event.getContainer().getSource().getEntity() instanceof Mob) {
            List<Mob> list = trueSource.level().getEntitiesOfClass(Mob.class, trueSource.getBoundingBox().inflate(GuardConfig.COMMON.GuardVillagerHelpRange.get(), 5.0D, GuardConfig.COMMON.GuardVillagerHelpRange.get()));
            for (Mob mob : list) {
                boolean type = mob.getType() == GuardEntityType.GUARD.get() || mob.getType() == EntityType.IRON_GOLEM;
                boolean trueSourceGolem = trueSource.getType() == GuardEntityType.GUARD.get() || trueSource.getType() == EntityType.IRON_GOLEM;
                if (!trueSourceGolem && type && mob.getTarget() == null) {
                    if (mob.getTeam() != null && entity.getTeam() != null && entity.getTeam().isAlliedTo(mob.getTeam()))
                        return;
                    else
                        mob.setTarget((Mob) event.getContainer().getSource().getEntity());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingSpawned(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Mob mob) {
            if (mob instanceof Raider raider && raider.hasActiveRaid()) {
                return;
            }
            boolean inActiveRaid = (mob instanceof Raider raider) && raider.hasActiveRaid();
            if (inActiveRaid) {
                return;
            }

            if (mob instanceof Raider) {
                if (GuardConfig.COMMON.RaidAnimals.get()) {
                    mob.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(((Raider) mob), Animal.class, false));
                }
            }

            if (GuardConfig.COMMON.MobsAttackGuards.get()) {
                if (mob instanceof Enemy && !GuardConfig.COMMON.MobBlackList.get().contains(mob.getEncodeId())) {
                    if (!(mob instanceof Spider))
                        mob.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(mob, Guard.class, false));
                    else
                        mob.targetSelector.addGoal(3, new AttackEntityDaytimeGoal<>((Spider) mob, Guard.class));
                }
            }

            if (mob instanceof AbstractIllager illager) {
                if (GuardConfig.COMMON.IllagersRunFromPolarBears.get())
                    illager.goalSelector.addGoal(2, new AvoidEntityGoal<>(illager, PolarBear.class, 6.0F, 1.0D, 1.2D));
                illager.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(illager, Guard.class, false));
            }

            if (mob instanceof AbstractVillager abstractvillager) {
                if (GuardConfig.COMMON.VillagersRunFromPolarBears.get())
                    abstractvillager.goalSelector.addGoal(2, new AvoidEntityGoal<>(abstractvillager, PolarBear.class, 6.0F, 1.0D, 1.2D));
                if (GuardConfig.COMMON.WitchesVillager.get())
                    abstractvillager.goalSelector.addGoal(2, new AvoidEntityGoal<>(abstractvillager, Witch.class, 6.0F, 1.0D, 1.2D));
            }

            if (mob instanceof IronGolem golem) {
                HurtByTargetGoal tolerateFriendlyFire = new HurtByTargetGoal(golem, Guard.class).setAlertOthers();
                golem.targetSelector.getAvailableGoals().stream().map(it -> it.getGoal()).filter(it -> it instanceof HurtByTargetGoal).findFirst().ifPresent(angerGoal -> {
                    golem.targetSelector.removeGoal(angerGoal);
                    golem.targetSelector.addGoal(2, tolerateFriendlyFire);
                });
                if (GuardConfig.COMMON.golemFloat.get()) {
                    golem.goalSelector.addGoal(0, new GetOutOfWaterGoal(golem, 1.0D));
                    golem.goalSelector.addGoal(1, new GolemFloatWaterGoal(golem));

                }
            }

            if (mob instanceof Zombie zombie && !(zombie instanceof ZombifiedPiglin)) {
                zombie.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(zombie, Guard.class, false));
            }

            if (mob instanceof Ravager ravager) {
                ravager.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(ravager, Guard.class, false));
            }

            if (mob instanceof Witch witch) {
                if (GuardConfig.COMMON.WitchesVillager.get()) {
                    witch.targetSelector.addGoal(3, new NearestAttackableWitchTargetGoal<>(witch, AbstractVillager.class, 10, true, false,(serverLevel, target) -> ISNT_BABY.test(serverLevel)));
                    witch.targetSelector.addGoal(3, new NearestAttackableWitchTargetGoal<>(witch, IronGolem.class, 10, true, false, null));
                    witch.targetSelector.addGoal(2, new NearestAttackableWitchTargetGoal<>(witch, Guard.class, 10, true, false, null));
                }
            }

            if (mob instanceof Cat cat) {
                cat.goalSelector.addGoal(1, new AvoidEntityGoal<>(cat, AbstractIllager.class, 12.0F, 1.0D, 1.2D));
            }
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        ItemStack itemstack = event.getEntity().getMainHandItem();
        Entity target = event.getTarget();
        if (itemstack.is(GuardVillagerTags.GUARD_CONVERT) && player.isCrouching()) {
            if (target instanceof Villager villager) {
                if (!villager.isBaby()) {
                    if (GuardConfig.COMMON.convertibleProfessions.get().contains(villager.getVillagerData().getProfession().name())) {
                        if (!GuardConfig.COMMON.ConvertVillagerIfHaveHOTV.get() || player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) && GuardConfig.COMMON.ConvertVillagerIfHaveHOTV.get()) {
                            convertVillager(villager, player);
                            if (!player.getAbilities().instabuild)
                                itemstack.shrink(1);
                        }
                    }
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
                            guard.playSound(GuardSounds.GUARD_YES.value());
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

    private static void convertVillager(LivingEntity entity, Player player) {
        player.swing(InteractionHand.MAIN_HAND);
        ItemStack itemstack = player.getItemBySlot(EquipmentSlot.MAINHAND);
        Guard guard = GuardEntityType.GUARD.get().create(entity.level(), EntitySpawnReason.EVENT);
        Villager villager = (Villager) entity;
        if (guard == null)
            return;
        if (entity.level().isClientSide) {
            ParticleOptions iparticledata = ParticleTypes.HAPPY_VILLAGER;
            for (int i = 0; i < 10; ++i) {
                double d0 = villager.getRandom().nextGaussian() * 0.02D;
                double d1 = villager.getRandom().nextGaussian() * 0.02D;
                double d2 = villager.getRandom().nextGaussian() * 0.02D;
                villager.level().addParticle(iparticledata, villager.getX() + (double) (villager.getRandom().nextFloat() * villager.getBbWidth() * 2.0F) - (double) villager.getBbWidth(), villager.getY() + 0.5D + (double) (villager.getRandom().nextFloat() * villager.getBbHeight()),
                        villager.getZ() + (double) (villager.getRandom().nextFloat() * villager.getBbWidth() * 2.0F) - (double) villager.getBbWidth(), d0, d1, d2);
            }
        }
        guard.copyPosition(villager);
        guard.playSound(GuardSounds.GUARD_YES.value(), 1.0F, 1.0F);
        guard.setItemSlot(EquipmentSlot.MAINHAND, itemstack.copy());
        guard.setVariant(villager.getVariant().toString());
        guard.setPersistenceRequired();
        guard.setCustomName(villager.getCustomName());
        guard.setCustomNameVisible(villager.isCustomNameVisible());
        guard.setDropChance(EquipmentSlot.HEAD, 100.0F);
        guard.setDropChance(EquipmentSlot.CHEST, 100.0F);
        guard.setDropChance(EquipmentSlot.FEET, 100.0F);
        guard.setDropChance(EquipmentSlot.LEGS, 100.0F);
        guard.setDropChance(EquipmentSlot.MAINHAND, 100.0F);
        guard.setDropChance(EquipmentSlot.OFFHAND, 100.0F);
        guard.getGossips().add(player.getUUID(), GossipType.MINOR_POSITIVE, GuardConfig.COMMON.reputationRequirement.get());
        villager.level().addFreshEntity(guard);
        villager.releasePoi(MemoryModuleType.HOME);
        villager.releasePoi(MemoryModuleType.JOB_SITE);
        villager.releasePoi(MemoryModuleType.MEETING_POINT);
        villager.discard();
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.SUMMONED_ENTITY.trigger(serverPlayer, guard);
            player.awardStat(GuardStats.GUARDS_MADE.get());
        }
    }
}
