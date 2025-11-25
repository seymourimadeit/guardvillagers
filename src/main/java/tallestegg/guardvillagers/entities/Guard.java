package tallestegg.guardvillagers.entities;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.damagesource.DamageTypes;
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
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.PacketDistributor;
import tallestegg.guardvillagers.GuardLootTables;
import tallestegg.guardvillagers.GuardPacketHandler;
import tallestegg.guardvillagers.GuardVillagers;
import tallestegg.guardvillagers.ModCompat;
import tallestegg.guardvillagers.client.GuardSounds;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.entities.ai.goals.*;
import tallestegg.guardvillagers.networking.GuardOpenInventoryPacket;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class Guard extends PathfinderMob implements CrossbowAttackMob, RangedAttackMob, NeutralMob, ContainerListener, ReputationEventHandler {
    protected static final EntityDataAccessor<Optional<UUID>> OWNER_UNIQUE_ID = SynchedEntityData.defineId(Guard.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final UUID MODIFIER_UUID = UUID.fromString("5CD17E52-A79A-43D3-A529-90FDE04B181E");
    private static final AttributeModifier USE_ITEM_SPEED_PENALTY = new AttributeModifier(MODIFIER_UUID, "Use item speed penalty", -0.25D, AttributeModifier.Operation.ADDITION);
    private static final EntityDataAccessor<Optional<BlockPos>> GUARD_POS = SynchedEntityData.defineId(Guard.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Boolean> PATROLLING = SynchedEntityData.defineId(Guard.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> GUARD_VARIANT = SynchedEntityData.defineId(Guard.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> RUNNING_TO_EAT = SynchedEntityData.defineId(Guard.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_CHARGING_STATE = SynchedEntityData.defineId(Guard.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> KICKING = SynchedEntityData.defineId(Guard.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FOLLOWING = SynchedEntityData.defineId(Guard.class, EntityDataSerializers.BOOLEAN);
    private static final Map<Pose, EntityDimensions> SIZE_BY_POSE = ImmutableMap.<Pose, EntityDimensions>builder().put(Pose.STANDING, EntityDimensions.scalable(0.6F, 1.95F)).put(Pose.SLEEPING, SLEEPING_DIMENSIONS).put(Pose.FALL_FLYING, EntityDimensions.scalable(0.6F, 0.6F)).put(Pose.SWIMMING, EntityDimensions.scalable(0.6F, 0.6F)).put(Pose.SPIN_ATTACK, EntityDimensions.scalable(0.6F, 0.6F)).put(Pose.CROUCHING, EntityDimensions.scalable(0.6F, 1.75F)).put(Pose.DYING, EntityDimensions.fixed(0.2F, 0.2F)).build();
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
    private net.minecraftforge.common.util.LazyOptional<?> itemHandler;

    public Guard(EntityType<? extends Guard> type, Level world) {
        super(type, world);
        this.guardInventory.addListener(this);
        this.itemHandler = net.minecraftforge.common.util.LazyOptional.of(() -> new net.minecraftforge.items.wrapper.InvWrapper(this.guardInventory));
        this.setPersistenceRequired();
        if (GuardConfig.GuardsOpenDoors) ((GroundPathNavigation) this.getNavigation()).setCanOpenDoors(true);
    }

    public static int slotToInventoryIndex(EquipmentSlot slot) {
        return switch (slot) {
            case CHEST -> 1;
            case FEET -> 3;
            case LEGS -> 2;
            default -> 0;
        };
    }

    public static String getVariantFromBiome(LevelAccessor world, BlockPos pos) {
        VillagerType type = VillagerType.byBiome(world.getBiome(pos));
        return GuardVillagers.removeModIdFromVillagerType(type.toString());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, GuardConfig.COMMON.healthModifier.get()).add(Attributes.MOVEMENT_SPEED, GuardConfig.COMMON.speedModifier.get()).add(Attributes.ATTACK_DAMAGE, 1.0D).add(Attributes.FOLLOW_RANGE, GuardConfig.COMMON.followRangeModifier.get());
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
        this.setPersistenceRequired();
        this.setGuardVariant(Guard.getVariantFromBiome(level(), this.blockPosition()));
        RandomSource randomsource = worldIn.getRandom();
        this.populateDefaultEquipmentSlots(randomsource, difficultyIn);
        return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
    }

    @Override
    protected void doPush(Entity entityIn) {
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

    @Nullable
    public void setPatrolPos(BlockPos position) {
        this.entityData.set(GUARD_POS, Optional.ofNullable(position));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return GuardSounds.GUARD_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return GuardSounds.GUARD_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return GuardSounds.GUARD_DEATH.get();
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHitIn) {
        for (int i = 0; i < this.guardInventory.getContainerSize(); ++i) {
            ItemStack itemstack = this.guardInventory.getItem(i);
            RandomSource randomsource = level().getRandom();
            if (!itemstack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemstack) && randomsource.nextFloat() < GuardConfig.COMMON.chanceToDropEquipment.get().floatValue())
                this.spawnAtLocation(itemstack);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
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
        if (compound.contains("Variant")) {
            this.setGuardVariant(GuardVillagers.removeModIdFromVillagerType(compound.getString("Variant")));
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
        if (compound.contains("PatrolPosX")) {
            int x = compound.getInt("PatrolPosX");
            int y = compound.getInt("PatrolPosY");
            int z = compound.getInt("PatrolPosZ");
            this.entityData.set(GUARD_POS, Optional.ofNullable(new BlockPos(x, y, z)));
        }
        ListTag listtag = compound.getList("Gossips", 10);
        this.gossips.update(new Dynamic<>(NbtOps.INSTANCE, listtag));
        ListTag listnbt = compound.getList("Inventory", 9);
        for (int i = 0; i < listnbt.size(); ++i) {
            CompoundTag compoundnbt = listnbt.getCompound(i);
            int j = compoundnbt.getByte("Slot") & 255;
            this.guardInventory.setItem(j, ItemStack.of(compoundnbt));
        }
        if (compound.contains("ArmorItems", 9)) {
            ListTag armorItems = compound.getList("ArmorItems", 10);
            for (int i = 0; i < this.armorItems.size(); ++i) {
                int index = Guard.slotToInventoryIndex(Mob.getEquipmentSlotForItem(ItemStack.of(armorItems.getCompound(i))));
                this.guardInventory.setItem(index, ItemStack.of(armorItems.getCompound(i)));
            }
        }
        if (compound.contains("HandItems", 9)) {
            ListTag handItems = compound.getList("HandItems", 10);
            for (int i = 0; i < this.handItems.size(); ++i) {
                int handSlot = i == 0 ? 5 : 4;
                this.guardInventory.setItem(handSlot, ItemStack.of(handItems.getCompound(i)));
            }
        }
        if (!level().isClientSide) this.readPersistentAngerSaveData(level(), compound);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("Variant", this.getGuardVariant());
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
                itemstack.save(compoundnbt);
                listnbt.add(compoundnbt);
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
                    ItemStack itemstack = net.minecraftforge.event.ForgeEventFactory.onItemUseFinish(this, copy, getUseItemRemainingTicks(), this.useItem.finishUsingItem(level(), this));
                    if (itemstack != this.useItem) {
                        this.setItemInHand(interactionhand, itemstack);
                    }
                    if (!this.useItem.isEdible()) this.useItem.shrink(1);
                    this.stopUsingItem();
                }

            }
        }
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot pSlot) {
        switch (pSlot) {
            case HEAD:
                return this.guardInventory.getItem(0);
            case CHEST:
                return this.guardInventory.getItem(1);
            case LEGS:
                return this.guardInventory.getItem(2);
            case FEET:
                return this.guardInventory.getItem(3);
            case OFFHAND:
                return this.guardInventory.getItem(4);
            case MAINHAND:
                return this.guardInventory.getItem(5);
        }
        return ItemStack.EMPTY;
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
            boolean heroOfTheVillage = uuid != null && level().getPlayerByUUID(uuid) != null && level().getPlayerByUUID(uuid).hasEffect(MobEffects.HERO_OF_THE_VILLAGE);
            return uuid == null || (level().getPlayerByUUID(uuid) != null && (!heroOfTheVillage && GuardConfig.followHero) || !GuardConfig.followHero && level().getPlayerByUUID(uuid) == null) ? null : level().getPlayerByUUID(uuid);
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
    public boolean doHurtTarget(Entity entityIn) {
        if (this.isKicking()) {
            ((LivingEntity) entityIn).knockback(1.0F, Mth.sin(this.getYRot() * ((float) Math.PI / 180F)), (-Mth.cos(this.getYRot() * ((float) Math.PI / 180F))));
            this.kickTicks = 10;
            level().broadcastEntityEvent(this, (byte) 4);
            this.lookAt(entityIn, 90.0F, 90.0F);
        }
        ItemStack hand = this.getMainHandItem();
        this.damageGuardItem(1, EquipmentSlot.MAINHAND, hand);
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
    public void die(DamageSource source) {
        if ((level().getDifficulty() == Difficulty.NORMAL || level().getDifficulty() == Difficulty.HARD) && source.getEntity() instanceof Zombie && net.minecraftforge.event.ForgeEventFactory.canLivingConvert((LivingEntity) source.getEntity(), EntityType.ZOMBIE_VILLAGER, (timer) -> {
        })) {
            ZombieVillager zombieguard = this.convertTo(EntityType.ZOMBIE_VILLAGER, true);
            if (level().getDifficulty() != Difficulty.HARD && this.random.nextBoolean() || zombieguard == null) {
                return;
            }
            zombieguard.finalizeSpawn((ServerLevelAccessor) level(), level().getCurrentDifficultyAt(zombieguard.blockPosition()), MobSpawnType.CONVERSION, new Zombie.ZombieGroupData(false, true), (CompoundTag) null);
            if (!this.isSilent()) level().levelEvent(null, 1026, this.blockPosition(), 0);
            this.discard();
        }
        super.die(source);
    }

    @Override
    public ItemStack eat(Level world, ItemStack stack) {
        if (stack.isEdible()) {
            this.heal(stack.getItem().getFoodProperties(stack, this).getNutrition());
        }
        world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
        super.eat(world, stack);
        return stack;
    }

    @Override
    public void aiStep() {
        if (this.kickTicks > 0) --this.kickTicks;
        if (this.kickCoolDown > 0) --this.kickCoolDown;
        if (this.shieldCoolDown > 0) --this.shieldCoolDown;
        if (this.getHealth() < this.getMaxHealth() && this.tickCount % 200 == 0) {
            this.heal(GuardConfig.amountOfHealthRegenerated);
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
    public EntityDimensions getDimensions(Pose poseIn) {
        return SIZE_BY_POSE.getOrDefault(poseIn, EntityDimensions.scalable(0.6F, 1.95F));
    }

    @Override
    public float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
        if (poseIn == Pose.CROUCHING) {
            return 1.40F;
        }
        return super.getStandingEyeHeight(poseIn, sizeIn);
    }

    @Override
    protected void blockUsingShield(LivingEntity entityIn) {
        super.blockUsingShield(entityIn);
        this.playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 1.0F);
        if (entityIn.getMainHandItem().canDisableShield(this.useItem, this, entityIn)) this.disableShield(true);
    }

    @Override
    protected void hurtCurrentlyUsedShield(float damage) {
        if (this.useItem.canPerformAction(net.minecraftforge.common.ToolActions.SHIELD_BLOCK)) {
            if (damage >= 3.0F) {
                int i = 1 + Mth.floor(damage);
                InteractionHand hand = this.getUsedItemHand();
                this.damageGuardItem(i, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND, this.useItem);
                this.useItem.hurtAndBreak(i, this, (entity) -> entity.broadcastBreakEvent(hand));
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
    public void startUsingItem(InteractionHand hand) {
        super.startUsingItem(hand);
        ItemStack itemstack = this.getItemInHand(hand);
        if (itemstack.canPerformAction(net.minecraftforge.common.ToolActions.SHIELD_BLOCK)) {
            AttributeInstance modifiableattributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
            modifiableattributeinstance.removeModifier(USE_ITEM_SPEED_PENALTY);
            modifiableattributeinstance.addTransientModifier(USE_ITEM_SPEED_PENALTY);
        }
    }

    @Override
    public void stopUsingItem() {
        super.stopUsingItem();
        if (this.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(USE_ITEM_SPEED_PENALTY))
            this.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(USE_ITEM_SPEED_PENALTY);
    }

    public void disableShield(boolean increase) {
        float chance = 0.25F + (float) EnchantmentHelper.getBlockEfficiency(this) * 0.05F;
        if (increase) chance += 0.75;
        if (this.random.nextFloat() < chance) {
            this.shieldCoolDown = 100;
            this.stopUsingItem();
            level().broadcastEntityEvent(this, (byte) 30);
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(GUARD_VARIANT, VillagerType.PLAINS.toString());
        this.entityData.define(DATA_CHARGING_STATE, false);
        this.entityData.define(KICKING, false);
        this.entityData.define(OWNER_UNIQUE_ID, Optional.empty());
        this.entityData.define(FOLLOWING, false);
        this.entityData.define(GUARD_POS, Optional.empty());
        this.entityData.define(PATROLLING, false);
        this.entityData.define(RUNNING_TO_EAT, false);
    }

    public boolean isCharging() {
        return this.entityData.get(DATA_CHARGING_STATE);
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

    public static List<ItemStack> getItemsFromLootTable(LivingEntity entity) {
        LootTable loot = entity.level().getServer().getLootData().getLootTable(new ResourceLocation(GuardVillagers.MODID, "entities/guard_armor"));
        LootParams.Builder lootcontext$builder = (new LootParams.Builder((ServerLevel) entity.level()).withParameter(LootContextParams.THIS_ENTITY, entity));
        return loot.getRandomItems(lootcontext$builder.create(GuardLootTables.SLOT));
    }

    public String getGuardVariant() {
        String variant = this.entityData.get(GUARD_VARIANT);
        return !variant.isEmpty() ? variant : "plains";
    }

    public void setGuardVariant(String typeId) {
        this.entityData.set(GUARD_VARIANT, typeId);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new KickGoal(this));
        this.goalSelector.addGoal(0, new GuardEatFoodGoal(this));
        this.goalSelector.addGoal(0, new RaiseShieldGoal(this));
        this.goalSelector.addGoal(1, new GuardRunToEatGoal(this));
        this.goalSelector.addGoal(3, new RangedCrossbowAttackPassiveGoal<>(this, 1.0D, 8.0F));
        this.goalSelector.addGoal(3, new RangedBowAttackGoal(this, 0.5D, 20, 15.0F) {
            @Override
            public boolean canUse() {
                return Guard.this.getTarget() != null && this.isBowInMainhand() && !Guard.this.isEating() && !Guard.this.isBlocking();
            }

            protected boolean isBowInMainhand() {
                return Guard.this.getMainHandItem().getItem() instanceof BowItem;
            }

            @Override
            public void tick() {
                super.tick();
                if (Guard.this.getTarget() != null)
                    Guard.this.getLookControl().setLookAt(Guard.this.getTarget(), 30.0F, 30.0F);
                if (Guard.this.isPatrolling()) {
                    Guard.this.getNavigation().stop();
                    Guard.this.getMoveControl().strafe(0.0F, 0.0F);
                }
                if (RangedCrossbowAttackPassiveGoal.friendlyInLineOfSight(Guard.this)) {
                    Guard.this.stopUsingItem();
                }
            }

            @Override
            public boolean canContinueToUse() {
                return (this.canUse() || !Guard.this.getNavigation().isDone()) && this.isBowInMainhand();
            }
        });
        if (ModList.get().isLoaded("musketmod"))
            this.goalSelector.addGoal(3, new ModCompat.UseMusketGoal(this, 20, 15.0F));
        this.goalSelector.addGoal(3, new GuardMeleeGoal(this, 0.8D, true));
        this.goalSelector.addGoal(3, new WalkBackToCheckPointGoal(this, 0.5D));
       this.goalSelector.addGoal(4, new FollowHeroGoal(this, 0.8F, 10.0F, 4.0F));
        if (GuardConfig.GuardsRunFromPolarBears)
            this.goalSelector.addGoal(4, new AvoidEntityGoal<>(this, PolarBear.class, 12.0F, 1.0D, 1.2D));
        this.goalSelector.addGoal(4, new MoveBackToVillageGoal(this, 0.5D, false));
        if (GuardConfig.GuardsOpenDoors) this.goalSelector.addGoal(4, new GuardInteractDoorGoal(this, true));
        if (GuardConfig.GuardFormation) this.goalSelector.addGoal(6, new FollowShieldGuards(this)); // phalanx
        if (GuardConfig.ClericHealing) this.goalSelector.addGoal(6, new RunToClericGoal(this));
        if (GuardConfig.armorerRepairGuardArmor) this.goalSelector.addGoal(6, new ArmorerRepairGuardArmorGoal(this));
        this.goalSelector.addGoal(5, new GolemRandomStrollInVillageGoal(this, 0.5D));
        if (GuardConfig.COMMON.guardPatrol.get())
            this.goalSelector.addGoal(5, new MoveThroughVillageGoal(this, 0.5D, false, 4, () -> false));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.5D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, AbstractVillager.class, 8.0F));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new GuardLookAtAndStopMovingWhenBeingTheInteractionTarget(this));
        this.targetSelector.addGoal(2, (new HurtByTargetGoal(this, Guard.class, IronGolem.class)).setAlertOthers());
        this.targetSelector.addGoal(3, new HeroHurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new HeroHurtTargetGoal(this));
        this.targetSelector.addGoal(5, new Guard.DefendVillageGuardGoal(this));
        if (GuardConfig.AttackAllMobs) {
            this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 5, true, true, (mob) -> mob instanceof Enemy && !GuardConfig.MobBlackList.contains(mob.getEncodeId())));
        } else {
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Ravager.class, true)); // To make witches and ravagers have a priority than other mobs this has to be done
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Witch.class, true));
            this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Raider.class, true));
            this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Zombie.class, true, (mob) -> !(mob instanceof NeutralMob)));
        }
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 5, true, true, (mob) -> GuardConfig.COMMON.MobWhiteList.get().contains(mob.getEncodeId())));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(4, new ResetUniversalAngerTargetGoal<>(this, false));
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return false;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        this.shieldCoolDown = 8;
        if (this.getMainHandItem().getItem() instanceof CrossbowItem) this.performCrossbowAttack(this, 1.6F);
        if (this.getMainHandItem().getItem() instanceof BowItem) {
            ItemStack hand = this.getMainHandItem();
            ItemStack itemstack = this.getProjectile(hand);
            AbstractArrow abstractarrowentity = ProjectileUtil.getMobArrow(this, itemstack, distanceFactor);
            abstractarrowentity = ((BowItem) this.getMainHandItem().getItem()).customArrow(abstractarrowentity);
            double d0 = target.getX() - this.getX();
            double d1 = target.getY(0.3333333333333333D) - abstractarrowentity.getY();
            double d2 = target.getZ() - this.getZ();
            double d3 = Mth.sqrt((float) (d0 * d0 + d2 * d2));
            abstractarrowentity.shoot(d0, d1 + d3 * (double) 0.2F, d2, 1.6F, 0.0F);
            this.playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
            this.level().addFreshEntity(abstractarrowentity);
            this.damageGuardItem(1, EquipmentSlot.MAINHAND, hand);
        }
        if (ModList.get().isLoaded("musketmod"))
            ModCompat.shootGun(this);
    }

    @Override
    public void performCrossbowAttack(LivingEntity pUser, float pVelocity) {
        ItemStack stack = pUser.getMainHandItem();
        CrossbowItem.performShooting(pUser.level(), pUser, InteractionHand.MAIN_HAND, stack, pVelocity, 0.0F);
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
        return this.entityData.get(FOLLOWING) && (this.getOwner() != null);
    }

    public void setFollowing(boolean following) {
        this.entityData.set(FOLLOWING, following);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return (GuardConfig.MobBlackList.contains(target.getEncodeId()) || target.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) || this.isOwner(target) ? false : super.canAttack(target));
    }

    @Override
    public void rideTick() {
        super.rideTick();
        if (this.getVehicle() instanceof PathfinderMob) {
            PathfinderMob creatureentity = (PathfinderMob) this.getVehicle();
            this.yBodyRot = creatureentity.yBodyRot;
        }
    }

    @Override
    public double getMyRidingOffset() {
        return -0.35D;
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
    public void shootCrossbowProjectile(LivingEntity arg0, ItemStack arg1, Projectile arg2, float arg3) {
        this.shootCrossbowProjectile(this, arg0, arg2, arg3, 1.6F);
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
        boolean configValues = player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) && GuardConfig.giveGuardStuffHOTV || player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) && GuardConfig.setGuardPatrolHotv || player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) && GuardConfig.giveGuardStuffHOTV && GuardConfig.setGuardPatrolHotv || this.getPlayerReputation(player) >= GuardConfig.reputationRequirement || player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) && !GuardConfig.giveGuardStuffHOTV && !GuardConfig.setGuardPatrolHotv || this.getOwnerId() != null && this.getOwnerId().equals(player.getUUID());
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
    protected void hurtArmor(DamageSource damageSource, float pDamage) {
        if (!(pDamage <= 0.0F)) {
            pDamage /= 4.0F;
            if (pDamage < 1.0F) {
                pDamage = 1.0F;
            }

            for (int i = 0; i < this.guardInventory.getContainerSize(); ++i) {
                ItemStack itemstack = this.guardInventory.getItem(i);
                if ((!damageSource.is(DamageTypes.ON_FIRE) || !itemstack.getItem().isFireResistant()) && itemstack.getItem() instanceof ArmorItem armorItem) {
                    EquipmentSlot slot = armorItem.getEquipmentSlot();
                    this.damageGuardItem(1, slot, itemstack);
                }
            }
        }
    }

    @Override
    public void thunderHit(ServerLevel p_241841_1_, LightningBolt p_241841_2_) {
        if (p_241841_1_.getDifficulty() != Difficulty.PEACEFUL && net.minecraftforge.event.ForgeEventFactory.canLivingConvert(this, EntityType.WITCH, (timer) -> {
        })) {
            Witch witchentity = EntityType.WITCH.create(p_241841_1_);
            if (witchentity == null) return;
            witchentity.copyPosition(this);
            witchentity.finalizeSpawn(p_241841_1_, p_241841_1_.getCurrentDifficultyAt(witchentity.blockPosition()), MobSpawnType.CONVERSION, null, null);
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
        GuardPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new GuardOpenInventoryPacket(player.containerCounter, this.guardInventory.getContainerSize(), this.getId()));
        player.containerMenu = new GuardContainer(player.containerCounter, player.getInventory(), this.guardInventory, this);
        player.initMenu(player.containerMenu);
        MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, player.containerMenu));
    }

    @Override
    public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @Nullable net.minecraft.core.Direction facing) {
        if (this.isAlive() && capability == ForgeCapabilities.ITEM_HANDLER && itemHandler != null)
            return itemHandler.cast();
        return super.getCapability(capability, facing);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        if (itemHandler != null) {
            net.minecraftforge.common.util.LazyOptional<?> oldHandler = itemHandler;
            itemHandler = null;
            oldHandler.invalidate();
        }
    }

    public boolean isEating() {
        return GuardEatFoodGoal.isConsumable(this.getUseItem()) && this.isUsingItem();
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

    public void tryToTeleportToOwner() {
        LivingEntity livingentity = this.getOwner();
        if (livingentity != null)
            this.teleportToAroundBlockPos(livingentity.blockPosition());
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
        BlockPathTypes pathtype = WalkNodeEvaluator.getBlockPathTypeStatic(this.level(), pos.mutable());
        if (pathtype != BlockPathTypes.WALKABLE) {
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

    public void damageGuardItem(int damage, EquipmentSlot slotToDamage, ItemStack item) {
        if (this.random.nextFloat() < GuardConfig.COMMON.chanceToBreakEquipment.get().floatValue()) {
            item.hurtAndBreak(damage, this, (entity) -> entity.broadcastBreakEvent(slotToDamage));
        }
    }


    public static class DefendVillageGuardGoal extends TargetGoal {
        private final Guard guard;
        private LivingEntity villageAggressorTarget;

        public DefendVillageGuardGoal(Guard guardIn) {
            super(guardIn, true, true);
            this.guard = guardIn;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET, Goal.Flag.MOVE));
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
            this.oldWaterCost = this.guard.getPathfindingMalus(BlockPathTypes.WATER);
            this.guard.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        }

        @Override
        public void stop() {
            this.owner = null;
            this.navigation.stop();
            this.guard.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
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
                if (path != null && target.distanceTo(guard) <= 2.0D) guard.getNavigation().stop();
                super.tick();
            }
        }

        @Override
        protected double getAttackReachSqr(LivingEntity attackTarget) {
            return super.getAttackReachSqr(attackTarget) * 2.55D;
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity enemy, double distToEnemySqr) {
            double d0 = this.getAttackReachSqr(enemy);
            if (distToEnemySqr <= d0 && this.ticksUntilNextAttack <= 0) {
                this.resetAttackCooldown();
                this.guard.stopUsingItem();
                if (guard.shieldCoolDown == 0) this.guard.shieldCoolDown = 8;
                this.guard.swing(InteractionHand.MAIN_HAND);
                this.guard.doHurtTarget(enemy);
            }
        }
    }
}