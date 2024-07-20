package tallestegg.guardvillagers;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import tallestegg.guardvillagers.common.entities.Guard;
import tallestegg.guardvillagers.common.entities.ai.goals.AttackEntityDaytimeGoal;
import tallestegg.guardvillagers.common.entities.ai.goals.GetOutOfWaterGoal;
import tallestegg.guardvillagers.common.entities.ai.goals.HealGolemGoal;
import tallestegg.guardvillagers.common.entities.ai.goals.HealGuardAndPlayerGoal;
import tallestegg.guardvillagers.configuration.GuardConfig;

import java.util.List;

@EventBusSubscriber(modid = GuardVillagers.MODID)
public class HandlerEvents {
    @SubscribeEvent
    public static void onEntityTarget(LivingChangeTargetEvent event) {
        LivingEntity entity = event.getEntity();
        LivingEntity target = event.getNewAboutToBeSetTarget();
        if (target == null || entity.getType() == GuardEntityType.GUARD.get()) return;
        boolean isVillager = target.getType() == EntityType.VILLAGER || target.getType() == GuardEntityType.GUARD.get();
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
        if (entity == null || trueSource == null) return;
        boolean isVillager = entity.getType() == EntityType.VILLAGER || entity.getType() == GuardEntityType.GUARD.get();
        boolean isGolem = isVillager || entity.getType() == EntityType.IRON_GOLEM;
        if (isGolem && trueSource.getType() == GuardEntityType.GUARD.get() && !GuardConfig.COMMON.guardArrowsHurtVillagers.get()) {
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
    public static void onLivingTick(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof AbstractHorse horse) {
            Vec3 vec3 = new Vec3(horse.xxa, horse.yya, horse.zza);
            if (horse.hasControllingPassenger() && horse.getControllingPassenger() instanceof Guard) {
                horse.setSpeed((float) horse.getAttributeValue(Attributes.MOVEMENT_SPEED));
                horse.travel(vec3);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingSpawned(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Mob mob) {
            if (mob instanceof Raider) {
                if (((Raider) mob).hasActiveRaid() && GuardConfig.COMMON.RaidAnimals.get())
                    mob.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(((Raider) mob), Animal.class, false));
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

            if (mob instanceof Villager villager) {
                if (GuardConfig.COMMON.BlacksmithHealing.get())
                    villager.goalSelector.addGoal(1, new HealGolemGoal(villager));
                if (GuardConfig.COMMON.ClericHealing.get())
                    villager.goalSelector.addGoal(1, new HealGuardAndPlayerGoal(villager, 1.0D, 100, 0, 10.0F));
            }

            if (mob instanceof IronGolem golem) {
                HurtByTargetGoal tolerateFriendlyFire = new HurtByTargetGoal(golem, Guard.class).setAlertOthers();
                golem.targetSelector.getAvailableGoals().stream().map(it -> it.getGoal()).filter(it -> it instanceof HurtByTargetGoal).findFirst().ifPresent(angerGoal -> {
                    golem.targetSelector.removeGoal(angerGoal);
                    golem.targetSelector.addGoal(2, tolerateFriendlyFire);
                });
                if (GuardConfig.COMMON.golemFloat.get()) {
                    golem.goalSelector.addGoal(1, new tallestegg.guardvillagers.entities.ai.goals.GolemFloatWaterGoal(golem));
                    golem.goalSelector.addGoal(0, new GetOutOfWaterGoal(golem, 1.0D));

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

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        ItemStack itemstack = event.getEntity().getMainHandItem();
        Entity target = event.getTarget();
        if ((itemstack.getItem() instanceof SwordItem || itemstack.getItem() instanceof CrossbowItem) && player.isCrouching()) {
            if (target instanceof Villager villager) {
                if (!villager.isBaby()) {
                    if (villager.getVillagerData().getProfession() == VillagerProfession.NONE || villager.getVillagerData().getProfession() == VillagerProfession.NITWIT) {
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
        if (originalBlock.getBlock() instanceof BellBlock) {
            List<Guard> list = player.level().getEntitiesOfClass(Guard.class, player.getBoundingBox().inflate(32.0D, 32.0D, 32.0D));
            for (Guard guard : list) {
                if (GuardVillagers.canFollow(player)) {
                    guard.setOwnerId(player.getUUID());
                    guard.setFollowing(!guard.isFollowing());
                }
            }
        }
    }

    private static void convertVillager(LivingEntity entity, Player player) {
        player.swing(InteractionHand.MAIN_HAND);
        ItemStack itemstack = player.getItemBySlot(EquipmentSlot.MAINHAND);
        Guard guard = GuardEntityType.GUARD.get().create(entity.level());
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
        guard.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.0F);
        guard.setItemSlot(EquipmentSlot.MAINHAND, itemstack.copy());
        int i = Guard.getRandomTypeForBiome(guard.level(), guard.blockPosition());
        guard.setGuardVariant(i);
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
        if (player instanceof ServerPlayer)
            CriteriaTriggers.SUMMONED_ENTITY.trigger((ServerPlayer) player, guard);
    }
}
