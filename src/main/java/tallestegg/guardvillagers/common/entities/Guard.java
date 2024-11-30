package tallestegg.guardvillagers.common.entities;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import org.jetbrains.annotations.NotNull;
import tallestegg.guardvillagers.GuardEntityType;
import tallestegg.guardvillagers.GuardVillagers;
import tallestegg.guardvillagers.ModCompat;
import tallestegg.guardvillagers.client.GuardSounds;
import tallestegg.guardvillagers.common.entities.ai.goals.ArmorerRepairGuardArmorGoal;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.loot_tables.GuardLootTables;
import tallestegg.guardvillagers.networking.GuardOpenInventoryPacket;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class Guard extends PathfinderMob implements CrossbowAttackMob, RangedAttackMob, NeutralMob, ContainerListener, ReputationEventHandler {
    protected static final EntityDataAccessor<Optional<UUID>> OWNER_UNIQUE_ID = SynchedEntityData.defineId(Guard.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final AttributeModifier USE_ITEM_SPEED_PENALTY = new AttributeModifier(ResourceLocation.fromNamespaceAndPath(GuardVillagers.MODID, "item_slow_down"), -0.25D, AttributeModifier.Operation.ADD_VALUE);
    private static final EntityDataAccessor<Optional<BlockPos>> GUARD_POS = SynchedEntityData.defineId(Guard.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Boolean> PATROLLING = SynchedEntityData.defineId(Guard.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> GUARD_VARIANT = SynchedEntityData.defineId(Guard.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> RUNNING_TO_EAT = SynchedEntityData.defineId(Guard.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_CHARGING_STATE = SynchedEntityData.defineId(Guard.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> KICKING = SynchedEntityData.defineId(Guard.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FOLLOWING = SynchedEntityData.defineId(Guard.class, EntityDataSerializers.BOOLEAN);
    private static final Map<Pose, EntityDimensions> SIZE_BY_POSE = ImmutableMap.<Pose, EntityDimensions>builder()
            .put(Pose.SLEEPING, SLEEPING_DIMENSIONS)
            .put(Pose.FALL_FLYING, EntityDimensions.scalable(0.6F, 0.6F).withEyeHeight(0.4F))
            .put(Pose.SWIMMING, EntityDimensions.scalable(0.6F, 0.6F).withEyeHeight(0.4F))
            .put(Pose.SPIN_ATTACK, EntityDimensions.scalable(0.6F, 0.6F).withEyeHeight(0.4F))
            .put(
                    Pose.CROUCHING,
                    EntityDimensions.scalable(0.6F, 1.5F)
                            .withEyeHeight(1.27F)
                            .withAttachments(EntityAttachments.builder().attach(EntityAttachment.VEHICLE, new Vec3(0.0, 0.6, 0.0)
                            ))).put(Pose.DYING, EntityDimensions.fixed(0.2F, 0.2F).withEyeHeight(1.62F))
            .build();
    private static final UniformInt angerTime = TimeUtil.rangeOfSeconds(20, 39);
    private final GossipContainer gossips = new GossipContainer();
    public long lastGossipTime;
    public long lastGossipDecayTime;
    public SimpleContainer guardInventory = new SimpleContainer(6);
    public int kickTicks;
    public int shieldCoolDown;
    public int kickCoolDown;
    public boolean interacting;
    protected boolean spawnWithArmor;
    private int remainingPersistentAngerTime;
    private UUID persistentAngerTarget;

    public Guard(EntityType<? extends Guard> type, Level world) {
        super(type, world);
        this.guardInventory.addListener(this);
        this.setPersistenceRequired();
        if (GuardConfig.COMMON.GuardsOpenDoors.get())
            ((GroundPathNavigation) this.getNavigation()).setCanOpenDoors(true);
    }

    public static int slotToInventoryIndex(EquipmentSlot slot) {
        return switch (slot) {
            case CHEST -> 1;
            case FEET -> 3;
            case HEAD -> 0;
            case LEGS -> 2;
            default -> 0;
        };
    }

    public static String getVariantFromBiome(LevelAccessor world, BlockPos pos) {
        VillagerType type = VillagerType.byBiome(world.getBiome(pos));
        return type.toString();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, GuardConfig.STARTUP.healthModifier.get()).add(Attributes.MOVEMENT_SPEED, GuardConfig.STARTUP.speedModifier.get()).add(Attributes.ATTACK_DAMAGE, 1.0D).add(Attributes.FOLLOW_RANGE, GuardConfig.STARTUP.followRangeModifier.get());
    }

    @SuppressWarnings({"deprecation", "OverrideOnly"})
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor worldIn, @NotNull DifficultyInstance difficultyIn, @NotNull MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn) {
        this.setPersistenceRequired();
        String type = getVariantFromBiome(level(), this.blockPosition());
        this.setVariant(type);
        RandomSource randomsource = worldIn.getRandom();
        this.populateDefaultEquipmentSlots(randomsource, difficultyIn);
        return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn);
    }

    @Override
    protected void doPush(@NotNull Entity entityIn) {
        if (entityIn instanceof PathfinderMob living) {
            boolean attackTargets = living.getTarget() instanceof Villager || living.getTarget() instanceof IronGolem || living.getTarget() instanceof Guard;
            if (attackTargets) this.setTarget(living);
        }
        super.doPush(entityIn);
    }

    @Nullable
    public BlockPos getPatrolPos() {
        return this.entityData.get(GUARD_POS).orElse(null);
    }

    public void setPatrolPos(BlockPos position) {
        this.entityData.set(GUARD_POS, Optional.ofNullable(position));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return GuardSounds.GUARD_AMBIENT.value();
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSourceIn) {
        return GuardSounds.GUARD_HURT.value();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return GuardSounds.GUARD_DEATH.value();
    }

    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel level, @NotNull DamageSource source, boolean recentlyHitIn) {
        for (int i = 0; i < this.guardInventory.getContainerSize(); ++i) {
            ItemStack itemstack = this.guardInventory.getItem(i);
            RandomSource randomsource = level().getRandom();
            if (!itemstack.isEmpty() && !EnchantmentHelper.has(itemstack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP) && randomsource.nextFloat() < GuardConfig.COMMON.chanceToDropEquipment.get().floatValue())
                this.spawnAtLocation(itemstack);
        }
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Type", 99)) { // To accommodate guard variants in the previous updates
            int variantint = compound.getInt("Type");
            if (variantint == 1)
                compound.putString("Variant", "desert");
            else if (variantint == 2)
                compound.putString("Variant", "savanna");
            else if (variantint == 3)
                compound.putString("Variant", "swamp");
            else if (variantint == 4)
                compound.putString("Variant", "jungle");
            else if (variantint == 5)
                compound.putString("Variant", "taiga");
            else if (variantint == 6)
                compound.putString("Variant", "snow");
            else if (variantint == 0)
                compound.putString("Variant", "plains");
        }
        UUID uuid = compound.hasUUID("Owner") ? compound.getUUID("Owner") : null;
        if (uuid != null) {
            try {
                this.setOwnerId(uuid);
            } catch (Throwable throwable) {
                this.setOwnerId(null);
            }
        }
        this.kickTicks = compound.getInt("KickTicks");
        this.setFollowing(compound.getBoolean("Following"));
        this.interacting = compound.getBoolean("Interacting");
        this.setPatrolling(compound.getBoolean("Patrolling"));
        this.shieldCoolDown = compound.getInt("KickCooldown");
        this.kickCoolDown = compound.getInt("ShieldCooldown");
        this.lastGossipDecayTime = compound.getLong("LastGossipDecay");
        this.lastGossipTime = compound.getLong("LastGossipTime");
        this.spawnWithArmor = compound.getBoolean("SpawnWithArmor");
        this.setVariant(compound.getString("Variant"));
        if (compound.contains("PatrolPosX")) {
            int x = compound.getInt("PatrolPosX");
            int y = compound.getInt("PatrolPosY");
            int z = compound.getInt("PatrolPosZ");
            this.entityData.set(GUARD_POS, Optional.of(new BlockPos(x, y, z)));
        }
        ListTag listtag = compound.getList("Gossips", 10);
        this.gossips.update(new Dynamic<>(NbtOps.INSTANCE, listtag));
        ListTag listnbt = compound.getList("Inventory", 9);
        for (int i = 0; i < listnbt.size(); ++i) {
            CompoundTag compoundnbt = listnbt.getCompound(i);
            int j = compoundnbt.getByte("Slot") & 255;
            ItemStack stack = ItemStack.parseOptional(this.registryAccess(), compoundnbt);
            if (!stack.isEmpty())
                this.guardInventory.setItem(j, stack);
            else
                listtag.add(new CompoundTag());
        }
        if (compound.contains("ArmorItems", 9)) {
            ListTag armorItems = compound.getList("ArmorItems", 10);
            for (int i = 0; i < this.armorItems.size(); ++i) {
                ItemStack stack = ItemStack.parseOptional(this.registryAccess(), armorItems.getCompound(i));
                if (!stack.isEmpty()) {
                    int index = Guard.slotToInventoryIndex(this.getEquipmentSlotForItem(ItemStack.parse(this.registryAccess(), armorItems.getCompound(i)).orElse(ItemStack.EMPTY)));
                    this.guardInventory.setItem(index, stack);
                } else {
                    listtag.add(new CompoundTag());
                }
            }
            if (compound.contains("HandItems", 9)) {
                ListTag handItems = compound.getList("HandItems", 10);
                for (int i = 0; i < this.handItems.size(); ++i) {
                    int handSlot = i == 0 ? 5 : 4;
                    if (!ItemStack.parseOptional(this.registryAccess(), handItems.getCompound(i)).isEmpty())
                        this.guardInventory.setItem(handSlot, ItemStack.parseOptional(this.registryAccess(), handItems.getCompound(i)));
                    else
                        listtag.add(new CompoundTag());
                }
                if (!level().isClientSide) this.readPersistentAngerSaveData(level(), compound);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("Variant", this.getVariant());
        compound.putInt("KickTicks", this.kickTicks);
        compound.putInt("ShieldCooldown", this.shieldCoolDown);
        compound.putInt("KickCooldown", this.kickCoolDown);
        compound.putBoolean("Following", this.isFollowing());
        compound.putBoolean("Interacting", this.interacting);
        compound.putBoolean("Patrolling", this.isPatrolling());
        compound.putBoolean("SpawnWithArmor", this.spawnWithArmor);
        compound.putLong("LastGossipTime", this.lastGossipTime);
        compound.putLong("LastGossipDecay", this.lastGossipDecayTime);
        if (this.getOwnerId() != null) {
            compound.putUUID("Owner", this.getOwnerId());
        }
        ListTag listnbt = new ListTag();
        for (int i = 0; i < this.guardInventory.getContainerSize(); ++i) {
            ItemStack itemstack = this.guardInventory.getItem(i);
            if (!itemstack.isEmpty()) {
                CompoundTag compoundnbt = new CompoundTag();
                compoundnbt.putByte("Slot", (byte) i);
                listnbt.add(itemstack.save(this.registryAccess(), compoundnbt));
            } else {
                listnbt.add(new CompoundTag());
            }
        }
        compound.put("Inventory", listnbt);
        if (this.getPatrolPos() != null) {
            compound.putInt("PatrolPosX", this.getPatrolPos().getX());
            compound.putInt("PatrolPosY", this.getPatrolPos().getY());
            compound.putInt("PatrolPosZ", this.getPatrolPos().getZ());
        }
        compound.put("Gossips", this.gossips.store(NbtOps.INSTANCE));
        this.addPersistentAngerSaveData(compound);
    }

    private void maybeDecayGossip() {
        long i = level().getGameTime();
        if (this.lastGossipDecayTime == 0L) {
            this.lastGossipDecayTime = i;
        } else if (i >= this.lastGossipDecayTime + 24000L) {
            this.gossips.decay();
            this.lastGossipDecayTime = i;
        }
    }

    @Override
    protected void completeUsingItem() {
        if (this.isUsingItem()) {
            InteractionHand interactionhand = this.getUsedItemHand();
            if (!this.useItem.equals(this.getItemInHand(interactionhand))) {
                this.releaseUsingItem();
            } else {
                if (!this.useItem.isEmpty() && this.isUsingItem()) {
                    this.triggerItemUseEffects(this.useItem, 16);
                    ItemStack copy = this.useItem.copy();
                    ItemStack itemstack = EventHooks.onItemUseFinish(this, copy, getUseItemRemainingTicks(), this.useItem.finishUsingItem(this.level(), this));
                    if (itemstack != this.useItem) {
                        this.setItemInHand(interactionhand, itemstack);
                    }
                    if (!(this.useItem.getUseAnimation() == UseAnim.EAT)) this.useItem.shrink(1);
                    this.stopUsingItem();
                }

            }
        }
    }

    @Override
    public @NotNull ItemStack getItemBySlot(EquipmentSlot pSlot) {
        return switch (pSlot) {
            case HEAD -> this.guardInventory.getItem(0);
            case CHEST -> this.guardInventory.getItem(1);
            case LEGS -> this.guardInventory.getItem(2);
            case FEET -> this.guardInventory.getItem(3);
            case OFFHAND -> this.guardInventory.getItem(4);
            case MAINHAND -> this.guardInventory.getItem(5);
            default -> ItemStack.EMPTY;
        };
    }

    public GossipContainer getGossips() {
        return this.gossips;
    }

    public int getPlayerReputation(Player player) {
        return this.gossips.getReputation(player.getUUID(), (gossipType) -> true);
    }

    @Nullable
    public LivingEntity getOwner() {
        try {
            UUID uuid = this.getOwnerId();
            boolean heroOfTheVillage = uuid != null && level().getPlayerByUUID(uuid) != null && Objects.requireNonNull(level().getPlayerByUUID(uuid)).hasEffect(MobEffects.HERO_OF_THE_VILLAGE);
            return uuid == null || (level().getPlayerByUUID(uuid) != null && (!heroOfTheVillage && GuardConfig.COMMON.followHero.get()) || !GuardConfig.COMMON.followHero.get() && level().getPlayerByUUID(uuid) == null) ? null : level().getPlayerByUUID(uuid);
        } catch (IllegalArgumentException illegalargumentexception) {
            return null;
        }
    }

    public boolean isOwner(LivingEntity entityIn) {
        return entityIn == this.getOwner();
    }

    @Nullable
    public UUID getOwnerId() {
        return this.entityData.get(OWNER_UNIQUE_ID).orElse(null);
    }

    public void setOwnerId(@Nullable UUID p_184754_1_) {
        this.entityData.set(OWNER_UNIQUE_ID, Optional.ofNullable(p_184754_1_));
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity entityIn) {
        if (this.isKicking()) {
            ((LivingEntity) entityIn).knockback(1.0F, Mth.sin(this.getYRot() * ((float) Math.PI / 180F)), (-Mth.cos(this.getYRot() * ((float) Math.PI / 180F))));
            this.kickTicks = 10;
            level().broadcastEntityEvent(this, (byte) 4);
            this.lookAt(entityIn, 90.0F, 90.0F);
        }
        ItemStack hand = this.getMainHandItem();
        hand.hurtAndBreak(1, this, EquipmentSlot.MAINHAND);
        return super.doHurtTarget(entityIn);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 4) {
            this.kickTicks = 10;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public boolean isImmobile() {
        return this.interacting || super.isImmobile();
    }

    @Override
    public void die(@NotNull DamageSource source) {
        if ((level().getDifficulty() == Difficulty.NORMAL || level().getDifficulty() == Difficulty.HARD) && source.getEntity() instanceof Zombie && EventHooks.canLivingConvert((LivingEntity) source.getEntity(), EntityType.ZOMBIE_VILLAGER, (timer) -> {
        })) {
            ZombieVillager zombieguard = this.convertTo(EntityType.ZOMBIE_VILLAGER, true);
            if (level().getDifficulty() != Difficulty.HARD && this.random.nextBoolean() || zombieguard == null) {
                return;
            }
            if (!this.isSilent()) level().levelEvent(null, 1026, this.blockPosition(), 0);
            this.discard();
        }
        super.die(source);
    }

    @Override
    public @NotNull ItemStack eat(@NotNull Level world, ItemStack stack, @NotNull FoodProperties foodProperties) {
        if (stack.getUseAnimation() == UseAnim.EAT) {
            this.heal((foodProperties.nutrition()));
        }
        world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
        super.eat(world, stack, foodProperties);
        return stack;
    }

    @Override
    public void aiStep() {
        if (this.kickTicks > 0) --this.kickTicks;
        if (this.kickCoolDown > 0) --this.kickCoolDown;
        if (this.shieldCoolDown > 0) --this.shieldCoolDown;
        if (this.getHealth() < this.getMaxHealth() && this.tickCount % 200 == 0) {
            this.heal(GuardConfig.COMMON.amountOfHealthRegenerated.get().floatValue());
        }
        if (spawnWithArmor) {
            getItemsFromLootTable(this);
            this.spawnWithArmor = false;
        }
        if (!level().isClientSide) this.updatePersistentAnger((ServerLevel) level(), true);
        this.updateSwingTime();
        super.aiStep();
    }

    @Override
    public void tick() {
        this.maybeDecayGossip();
        super.tick();
    }

    @Override
    public @NotNull EntityDimensions getDefaultDimensions(@NotNull Pose pose) {
        return SIZE_BY_POSE.getOrDefault(pose, EntityDimensions.scalable(0.6F, 1.95F));
    }

    @Override
    protected void blockUsingShield(@NotNull LivingEntity entityIn) {
        super.blockUsingShield(entityIn);
        this.playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 1.0F);
        if (entityIn.getMainHandItem().canDisableShield(this.useItem, this, entityIn)) this.disableShield();
    }

    @Override
    protected void hurtCurrentlyUsedShield(float damage) {
        if (this.useItem.canPerformAction(ItemAbilities.SHIELD_BLOCK)) {
            if (damage >= 3.0F) {
                int i = 1 + Mth.floor(damage);
                InteractionHand hand = this.getUsedItemHand();
                this.useItem.hurtAndBreak(i, this, LivingEntity.getSlotForHand(hand));
                if (this.useItem.isEmpty()) {
                    if (hand == InteractionHand.MAIN_HAND) {
                        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    } else {
                        this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                    }
                    this.useItem = ItemStack.EMPTY;
                    this.playSound(SoundEvents.SHIELD_BREAK, 0.8F, 0.8F + level().random.nextFloat() * 0.4F);
                }
            }
        }
    }

    @Override
    public void startUsingItem(@NotNull InteractionHand hand) {
        super.startUsingItem(hand);
        ItemStack itemstack = this.getItemInHand(hand);
        if (itemstack.canPerformAction(ItemAbilities.SHIELD_BLOCK)) {
            AttributeInstance modifiableattributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
            assert modifiableattributeinstance != null;
            modifiableattributeinstance.removeModifier(USE_ITEM_SPEED_PENALTY);
            modifiableattributeinstance.addTransientModifier(USE_ITEM_SPEED_PENALTY);
        }
    }

    @Override
    public void stopUsingItem() {
        super.stopUsingItem();
        if (this.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(USE_ITEM_SPEED_PENALTY.id()))
            this.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(USE_ITEM_SPEED_PENALTY);
    }

    public void disableShield() {
        this.shieldCoolDown = 100;
        this.stopUsingItem();
        this.level().broadcastEntityEvent(this, (byte) 30);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder data) {
        super.defineSynchedData(data);
        data.define(GUARD_VARIANT, VillagerType.PLAINS.toString());
        data.define(DATA_CHARGING_STATE, false);
        data.define(KICKING, false);
        data.define(OWNER_UNIQUE_ID, Optional.empty());
        data.define(FOLLOWING, false);
        data.define(GUARD_POS, Optional.empty());
        data.define(PATROLLING, false);
        data.define(RUNNING_TO_EAT, false);
    }

    public void setChargingCrossbow(boolean charging) {
        this.entityData.set(DATA_CHARGING_STATE, charging);
    }

    public boolean isKicking() {
        return this.entityData.get(KICKING);
    }

    public void setKicking(boolean kicking) {
        this.entityData.set(KICKING, kicking);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource source, DifficultyInstance instance) {
        this.handDropChances[EquipmentSlot.MAINHAND.getIndex()] = 100.0F;
        this.handDropChances[EquipmentSlot.OFFHAND.getIndex()] = 100.0F;
        this.spawnWithArmor = true;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new KickGoal(this));
        this.goalSelector.addGoal(0, new GuardEatFoodGoal(this));
        this.goalSelector.addGoal(0, new RaiseShieldGoal(this));
        this.goalSelector.addGoal(1, new GuardRunToEatGoal(this));
        this.goalSelector.addGoal(3, new RangedCrossbowAttackPassiveGoal<>(this, 1.0D, 8.0F));
        this.goalSelector.addGoal(3, new GuardBowAttack(this, 0.5D, 20, 15.0F));
        if (ModList.get().isLoaded("musketmod"))
            this.goalSelector.addGoal(3, new ModCompat.UseMusketGoal(this, 20, 15.0F));
        this.goalSelector.addGoal(3, new GuardMeleeGoal(this, 0.8D, true));
        this.goalSelector.addGoal(4, new FollowHeroGoal(this, 0.8F, 10.0F, 4.0F));
        if (GuardConfig.COMMON.GuardsRunFromPolarBears.get())
            this.goalSelector.addGoal(4, new AvoidEntityGoal<>(this, PolarBear.class, 12.0F, 1.0D, 1.2D));
        this.goalSelector.addGoal(4, new MoveBackToVillageGoal(this, 0.5D, false));
        if (GuardConfig.COMMON.GuardsOpenDoors.get())
            this.goalSelector.addGoal(4, new GuardInteractDoorGoal(this, true));
        if (GuardConfig.COMMON.GuardFormation.get())
            this.goalSelector.addGoal(6, new FollowShieldGuards(this)); // phalanx
        if (GuardConfig.COMMON.ClericHealing.get()) this.goalSelector.addGoal(6, new RunToClericGoal(this));
        if (GuardConfig.COMMON.armorersRepairGuardArmor.get())
            this.goalSelector.addGoal(6, new ArmorerRepairGuardArmorGoal(this));
        this.goalSelector.addGoal(5, new WalkBackToCheckPointGoal(this, 0.5D));
        this.goalSelector.addGoal(5, new GolemRandomStrollInVillageGoal(this, 0.5D));
        if (GuardConfig.COMMON.guardPatrolVillageAi.get())
            this.goalSelector.addGoal(5, new MoveThroughVillageGoal(this, 0.5D, false, 4, () -> false));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.5D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, AbstractVillager.class, 8.0F));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new GuardLookAtAndStopMovingWhenBeingTheInteractionTarget(this));
        this.targetSelector.addGoal(2, (new HurtByTargetGoal(this, Guard.class, IronGolem.class)).setAlertOthers());
        this.targetSelector.addGoal(3, new HeroHurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new HeroHurtTargetGoal(this));
        this.targetSelector.addGoal(5, new DefendVillageGuardGoal(this));
        if (GuardConfig.COMMON.AttackAllMobs.get()) {
            this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 5, true, true, (mob) -> mob instanceof Enemy && !GuardConfig.COMMON.MobBlackList.get().contains(mob.getEncodeId())));
        } else {
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Ravager.class, true)); // To make witches and ravagers have a priority than other mobs this has to be done
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Witch.class, true));
            this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Raider.class, true));
            this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Zombie.class, true, (mob) -> !(mob instanceof ZombifiedPiglin)));
        }
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 5, true, true, (mob) -> GuardConfig.COMMON.MobWhiteList.get().contains(mob.getEncodeId())));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(4, new ResetUniversalAngerTargetGoal<>(this, false));
    }

    @Override
    public boolean mayBeLeashed() {
        return false;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        this.shieldCoolDown = 8;
        if (this.getMainHandItem().getItem() instanceof CrossbowItem) this.performCrossbowAttack(this, 1.6F);
        if (this.getMainHandItem().getItem() instanceof BowItem) {
            ItemStack hand = this.getMainHandItem();
            ItemStack itemstack = this.getProjectile(hand);
            AbstractArrow abstractarrowentity = ProjectileUtil.getMobArrow(this, itemstack, distanceFactor, hand);
            abstractarrowentity = ((BowItem) this.getMainHandItem().getItem()).customArrow(abstractarrowentity, itemstack, hand);
            double d0 = target.getX() - this.getX();
            double d1 = target.getY(0.3333333333333333D) - abstractarrowentity.getY();
            double d2 = target.getZ() - this.getZ();
            double d3 = Mth.sqrt((float) (d0 * d0 + d2 * d2));
            abstractarrowentity.shoot(d0, d1 + d3 * (double) 0.2F, d2, 1.6F, 1.0F);
            this.playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
            this.level().addFreshEntity(abstractarrowentity);
            hand.hurtAndBreak(1, this, EquipmentSlot.MAINHAND);
        }
        if (ModList.get().isLoaded("musketmod"))
            ModCompat.shootGun(this);
    }

    @Override
    public void performCrossbowAttack(LivingEntity p_32337_, float p_32338_) {
        InteractionHand interactionhand = ProjectileUtil.getWeaponHoldingHand(p_32337_, item -> item instanceof CrossbowItem);
        ItemStack itemstack = p_32337_.getItemInHand(interactionhand);
        if (itemstack.getItem() instanceof CrossbowItem crossbowitem) {
            crossbowitem.performShooting(
                    p_32337_.level(), p_32337_, interactionhand, itemstack, p_32338_, 1.0F, null
            );
        }

        this.onCrossbowAttackPerformed();
    }

    @Override
    public void setItemSlot(EquipmentSlot slotIn, ItemStack stack) {
        super.setItemSlot(slotIn, stack);
        switch (slotIn) {
            case CHEST:
                if (this.guardInventory.getItem(1).isEmpty())
                    this.guardInventory.setItem(1, this.armorItems.get(slotIn.getIndex()));
                break;
            case FEET:
                if (this.guardInventory.getItem(3).isEmpty())
                    this.guardInventory.setItem(3, this.armorItems.get(slotIn.getIndex()));
                break;
            case HEAD:
                if (this.guardInventory.getItem(0).isEmpty())
                    this.guardInventory.setItem(0, this.armorItems.get(slotIn.getIndex()));
                break;
            case LEGS:
                if (this.guardInventory.getItem(2).isEmpty())
                    this.guardInventory.setItem(2, this.armorItems.get(slotIn.getIndex()));
                break;
            case MAINHAND:
                this.guardInventory.setItem(5, this.handItems.get(slotIn.getIndex()));
                break;
            case OFFHAND:
                this.guardInventory.setItem(4, this.handItems.get(slotIn.getIndex()));
                break;
        }
    }

    @Override
    public ItemStack getProjectile(ItemStack shootable) {
        if (shootable.getItem() instanceof ProjectileWeaponItem) {
            Predicate<ItemStack> predicate = ((ProjectileWeaponItem) shootable.getItem()).getSupportedHeldProjectiles();
            ItemStack itemstack = ProjectileWeaponItem.getHeldProjectile(this, predicate);
            return itemstack.isEmpty() ? new ItemStack(Items.ARROW) : itemstack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    public int getKickTicks() {
        return this.kickTicks;
    }

    public boolean isFollowing() {
        return this.entityData.get(FOLLOWING);
    }

    public void setFollowing(boolean following) {
        this.entityData.set(FOLLOWING, following);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return (!GuardConfig.COMMON.MobBlackList.get().contains(target.getEncodeId()) && !target.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) && !this.isOwner(target) && super.canAttack(target));
    }

    @Override
    public void rideTick() {
        super.rideTick();
        if (this.getVehicle() instanceof PathfinderMob creatureentity) {
            this.yBodyRot = creatureentity.yBodyRot;
        }
    }

    @Override
    public void onCrossbowAttackPerformed() {
        this.noActionTime = 0;
    }

    @Override
    public void setTarget(LivingEntity entity) {
        if (entity != null && ((this.getTeam() != null && entity.getTeam() != null && this.getTeam().isAlliedTo(entity.getTeam())) || GuardConfig.COMMON.MobBlackList.get().contains(EntityType.getKey(entity.getType()).toString()) || entity.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) || this.isOwner(entity) || (entity instanceof TamableAnimal tamed && (tamed.getOwnerUUID() != null && tamed.getOwnerUUID().equals(this.getOwnerId())))))
            return;
        super.setTarget(entity);
    }

    public void gossip(Villager villager, long gameTime) {
        if ((gameTime < this.lastGossipTime || gameTime >= this.lastGossipTime + 1200L) && (gameTime < villager.lastGossipTime || gameTime >= villager.lastGossipTime + 1200L)) {
            this.gossips.transferFrom(villager.getGossips(), this.random, 10);
            this.lastGossipTime = gameTime;
            villager.lastGossipTime = gameTime;
        }
    }


    @Override
    protected void blockedByShield(LivingEntity entityIn) {
        if (this.isKicking()) {
            this.setKicking(false);
        }
        super.blockedByShield(this);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        boolean configValues = player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) && GuardConfig.COMMON.giveGuardStuffHOTV.get() || player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) && GuardConfig.COMMON.setGuardPatrolHotv.get() || player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) && GuardConfig.COMMON.giveGuardStuffHOTV.get() && GuardConfig.COMMON.setGuardPatrolHotv.get() || this.getPlayerReputation(player) >= GuardConfig.COMMON.reputationRequirement.get() || player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) && !GuardConfig.COMMON.giveGuardStuffHOTV.get() && !GuardConfig.COMMON.setGuardPatrolHotv.get() || this.getOwnerId() != null && this.getOwnerId().equals(player.getUUID());
        boolean inventoryRequirements = !player.isSecondaryUseActive();
        if (inventoryRequirements) {
            if (this.getTarget() != player && this.isEffectiveAi() && configValues) {
                if (player instanceof ServerPlayer) {
                    this.openGui((ServerPlayer) player);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.CONSUME;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void onReputationEventFrom(ReputationEventType reputationEventType, Entity entity) {
    }

    @Override
    public void containerChanged(Container invBasic) {
    }

    @Override
    protected void hurtArmor(DamageSource damageSource, float damage) {
        this.doHurtEquipment(damageSource, damage, EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD);
    }

    @Override
    public void thunderHit(ServerLevel p_241841_1_, LightningBolt p_241841_2_) {
        if (p_241841_1_.getDifficulty() != Difficulty.PEACEFUL && EventHooks.canLivingConvert(this, EntityType.WITCH, (timer) -> {
        })) {
            Witch witchentity = EntityType.WITCH.create(p_241841_1_);
            if (witchentity == null) return;
            witchentity.copyPosition(this);
            witchentity.finalizeSpawn(p_241841_1_, p_241841_1_.getCurrentDifficultyAt(witchentity.blockPosition()), MobSpawnType.CONVERSION, null);
            witchentity.setNoAi(this.isNoAi());
            witchentity.setCustomName(this.getCustomName());
            witchentity.setCustomNameVisible(this.isCustomNameVisible());
            witchentity.setPersistenceRequired();
            p_241841_1_.addFreshEntityWithPassengers(witchentity);
            this.discard();
        } else {
            super.thunderHit(p_241841_1_, p_241841_2_);
        }
    }

    @Override
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void setPersistentAngerTarget(UUID arg0) {
        this.persistentAngerTarget = arg0;
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.remainingPersistentAngerTime;
    }

    @Override
    public void setRemainingPersistentAngerTime(int arg0) {
        this.remainingPersistentAngerTime = arg0;
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(angerTime.sample(random));
    }

    public void openGui(ServerPlayer player) {
        this.setOwnerId(player.getUUID());
        if (player.containerMenu != player.inventoryMenu) {
            player.closeContainer();
        }
        this.interacting = true;
        player.nextContainerCounter();
        player.connection.send(new GuardOpenInventoryPacket(player.containerCounter, this.guardInventory.getContainerSize(), this.getId()));
        player.containerMenu = new GuardContainer(player.containerCounter, player.getInventory(), this.guardInventory, this);
        player.initMenu(player.containerMenu);
        NeoForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, player.containerMenu));
    }


    public boolean isEating() {
        return isConsumable(this.getUseItem()) && this.isUsingItem();
    }

    public boolean isPatrolling() {
        return this.entityData.get(PATROLLING);
    }

    public void setPatrolling(boolean patrolling) {
        this.entityData.set(PATROLLING, patrolling);
    }

    @Override
    public boolean canFireProjectileWeapon(ProjectileWeaponItem item) {
        return item instanceof BowItem || item instanceof CrossbowItem || super.canFireProjectileWeapon(item);
    }

    public static boolean isConsumable(ItemStack stack) {
        return stack.getUseAnimation() == UseAnim.EAT || stack.getUseAnimation() == UseAnim.DRINK && !(stack.getItem() instanceof SplashPotionItem);
    }

    public void tryToTeleportToOwner() {
        LivingEntity livingentity = this.getOwner();
        if (livingentity != null) {
            this.teleportToAroundBlockPos(livingentity.blockPosition());
        }
    }

    public boolean shouldTryTeleportToOwner() {
        LivingEntity livingentity = this.getOwner();
        return livingentity != null && this.distanceToSqr(this.getOwner()) >= 144.0 && GuardConfig.COMMON.guardTeleport.get() && this.getTarget() == null;
    }

    private void teleportToAroundBlockPos(BlockPos pos) {
        for (int i = 0; i < 10; i++) {
            int j = this.random.nextIntBetweenInclusive(-4, 4);
            int k = this.random.nextIntBetweenInclusive(-4, 4);
            if (Math.abs(j) >= 3 || Math.abs(k) >= 3) {
                int l = this.random.nextIntBetweenInclusive(-1, 1);
                if (this.maybeTeleportTo(pos.getX() + j, pos.getY() + l, pos.getZ() + k)) {
                    return;
                }
            }
        }
    }

    private boolean maybeTeleportTo(int x, int y, int z) {
        if (!this.canTeleportTo(new BlockPos(x, y, z))) {
            return false;
        } else {
            this.moveTo((double) x + 0.5, y, (double) z + 0.5, this.getYRot(), this.getXRot());
            this.navigation.stop();
            return true;
        }
    }

    private boolean canTeleportTo(BlockPos pos) {
        PathType pathtype = WalkNodeEvaluator.getPathTypeStatic(this, pos);
        if (pathtype != PathType.WALKABLE) {
            return false;
        } else {
            BlockState blockstate = this.level().getBlockState(pos.below());
            if (blockstate.getBlock() instanceof LeavesBlock) {
                return false;
            } else {
                BlockPos blockpos = pos.subtract(this.blockPosition());
                return this.level().noCollision(this, this.getBoundingBox().move(blockpos));
            }
        }
    }

    public static List<ItemStack> getItemsFromLootTable(LivingEntity entity) {
        LootTable loot = entity.level().getServer().reloadableRegistries().getLootTable(getLootTableFromData());
        LootParams.Builder lootcontext$builder = (new LootParams.Builder((ServerLevel) entity.level()).withParameter(LootContextParams.THIS_ENTITY, entity));
        return loot.getRandomItems(lootcontext$builder.create(GuardLootTables.SLOT));
    }

    public static ResourceKey<LootTable> getLootTableFromData() {

        ResourceLocation lootTable = ResourceLocation.fromNamespaceAndPath(GuardVillagers.MODID, "entities/guard_armor");
        return ResourceKey.create(Registries.LOOT_TABLE, lootTable);
    }

    public void setVariant(String variant) {
        this.entityData.set(GUARD_VARIANT, variant);
    }

    public String getVariant() {
        String variant = this.entityData.get(GUARD_VARIANT);
        return !variant.isEmpty() ? variant : "plains";
    }

    public static class DefendVillageGuardGoal extends TargetGoal {
        private final Guard guard;
        private LivingEntity villageAggressorTarget;

        public DefendVillageGuardGoal(Guard guardIn) {
            super(guardIn, true, true);
            this.guard = guardIn;
            this.setFlags(EnumSet.of(Flag.TARGET, Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            AABB axisalignedbb = this.guard.getBoundingBox().inflate(10.0D, 8.0D, 10.0D);
            List<Villager> list = guard.level().getEntitiesOfClass(Villager.class, axisalignedbb);
            List<Player> list1 = guard.level().getEntitiesOfClass(Player.class, axisalignedbb);
            for (Villager villager : list) {
                for (Player player : list1) {
                    int i = villager.getPlayerReputation(player);
                    if (i <= GuardConfig.COMMON.reputationRequirementToBeAttacked.get()) {
                        this.villageAggressorTarget = player;
                        if (villageAggressorTarget.getTeam() != null && guard.getTeam() != null && guard.getTeam().isAlliedTo(villageAggressorTarget.getTeam()))
                            return false;
                    }
                }
            }
            return villageAggressorTarget != null && !villageAggressorTarget.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) && !this.villageAggressorTarget.isSpectator() && !((Player) this.villageAggressorTarget).isCreative();
        }

        @Override
        public void start() {
            this.guard.setTarget(this.villageAggressorTarget);
            super.start();
        }
    }

    public static class FollowHeroGoal extends Goal {
        private final Guard guard;
        private LivingEntity owner;
        private final double speedModifier;
        private final PathNavigation navigation;
        private int timeToRecalcPath;
        private final float stopDistance;
        private final float startDistance;
        private float oldWaterCost;

        public FollowHeroGoal(Guard guard, double speedModifier, float startDistance, float stopDistance) {
            this.guard = guard;
            this.speedModifier = speedModifier;
            this.navigation = guard.getNavigation();
            this.startDistance = startDistance;
            this.stopDistance = stopDistance;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity livingentity = this.guard.getOwner();
            if (livingentity == null) {
                return false;
            } else if (this.guard.distanceToSqr(livingentity) < (double) (this.startDistance * this.startDistance)) {
                return false;
            } else {
                this.owner = livingentity;
                return this.guard.isFollowing();
            }
        }

        @Override
        public boolean canContinueToUse() {
            if (!this.navigation.isDone()) {
                return this.guard.distanceToSqr(this.owner) >= (double) (this.stopDistance * this.stopDistance) && this.guard.isFollowing();
            } else {
                return false;
            }
        }

        @Override
        public void start() {
            this.timeToRecalcPath = 0;
            this.oldWaterCost = this.guard.getPathfindingMalus(PathType.WATER);
            this.guard.setPathfindingMalus(PathType.WATER, 0.0F);
        }

        @Override
        public void stop() {
            this.owner = null;
            this.navigation.stop();
            this.guard.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
        }

        @Override
        public void tick() {
            boolean shouldTryTeleportToOwner = this.guard.shouldTryTeleportToOwner();
            if (!shouldTryTeleportToOwner) {
                this.guard.getLookControl().setLookAt(this.owner, 10.0F, (float) this.guard.getMaxHeadXRot());
            }

            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = this.adjustedTickDelay(10);
                if (shouldTryTeleportToOwner) {
                    this.guard.tryToTeleportToOwner();
                } else {
                    this.navigation.moveTo(this.owner, this.speedModifier);
                }
            }
        }
    }

    public static class GuardMeleeGoal extends MeleeAttackGoal {
        private static final double DEFAULT_ATTACK_REACH = Math.sqrt(2.04F) - (double) 0.6F;
        public final Guard guard;

        public GuardMeleeGoal(Guard guard, double speedIn, boolean useLongMemory) {
            super(guard, speedIn, useLongMemory);
            this.guard = guard;
        }

        @Override
        public boolean canUse() {
            return (!(mob.getMainHandItem().getItem() instanceof CrossbowItem) || !(mob.getMainHandItem().getItem() instanceof BowItem)) && this.guard.getTarget() != null && !this.guard.isEating() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && this.guard.getTarget() != null;
        }

        @Override
        public void tick() {
            LivingEntity target = guard.getTarget();
            if (target != null) {
                if (target.distanceTo(guard) <= 3.0D) {
                    guard.getMoveControl().strafe(-2.0F, 0.0F);
                    guard.lookAt(target, 30.0F, 30.0F);
                }
                if (path != null && target.distanceTo(guard) <= 2.5D) guard.getNavigation().stop();
                super.tick();
            }
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity enemy) {
            if (canPerformAttack(enemy)) {
                this.resetAttackCooldown();
                this.guard.stopUsingItem();
                if (guard.shieldCoolDown == 0) this.guard.shieldCoolDown = 8;
                this.guard.swing(InteractionHand.MAIN_HAND);
                this.guard.doHurtTarget(enemy);
            }
        }

        @Override
        protected boolean canPerformAttack(LivingEntity mob) {
            return this.isTimeToAttack() && this.mobHitBox(this.mob).inflate(0.65).intersects(this.mobHitBox(mob)) && this.mob.getSensing().hasLineOfSight(mob);
        }

        protected AABB mobHitBox(LivingEntity mob) {
            Entity entity = mob.getVehicle();
            AABB aabb;
            if (entity != null) {
                AABB aabb1 = entity.getBoundingBox();
                AABB aabb2 = mob.getBoundingBox();
                aabb = new AABB(Math.min(aabb2.minX, aabb1.minX), aabb2.minY, Math.min(aabb2.minZ, aabb1.minZ), Math.max(aabb2.maxX, aabb1.maxX), aabb2.maxY, Math.max(aabb2.maxZ, aabb1.maxZ));
            } else {
                aabb = mob.getBoundingBox();
            }

            return aabb.inflate(DEFAULT_ATTACK_REACH, 0.0D, DEFAULT_ATTACK_REACH);
        }
    }

    public static class GuardBowAttack extends RangedBowAttackGoal<Guard> {
        protected Guard guard;

        public GuardBowAttack(Guard mob, double speedModifier, int attackIntervalMin, float attackRadius) {
            super(mob, speedModifier, attackIntervalMin, attackRadius);
            this.guard = mob;
        }

        @Override
        public boolean canUse() {
            return guard.getTarget() != null && this.isBowInMainhand() && !guard.isEating() && !guard.isBlocking();
        }

        protected boolean isBowInMainhand() {
            return guard.getMainHandItem().getItem() instanceof BowItem;
        }

        @Override
        public void tick() {
            super.tick();
            if (guard.isPatrolling()) {
                guard.getNavigation().stop();
                guard.getMoveControl().strafe(0.0F, 0.0F);
            }
            if (RangedCrossbowAttackPassiveGoal.friendlyInLineOfSight(guard)) {
                this.guard.stopUsingItem();
            }
        }

        @Override
        public boolean canContinueToUse() {
            return (this.canUse() || !guard.getNavigation().isDone()) && this.isBowInMainhand();
        }

    }

    public static class GuardInteractDoorGoal extends OpenDoorGoal {
        private final Guard guard;

        public GuardInteractDoorGoal(Guard pMob, boolean pCloseDoor) {
            super(pMob, pCloseDoor);
            this.guard = pMob;
        }

        @Override
        public boolean canUse() {
            return super.canUse();
        }

        @Override
        public void start() {
            if (areOtherMobsComingThroughDoor(guard)) {
                super.start();
                guard.swing(InteractionHand.MAIN_HAND);
            }
        }

        private boolean areOtherMobsComingThroughDoor(Guard pEntity) {
            List<? extends PathfinderMob> nearbyEntityList = pEntity.level().getEntitiesOfClass(PathfinderMob.class,
                    pEntity.getBoundingBox().inflate(4.0D));
            if (!nearbyEntityList.isEmpty()) {
                for (PathfinderMob mob : nearbyEntityList) {
                    if (mob.blockPosition().closerToCenterThan(pEntity.position(), 2.0D))
                        return isMobComingThroughDoor(mob);
                }
            }
            return false;
        }

        private boolean isMobComingThroughDoor(PathfinderMob pEntity) {
            if (pEntity.getNavigation() == null) {
                return false;
            } else {
                Path path = pEntity.getNavigation().getPath();
                if (path == null || path.isDone()) {
                    return false;
                } else {
                    Node node = path.getPreviousNode();
                    if (node == null) {
                        return false;
                    } else {
                        Node node1 = path.getNextNode();
                        return pEntity.blockPosition().equals(node.asBlockPos()) || pEntity.blockPosition().equals(node1.asBlockPos());
                    }
                }
            }
        }
    }

    public static class GuardLookAtAndStopMovingWhenBeingTheInteractionTarget extends Goal {
        private final Guard guard;
        private Villager villager;

        public GuardLookAtAndStopMovingWhenBeingTheInteractionTarget(Guard guard) {
            this.guard = guard;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            List<Villager> list = this.guard.level().getEntitiesOfClass(Villager.class, guard.getBoundingBox().inflate(10.0D));
            if (!list.isEmpty()) {
                for (Villager villager : list) {
                    if (villager.getBrain().hasMemoryValue(MemoryModuleType.INTERACTION_TARGET) && villager.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get().is(guard)) {
                        this.villager = villager;
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            super.tick();
            guard.getNavigation().stop();
            guard.lookAt(villager, 30.0F, 30.0F);
            guard.getLookControl().setLookAt(villager);
        }
    }

    public static class GuardEatFoodGoal extends Goal {
        public final Guard guard;

        public GuardEatFoodGoal(Guard guard) {
            this.guard = guard;
        }

        @Override
        public boolean canUse() {
            return guard.getHealth() < guard.getMaxHealth() && isConsumable(guard.getOffhandItem()) && guard.isEating() || guard.getHealth() < guard.getMaxHealth() && isConsumable(guard.getOffhandItem()) && guard.getTarget() == null && !guard.isAggressive();
        }

        @Override
        public boolean canContinueToUse() {
            List<LivingEntity> list = this.guard.level().getEntitiesOfClass(LivingEntity.class, this.guard.getBoundingBox().inflate(5.0D, 3.0D, 5.0D));
            if (!list.isEmpty()) {
                for (LivingEntity mob : list) {
                    if (mob != null) {
                        if (mob instanceof Mob && ((Mob) mob).getTarget() instanceof Guard) {
                            return false;
                        }
                    }
                }
            }
            return guard.isUsingItem() && guard.getTarget() == null && guard.getHealth() < guard.getMaxHealth() || guard.getTarget() != null && guard.getHealth() < guard.getMaxHealth() / 2 + 2 && guard.isEating();
            // Guards will only keep eating until they're up to full health if they're not aggroed, otherwise they will just heal back above half health and then join back the fight.
        }

        @Override
        public void start() {
            guard.startUsingItem(InteractionHand.OFF_HAND);
        }
    }

    public static class GuardRunToEatGoal extends RandomStrollGoal {
        private final Guard guard;
        private int walkTimer;

        public GuardRunToEatGoal(Guard guard) {
            super(guard, 1.0D);
            this.guard = guard;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.TARGET, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return guard.getHealth() < (guard.getMaxHealth() / 2) && isConsumable(guard.getOffhandItem()) && !guard.isEating() && guard.getTarget() != null && this.getPosition() != null;
        }

        @Override
        public void start() {
            super.start();
            this.guard.setTarget(null);
            if (this.walkTimer <= 0) {
                this.walkTimer = 20;
            }
        }

        @Override
        public void tick() {
            --walkTimer;
            List<LivingEntity> list = this.guard.level().getEntitiesOfClass(LivingEntity.class, this.guard.getBoundingBox().inflate(5.0D, 3.0D, 5.0D));
            if (!list.isEmpty()) {
                for (LivingEntity mob : list) {
                    if (mob != null) {
                        if (mob.getLastHurtMob() instanceof Guard || mob instanceof Mob && ((Mob) mob).getTarget() instanceof Guard) {
                            if (walkTimer < 20)
                                this.walkTimer += 5;
                        }
                    }
                }
            }
        }

        @Override
        protected Vec3 getPosition() {
            List<LivingEntity> list = this.guard.level().getEntitiesOfClass(LivingEntity.class, this.guard.getBoundingBox().inflate(5.0D, 3.0D, 5.0D));
            if (!list.isEmpty()) {
                for (LivingEntity mob : list) {
                    if (mob != null) {
                        if (mob.getLastHurtMob() instanceof Guard || mob instanceof Mob && ((Mob) mob).getTarget() instanceof Guard) {
                            return DefaultRandomPos.getPosAway(guard, 16, 7, mob.position());
                        }
                    }
                }
            }
            return super.getPosition();
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && this.walkTimer > 0 && !guard.isEating();
        }

        @Override
        public void stop() {
            super.stop();
            this.guard.startUsingItem(InteractionHand.OFF_HAND);
            this.guard.getNavigation().stop();
        }
    }

    public static class FollowShieldGuards extends Goal {
        private static final TargetingConditions NEARBY_GUARDS = TargetingConditions.forNonCombat().range(8.0D)
                .ignoreLineOfSight();
        private final Guard taskOwner;
        private Guard guardtofollow;
        private double x;
        private double y;
        private double z;

        public FollowShieldGuards(Guard taskOwnerIn) {
            this.taskOwner = taskOwnerIn;
        }

        @Override
        public boolean canUse() {
            List<? extends Guard> list = this.taskOwner.level().getEntitiesOfClass(this.taskOwner.getClass(),
                    this.taskOwner.getBoundingBox().inflate(8.0D, 8.0D, 8.0D));
            if (!list.isEmpty()) {
                for (Guard guard : list) {
                    if (!guard.isInvisible() && guard.getOffhandItem().canPerformAction(ItemAbilities.SHIELD_BLOCK) && guard.isBlocking()
                            && this.taskOwner.level()
                            .getNearbyEntities(Guard.class, NEARBY_GUARDS.range(3.0D), guard,
                                    this.taskOwner.getBoundingBox().inflate(5.0D))
                            .size() < 5) {
                        this.guardtofollow = guard;
                        Vec3 vec3d = this.getPosition();
                        if (vec3d == null) {
                            return false;
                        } else {
                            this.x = vec3d.x;
                            this.y = vec3d.y;
                            this.z = vec3d.z;
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        @Nullable
        protected Vec3 getPosition() {
            return DefaultRandomPos.getPosTowards(this.taskOwner, 16, 7, this.guardtofollow.position(), (float) Math.PI / 2F);
        }

        @Override
        public boolean canContinueToUse() {
            return !this.taskOwner.getNavigation().isDone() && !this.taskOwner.isVehicle();
        }

        @Override
        public void stop() {
            this.taskOwner.getNavigation().stop();
            super.stop();
        }

        @Override
        public void start() {
            this.taskOwner.getNavigation().moveTo(x, y, z, 0.4D);
        }
    }

    public static class RangedCrossbowAttackPassiveGoal<T extends Guard> extends Goal {
        public static final UniformInt PATHFINDING_DELAY_RANGE = TimeUtil.rangeOfSeconds(1, 2);
        private final T mob;
        private final double speedModifier;
        private final float attackRadiusSqr;
        protected double wantedX;
        protected double wantedY;
        protected double wantedZ;
        private CrossbowState crossbowState = CrossbowState.UNCHARGED;
        private int seeTime;
        private int attackDelay;
        private int updatePathDelay;

        public RangedCrossbowAttackPassiveGoal(T pMob, double pSpeedModifier, float pAttackRadius) {
            this.mob = pMob;
            this.speedModifier = pSpeedModifier;
            this.attackRadiusSqr = pAttackRadius * pAttackRadius;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return this.isValidTarget() && this.isHoldingCrossbow();
        }

        private boolean isHoldingCrossbow() {
            return this.mob.isHolding(is -> is.getItem() instanceof CrossbowItem);
        }

        @Override
        public boolean canContinueToUse() {
            return this.isValidTarget() && (this.canUse() || !this.mob.getNavigation().isDone()) && this.isHoldingCrossbow();
        }

        private boolean isValidTarget() {
            return this.mob.getTarget() != null && this.mob.getTarget().isAlive();
        }

        @Override
        public void stop() {
            super.stop();
            this.mob.setAggressive(false);
            this.mob.setTarget(null);
            this.seeTime = 0;
            if (this.mob.isUsingItem()) {
                this.mob.stopUsingItem();
                this.mob.setChargingCrossbow(false);
            }
            this.mob.setPose(Pose.STANDING);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            this.mob.setAggressive(true);
        }

        @Override
        public void tick() {
            LivingEntity livingentity = this.mob.getTarget();
            if (livingentity != null) {
                boolean canSee = this.mob.getSensing().hasLineOfSight(livingentity);
                boolean hasSeenEntityRecently = this.seeTime > 0;
                if (canSee != hasSeenEntityRecently) {
                    this.seeTime = 0;
                }
                if (canSee) {
                    ++this.seeTime;
                } else {
                    --this.seeTime;
                }
                double d0 = this.mob.distanceToSqr(livingentity);
                double d1 = livingentity.distanceTo(this.mob);
                if (d1 <= 4.0D) {
                    this.mob.getMoveControl().strafe(this.mob.isUsingItem() ? -0.5F : -3.0F, 0.0F);
                    this.mob.lookAt(livingentity, 30.0F, 30.0F);
                }
                if (this.mob.getRandom().nextInt(50) == 0) {
                    if (this.mob.hasPose(Pose.STANDING))
                        this.mob.setPose(Pose.CROUCHING);
                    else
                        this.mob.setPose(Pose.STANDING);
                }
                boolean canSee2 = (d0 > (double) this.attackRadiusSqr || this.seeTime < 5) && this.attackDelay == 0;
                if (canSee2) {
                    --this.updatePathDelay;
                    if (this.updatePathDelay <= 0 && !this.mob.isPatrolling()) {
                        this.mob.getNavigation().moveTo(livingentity, this.canRun() ? this.speedModifier : this.speedModifier * 0.5D);
                        this.updatePathDelay = PATHFINDING_DELAY_RANGE.sample(this.mob.getRandom());
                    }
                } else {
                    this.updatePathDelay = 0;
                    this.mob.getNavigation().stop();
                }
                this.mob.lookAt(livingentity, 30.0F, 30.0F);
                this.mob.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
                if (friendlyInLineOfSight(this.mob))
                    this.crossbowState = CrossbowState.FIND_NEW_POSITION;
                if (this.crossbowState == CrossbowState.FIND_NEW_POSITION) {
                    this.mob.stopUsingItem();
                    this.mob.setChargingCrossbow(false);
                    if (this.findPosition())
                        this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.mob.isCrouching() ? 0.5D : 0.9D);
                    this.crossbowState = CrossbowState.UNCHARGED;
                } else if (this.crossbowState == CrossbowState.UNCHARGED) {
                    if (!canSee2) {
                        this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof CrossbowItem));
                        this.crossbowState = CrossbowState.CHARGING;
                        this.mob.setChargingCrossbow(true);
                    }
                } else if (this.crossbowState == CrossbowState.CHARGING) {
                    if (!this.mob.isUsingItem()) {
                        this.crossbowState = CrossbowState.UNCHARGED;
                    }
                    int i = this.mob.getTicksUsingItem();
                    ItemStack itemstack = this.mob.getUseItem();
                    if (i >= CrossbowItem.getChargeDuration(itemstack, this.mob) || CrossbowItem.isCharged(itemstack)) {
                        this.mob.releaseUsingItem();
                        this.crossbowState = CrossbowState.CHARGED;
                        this.attackDelay = 10 + this.mob.getRandom().nextInt(5);
                        this.mob.setChargingCrossbow(false);
                    }
                } else if (this.crossbowState == CrossbowState.CHARGED) {
                    --this.attackDelay;
                    if (this.attackDelay == 0) {
                        this.crossbowState = CrossbowState.READY_TO_ATTACK;
                    }
                } else if (this.crossbowState == CrossbowState.READY_TO_ATTACK && canSee) {
                    this.mob.performRangedAttack(livingentity, 1.0F);
                    this.crossbowState = CrossbowState.UNCHARGED;
                }
            }

        }

        public static boolean friendlyInLineOfSight(Mob mob) {
            Vec3 lookAngle = mob.getViewVector(1.0F);
            AABB aabb = mob.getBoundingBox().expandTowards(lookAngle.scale(6.0D)).inflate(1.0, 1.0, 1.0);
            List<Entity> list = mob.level().getEntities(mob, aabb);
            for (Entity guard : list) {
                if (guard != mob.getTarget()) {
                    boolean isVillager = ((Guard) mob).getOwner() == guard || guard.getType() == EntityType.VILLAGER || guard.getType() == GuardEntityType.GUARD.get() || guard.getType() == EntityType.IRON_GOLEM;
                    if (isVillager) {
                        Vec3 vector3d = mob.getLookAngle();
                        Vec3 vector3d1 = guard.position().vectorTo(mob.position()).normalize();
                        vector3d1 = new Vec3(vector3d1.x, vector3d1.y, vector3d1.z);
                        if (vector3d1.dot(vector3d) < 1.0D && mob.hasLineOfSight(guard))
                            return GuardConfig.COMMON.FriendlyFire.get();
                    }
                }
            }
            return false;
        }

        public boolean findPosition() {
            Vec3 vector3d = this.getPosition();
            if (vector3d == null) {
                return false;
            } else {
                this.wantedX = vector3d.x;
                this.wantedY = vector3d.y;
                this.wantedZ = vector3d.z;
                return true;
            }
        }

        @Nullable
        protected Vec3 getPosition() {
            if (this.isValidTarget() && this.mob.getTarget().position() != null)
                return DefaultRandomPos.getPosAway(this.mob, 16, 7, this.mob.getTarget().position());
            else
                return DefaultRandomPos.getPos(this.mob, 16, 7);
        }

        private boolean canRun() {
            return this.crossbowState == CrossbowState.UNCHARGED;
        }

        public enum CrossbowState {
            UNCHARGED,
            CHARGING,
            CHARGED,
            READY_TO_ATTACK,
            FIND_NEW_POSITION
        }
    }

    public static class KickGoal extends Goal {
        public final Guard guard;

        public KickGoal(Guard guard) {
            this.guard = guard;
        }

        @Override
        public boolean canUse() {
            return guard.getTarget() != null && guard.getTarget().distanceTo(guard) <= 2.5D && guard.getMainHandItem().getItem().useOnRelease(guard.getMainHandItem()) && !guard.isBlocking() && guard.kickCoolDown == 0;
        }

        @Override
        public void start() {
            guard.setKicking(true);
            if (guard.kickTicks <= 0) {
                guard.kickTicks = 10;
            }
            guard.doHurtTarget(guard.getTarget());
        }

        @Override
        public void stop() {
            guard.setKicking(false);
            guard.kickCoolDown = 50;
        }
    }

    public static class RaiseShieldGoal extends Goal {
        public final Guard guard;

        public RaiseShieldGoal(Guard guard) {
            this.guard = guard;
        }

        @Override
        public boolean canUse() {
            return !CrossbowItem.isCharged(guard.getMainHandItem()) && (guard.getOffhandItem().getItem().canPerformAction(guard.getOffhandItem(), ItemAbilities.SHIELD_BLOCK) && raiseShield() && guard.shieldCoolDown == 0
                    && !guard.getOffhandItem().getItem().equals(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("piglinproliferation", "buckler"))));
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public void start() {
            if (guard.getOffhandItem().getItem().canPerformAction(guard.getOffhandItem(), ItemAbilities.SHIELD_BLOCK))
                guard.startUsingItem(InteractionHand.OFF_HAND);
        }

        @Override
        public void stop() {
            if (!GuardConfig.COMMON.GuardRaiseShield.get())
                guard.stopUsingItem();
        }

        protected boolean raiseShield() {
            LivingEntity target = guard.getTarget();
            if (target != null && guard.shieldCoolDown == 0) {
                boolean ranged = guard.getMainHandItem().getItem() instanceof CrossbowItem || guard.getMainHandItem().getItem() instanceof BowItem;
                return guard.distanceTo(target) <= 4.0D || target instanceof Creeper || target instanceof RangedAttackMob && target.distanceTo(guard) >= 5.0D && !ranged || target instanceof Ravager || GuardConfig.COMMON.GuardRaiseShield.get();
            }
            return false;
        }
    }

    public static class RunToClericGoal extends Goal {
        public final Guard guard;
        public Villager cleric;

        public RunToClericGoal(Guard guard) {
            this.guard = guard;
        }

        @Override
        public boolean canUse() {
            List<Villager> list = this.guard.level().getEntitiesOfClass(Villager.class, this.guard.getBoundingBox().inflate(10.0D, 3.0D, 10.0D));
            if (!list.isEmpty()) {
                for (Villager mob : list) {
                    if (mob != null) {
                        if (mob.getVillagerData().getProfession() == VillagerProfession.CLERIC && guard.getHealth() < guard.getMaxHealth() && guard.getTarget() == null && !guard.hasEffect(MobEffects.REGENERATION) && !mob.isSleeping()) {
                            this.cleric = mob;
                            return GuardConfig.COMMON.ClericHealing.get();
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public void tick() {
            guard.lookAt(cleric, 30.0F, 30.0F);
            guard.getLookControl().setLookAt(cleric, 30.0F, 30.0F);
            if (guard.distanceTo(cleric) >= 6.0D) {
                guard.getNavigation().moveTo(cleric, 0.5D);
            } else {
                guard.getMoveControl().strafe(-1.0F, 0.0F);
                guard.getNavigation().stop();
            }
        }
    }

    public static class HeroHurtByTargetGoal extends TargetGoal {
        private final Guard guard;
        private LivingEntity attacker;
        private int timestamp;

        public HeroHurtByTargetGoal(Guard guard) {
            super(guard, false);
            this.guard = guard;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            LivingEntity livingentity = this.guard.getOwner();
            if (livingentity == null) {
                return false;
            } else {
                this.attacker = livingentity.getLastHurtByMob();
                int i = livingentity.getLastHurtByMobTimestamp();
                return i != this.timestamp && this.canAttack(this.attacker, TargetingConditions.DEFAULT);
            }
        }

        @Override
        protected boolean canAttack(@Nullable LivingEntity potentialTarget, TargetingConditions targetPredicate) {
            return super.canAttack(potentialTarget, targetPredicate) && !(potentialTarget instanceof IronGolem) && !(potentialTarget instanceof Guard);
        }

        @Override
        public void start() {
            this.mob.setTarget(this.attacker);
            LivingEntity livingentity = this.guard.getOwner();
            if (livingentity != null) {
                this.timestamp = livingentity.getLastHurtByMobTimestamp();
            }
            super.start();
        }
    }

    public static class HeroHurtTargetGoal extends TargetGoal {
        private final Guard guard;
        private LivingEntity attacker;
        private int timestamp;

        public HeroHurtTargetGoal(Guard theEntityTameableIn) {
            super(theEntityTameableIn, false);
            this.guard = theEntityTameableIn;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        public boolean canUse() {
            LivingEntity livingentity = this.guard.getOwner();
            if (livingentity == null) {
                return false;
            } else {
                this.attacker = livingentity.getLastHurtMob();
                int i = livingentity.getLastHurtMobTimestamp();
                return i != this.timestamp && this.canAttack(this.attacker, TargetingConditions.DEFAULT);
            }
        }

        @Override
        protected boolean canAttack(@Nullable LivingEntity potentialTarget, TargetingConditions targetPredicate) {
            return super.canAttack(potentialTarget, targetPredicate) && !(potentialTarget instanceof AbstractVillager) && !(potentialTarget instanceof Guard);
        }

        @Override
        public void start() {
            this.mob.setTarget(this.attacker);
            LivingEntity livingentity = this.guard.getOwner();
            if (livingentity != null) {
                this.timestamp = livingentity.getLastHurtMobTimestamp();
            }
            super.start();
        }
    }

    public static class WalkBackToCheckPointGoal extends Goal {
        private final Guard guard;
        private final double speed;

        public WalkBackToCheckPointGoal(Guard guard, double speedIn) {
            this.guard = guard;
            this.speed = speedIn;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return guard.getPatrolPos() != null && this.guard.blockPosition() != this.guard.getPatrolPos() && !guard.isFollowing() && guard.isPatrolling();
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse() && this.guard.getNavigation().isInProgress();
        }

        @Override
        public void start() {
            BlockPos blockpos = this.guard.getPatrolPos();
            if (blockpos != null) {
                Vec3 vector3d = Vec3.atCenterOf(blockpos);
                this.guard.getNavigation().moveTo(vector3d.x, vector3d.y, vector3d.z, this.speed);
            }
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }
    }
}