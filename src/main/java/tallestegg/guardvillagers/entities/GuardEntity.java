package tallestegg.guardvillagers.entities;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IAngerable;
import net.minecraft.entity.ICrossbowUser;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.OpenDoorGoal;
import net.minecraft.entity.ai.goal.PatrolVillageGoal;
import net.minecraft.entity.ai.goal.RandomWalkingGoal;
import net.minecraft.entity.ai.goal.ResetAngerGoal;
import net.minecraft.entity.ai.goal.ReturnToVillageGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.AbstractIllagerEntity;
import net.minecraft.entity.monster.AbstractRaiderEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.monster.IllusionerEntity;
import net.minecraft.entity.monster.RavagerEntity;
import net.minecraft.entity.monster.WitchEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MilkBucketItem;
import net.minecraft.item.PotionItem;
import net.minecraft.item.ShootableItem;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.RangedInteger;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.TickRangeConverter;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import tallestegg.guardvillagers.GuardItems;
import tallestegg.guardvillagers.GuardLootTables;
import tallestegg.guardvillagers.GuardPacketHandler;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.entities.ai.goals.ArmorerRepairGuardArmorGoal;
import tallestegg.guardvillagers.entities.ai.goals.FollowShieldGuards;
import tallestegg.guardvillagers.entities.ai.goals.GuardEatFoodGoal;
import tallestegg.guardvillagers.entities.ai.goals.GuardRunToEatGoal;
import tallestegg.guardvillagers.entities.ai.goals.GuardSetRunningToEatGoal;
import tallestegg.guardvillagers.entities.ai.goals.HeroHurtByTargetGoal;
import tallestegg.guardvillagers.entities.ai.goals.HeroHurtTargetGoal;
import tallestegg.guardvillagers.entities.ai.goals.KickGoal;
import tallestegg.guardvillagers.entities.ai.goals.RaiseShieldGoal;
import tallestegg.guardvillagers.entities.ai.goals.RangedBowAttackPassiveGoal;
import tallestegg.guardvillagers.entities.ai.goals.RangedCrossbowAttackPassiveGoal;
import tallestegg.guardvillagers.entities.ai.goals.RunToClericGoal;
import tallestegg.guardvillagers.entities.ai.goals.WalkBackToCheckPointGoal;
import tallestegg.guardvillagers.networking.GuardOpenInventoryPacket;

public class GuardEntity extends CreatureEntity implements ICrossbowUser, IRangedAttackMob, IAngerable, IInventoryChangedListener {
    private static final UUID MODIFIER_UUID = UUID.fromString("5CD17E52-A79A-43D3-A529-90FDE04B181E");
    private static final AttributeModifier USE_ITEM_SPEED_PENALTY = new AttributeModifier(MODIFIER_UUID, "Use item speed penalty", -0.25D, AttributeModifier.Operation.ADDITION);
    private static final DataParameter<Optional<BlockPos>> GUARD_POS = EntityDataManager.createKey(GuardEntity.class, DataSerializers.OPTIONAL_BLOCK_POS);
    private static final DataParameter<Boolean> PATROLLING = EntityDataManager.createKey(GuardEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> GUARD_VARIANT = EntityDataManager.createKey(GuardEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> RUNNING_TO_EAT = EntityDataManager.createKey(GuardEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> DATA_CHARGING_STATE = EntityDataManager.createKey(GuardEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> EATING = EntityDataManager.createKey(GuardEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> KICKING = EntityDataManager.createKey(GuardEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> FOLLOWING = EntityDataManager.createKey(GuardEntity.class, DataSerializers.BOOLEAN);
    protected static final DataParameter<Optional<UUID>> OWNER_UNIQUE_ID = EntityDataManager.createKey(GuardEntity.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final Map<Pose, EntitySize> SIZE_BY_POSE = ImmutableMap.<Pose, EntitySize>builder().put(Pose.STANDING, EntitySize.flexible(0.6F, 1.95F)).put(Pose.SLEEPING, SLEEPING_SIZE).put(Pose.FALL_FLYING, EntitySize.flexible(0.6F, 0.6F)).put(Pose.SWIMMING, EntitySize.flexible(0.6F, 0.6F))
            .put(Pose.SPIN_ATTACK, EntitySize.flexible(0.6F, 0.6F)).put(Pose.CROUCHING, EntitySize.flexible(0.6F, 1.75F)).put(Pose.DYING, EntitySize.fixed(0.2F, 0.2F)).build();
    public Inventory guardInventory = new Inventory(6);
    public int kickTicks;
    public int shieldCoolDown;
    public int kickCoolDown;
    public boolean interacting;
    private int field_234197_bv_;
    private static final RangedInteger angerTime = TickRangeConverter.convertRange(20, 39);
    private UUID field_234198_bw_;
    private static final Map<EquipmentSlotType, ResourceLocation> EQUIPMENT_SLOT_ITEMS = Util.make(Maps.newHashMap(), (slotItems) -> {
        slotItems.put(EquipmentSlotType.MAINHAND, GuardLootTables.GUARD_MAIN_HAND);
        slotItems.put(EquipmentSlotType.OFFHAND, GuardLootTables.GUARD_OFF_HAND);
        slotItems.put(EquipmentSlotType.HEAD, GuardLootTables.GUARD_HELMET);
        slotItems.put(EquipmentSlotType.CHEST, GuardLootTables.GUARD_CHEST);
        slotItems.put(EquipmentSlotType.LEGS, GuardLootTables.GUARD_LEGGINGS);
        slotItems.put(EquipmentSlotType.FEET, GuardLootTables.GUARD_FEET);
    });

    public GuardEntity(EntityType<? extends GuardEntity> type, World world) {
        super(type, world);
        this.guardInventory.addListener(this);
        this.itemHandler = net.minecraftforge.common.util.LazyOptional.of(() -> new net.minecraftforge.items.wrapper.InvWrapper(this.guardInventory));
        this.enablePersistence();
        if (GuardConfig.GuardsOpenDoors)
            ((GroundPathNavigator) this.getNavigator()).setBreakDoors(true);
    }

    @Override
    public ILivingEntityData onInitialSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
        this.enablePersistence();
        int type = GuardEntity.getRandomTypeForBiome(world, this.getPosition());
        if (spawnDataIn instanceof GuardEntity.GuardData) {
            type = ((GuardEntity.GuardData) spawnDataIn).variantData;
            spawnDataIn = new GuardEntity.GuardData(type);
        }
        this.setGuardVariant(type);
        this.setEquipmentBasedOnDifficulty(difficultyIn);
        return super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
    }

    @Override
    protected void collideWithEntity(Entity entityIn) {
        if (entityIn instanceof CreatureEntity) {
            CreatureEntity living = (CreatureEntity) entityIn;
            boolean attackTargets = living.getAttackTarget() instanceof VillagerEntity || living.getAttackTarget() instanceof IronGolemEntity || living.getAttackTarget() instanceof GuardEntity;
            if (attackTargets)
                this.setAttackTarget(living);
        }
        super.collideWithEntity(entityIn);
    }

    @Nullable
    public void setPatrolPos(BlockPos position) {
        this.dataManager.set(GUARD_POS, Optional.ofNullable(position));
    }

    @Nullable
    public BlockPos getPatrolPos() {
        return this.dataManager.get(GUARD_POS).orElse((BlockPos) null);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        if (this.isActiveItemStackBlocking()) {
            return SoundEvents.ITEM_SHIELD_BLOCK;
        } else {
            return SoundEvents.ENTITY_VILLAGER_HURT;
        }
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_VILLAGER_DEATH;
    }

    public static int slotToInventoryIndex(EquipmentSlotType slot) {
        switch (slot) {
        case CHEST:
            return 1;
        case FEET:
            return 3;
        case HEAD:
            return 0;
        case LEGS:
            return 2;
        default:
            break;
        }
        return 0;
    }

    @Override
    protected void dropSpecialItems(DamageSource source, int looting, boolean recentlyHitIn) {
        super.dropSpecialItems(source, looting, recentlyHitIn);
        for (int i = 0; i < this.guardInventory.getSizeInventory(); ++i) {
            ItemStack itemstack = this.guardInventory.getStackInSlot(i);
            if (!itemstack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemstack)) {
                this.entityDropItem(itemstack);
            }
        }
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        UUID uuid = compound.hasUniqueId("Owner") ? compound.getUniqueId("Owner") : null;
        if (uuid != null) {
            try {
                this.setOwnerId(uuid);
            } catch (Throwable throwable) {
                this.setOwnerId(null);
            }
        }
        this.setGuardVariant(compound.getInt("Type"));
        this.kickTicks = compound.getInt("KickTicks");
        this.setFollowing(compound.getBoolean("Following"));
        this.interacting = compound.getBoolean("Interacting");
        this.setEating(compound.getBoolean("Eating"));
        this.setPatrolling(compound.getBoolean("Patrolling"));
        this.setRunningToEat(compound.getBoolean("RunningToEat"));
        this.shieldCoolDown = compound.getInt("KickCooldown");
        this.kickCoolDown = compound.getInt("ShieldCooldown");
        ListNBT listnbt = compound.getList("Inventory", 10);
        for (int i = 0; i < listnbt.size(); ++i) {
            CompoundNBT compoundnbt = listnbt.getCompound(i);
            int j = compoundnbt.getByte("Slot") & 255;
            this.guardInventory.setInventorySlotContents(j, ItemStack.read(compoundnbt));
        }
        if (compound.contains("ArmorItems", 9)) {
            ListNBT armorItems = compound.getList("ArmorItems", 10);
            for (int i = 0; i < this.inventoryArmor.size(); ++i) {
                int index = GuardEntity.slotToInventoryIndex(MobEntity.getSlotForItemStack(ItemStack.read(armorItems.getCompound(i))));
                this.guardInventory.setInventorySlotContents(index, ItemStack.read(armorItems.getCompound(i)));
            }
        }
        if (compound.contains("HandItems", 9)) {
            ListNBT handItems = compound.getList("HandItems", 10);
            for (int i = 0; i < this.inventoryHands.size(); ++i) {
                int handSlot = i == 0 ? 5 : 4;
                this.guardInventory.setInventorySlotContents(handSlot, ItemStack.read(handItems.getCompound(i)));
            }
        }
        if (compound.contains("PatrolPosX")) {
            int x = compound.getInt("PatrolPosX");
            int y = compound.getInt("PatrolPosY");
            int z = compound.getInt("PatrolPosZ");
            this.dataManager.set(GUARD_POS, Optional.ofNullable(new BlockPos(x, y, z)));
        }
        if (!world.isRemote)
            this.readAngerNBT((ServerWorld) this.world, compound);
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putInt("Type", this.getGuardVariant());
        compound.putInt("KickTicks", this.kickTicks);
        compound.putInt("ShieldCooldown", this.shieldCoolDown);
        compound.putInt("KickCooldown", this.kickCoolDown);
        compound.putBoolean("Following", this.isFollowing());
        compound.putBoolean("Interacting", this.interacting);
        compound.putBoolean("Eating", this.isEating());
        compound.putBoolean("Patrolling", this.isPatrolling());
        compound.putBoolean("RunningToEat", this.isRunningToEat());
        if (this.getOwnerId() != null) {
            compound.putUniqueId("Owner", this.getOwnerId());
        }
        ListNBT listnbt = new ListNBT();
        for (int i = 0; i < this.guardInventory.getSizeInventory(); ++i) {
            ItemStack itemstack = this.guardInventory.getStackInSlot(i);
            if (!itemstack.isEmpty()) {
                CompoundNBT compoundnbt = new CompoundNBT();
                compoundnbt.putByte("Slot", (byte) i);
                itemstack.write(compoundnbt);
                listnbt.add(compoundnbt);
            }
        }
        compound.put("Inventory", listnbt);
        if (this.getPatrolPos() != null) {
            compound.putInt("PatrolPosX", this.getPatrolPos().getX());
            compound.putInt("PatrolPosY", this.getPatrolPos().getY());
            compound.putInt("PatrolPosZ", this.getPatrolPos().getZ());
        }
        this.writeAngerNBT(compound);
    }

    @Nullable
    public LivingEntity getOwner() {
        try {
            UUID uuid = this.getOwnerId();
            return uuid == null || !this.world.getPlayerByUuid(uuid).isPotionActive(Effects.HERO_OF_THE_VILLAGE) && uuid != null ? null : this.world.getPlayerByUuid(uuid);
        } catch (IllegalArgumentException illegalargumentexception) {
            return null;
        }
    }

    public boolean isOwner(LivingEntity entityIn) {
        return entityIn == this.getOwner();
    }

    @Nullable
    public UUID getOwnerId() {
        return this.dataManager.get(OWNER_UNIQUE_ID).orElse(null);
    }

    public void setOwnerId(@Nullable UUID p_184754_1_) {
        this.dataManager.set(OWNER_UNIQUE_ID, Optional.ofNullable(p_184754_1_));
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (this.isKicking()) {
            ((LivingEntity) entityIn).applyKnockback(1.0F, MathHelper.sin(this.rotationYaw * ((float) Math.PI / 180F)), (-MathHelper.cos(this.rotationYaw * ((float) Math.PI / 180F))));
            this.kickTicks = 10;
            this.world.setEntityState(this, (byte) 4);
            this.faceEntity(entityIn, 90.0F, 90.0F);
        }
        ItemStack hand = this.getHeldItemMainhand();
        hand.damageItem(1, this, (entity) -> entity.sendBreakAnimation(EquipmentSlotType.MAINHAND));
        return super.attackEntityAsMob(entityIn);
    }

    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 4) {
            this.kickTicks = 10;
        } else {
            super.handleStatusUpdate(id);
        }
    }

    @Override
    protected void onItemUseFinish() {
        super.onItemUseFinish();
        if (this.getHeldItemOffhand().getItem() instanceof PotionItem && !(this.getHeldItemOffhand().getItem() instanceof SplashPotionItem))
            this.setHeldItem(Hand.OFF_HAND, new ItemStack(Items.GLASS_BOTTLE));
        if (this.getHeldItemOffhand().getItem() instanceof MilkBucketItem)
            this.setHeldItem(Hand.OFF_HAND, new ItemStack(Items.BUCKET));
    }

    @Override
    public ItemStack onFoodEaten(World world, ItemStack stack) {
        if (stack.isFood()) {
            this.heal(stack.getItem().getFood().getHealing());
        }
        super.onFoodEaten(world, stack);
        world.playSound(null, this.getPosX(), this.getPosY(), this.getPosZ(), SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5F, world.rand.nextFloat() * 0.1F + 0.9F);
        this.setEating(false);
        return stack;
    }

    @Override
    public void livingTick() {
        if (this.kickTicks > 0) {
            --this.kickTicks;
        }
        if (this.kickCoolDown > 0) {
            --this.kickCoolDown;
        }
        if (this.shieldCoolDown > 0) {
            --this.shieldCoolDown;
        }
        if (this.getHealth() < this.getMaxHealth() && this.ticksExisted % 200 == 0) {
            this.heal(GuardConfig.amountOfHealthRegenerated);
        }
        if (!this.world.isRemote)
            this.func_241359_a_((ServerWorld) this.world, true);
        this.updateArmSwingProgress();
        super.livingTick();
    }

    @Override
    public EntitySize getSize(Pose poseIn) {
        return SIZE_BY_POSE.getOrDefault(poseIn, EntitySize.flexible(0.6F, 1.95F));
    }

    @Override
    public float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn) {
        if (poseIn == Pose.CROUCHING) {
            return 1.40F;
        }
        return super.getStandingEyeHeight(poseIn, sizeIn);
    }

    @Override
    protected void blockUsingShield(LivingEntity entityIn) {
        super.blockUsingShield(entityIn);
        if (entityIn.getHeldItemMainhand().canDisableShield(this.activeItemStack, this, entityIn)) {
            this.disableShield(true);
        }
    }

    @Override
    protected void damageShield(float damage) {
        if (this.activeItemStack.isShield(this)) {
            if (damage >= 3.0F) {
                int i = 1 + MathHelper.floor(damage);
                Hand hand = this.getActiveHand();
                this.activeItemStack.damageItem(i, this, (entity) -> entity.sendBreakAnimation(hand));
                if (this.activeItemStack.isEmpty()) {
                    if (hand == Hand.MAIN_HAND) {
                        this.setItemStackToSlot(EquipmentSlotType.MAINHAND, ItemStack.EMPTY);
                    } else {
                        this.setItemStackToSlot(EquipmentSlotType.OFFHAND, ItemStack.EMPTY);
                    }
                    this.activeItemStack = ItemStack.EMPTY;
                    this.playSound(SoundEvents.ITEM_SHIELD_BREAK, 0.8F, 0.8F + this.world.rand.nextFloat() * 0.4F);
                }
            }
        }
    }

    @Override
    public void setActiveHand(Hand hand) {
        ItemStack itemstack = this.getHeldItem(hand);
        if (itemstack.isShield(this)) {
            ModifiableAttributeInstance modifiableattributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
            modifiableattributeinstance.removeModifier(USE_ITEM_SPEED_PENALTY);
            modifiableattributeinstance.applyNonPersistentModifier(USE_ITEM_SPEED_PENALTY);
        }
        super.setActiveHand(hand);
    }

    @Override
    public void resetActiveHand() {
        if (this.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(USE_ITEM_SPEED_PENALTY))
            this.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(USE_ITEM_SPEED_PENALTY);
        super.resetActiveHand();
    }

    @Override
    public void setAttackTarget(LivingEntity target) {
        super.setAttackTarget(target);
    }

    public void disableShield(boolean increase) {
        float f = 0.25F + (float) EnchantmentHelper.getEfficiencyModifier(this) * 0.05F;
        if (increase) {
            f += 0.75F;
        }
        if (this.rand.nextFloat() < f) {
            this.shieldCoolDown = 100;
            this.resetActiveHand();
            this.world.setEntityState(this, (byte) 30);
        }
    }

    @Override
    protected void registerData() {
        super.registerData();
        this.dataManager.register(GUARD_VARIANT, 0);
        this.dataManager.register(DATA_CHARGING_STATE, false);
        this.dataManager.register(KICKING, false);
        this.dataManager.register(OWNER_UNIQUE_ID, Optional.empty());
        this.dataManager.register(EATING, false);
        this.dataManager.register(FOLLOWING, false);
        this.dataManager.register(GUARD_POS, Optional.empty());
        this.dataManager.register(PATROLLING, false);
        this.dataManager.register(RUNNING_TO_EAT, false);
    }

    public boolean isCharging() {
        return this.dataManager.get(DATA_CHARGING_STATE);
    }

    public void setCharging(boolean charging) {
        this.dataManager.set(DATA_CHARGING_STATE, charging);
    }

    public boolean isKicking() {
        return this.dataManager.get(KICKING);
    }

    public void setKicking(boolean kicking) {
        this.dataManager.set(KICKING, kicking);
    }

    @Override
    protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
        for (EquipmentSlotType equipmentslottype : EquipmentSlotType.values()) {
            for (ItemStack stack : this.getItemsFromLootTable(equipmentslottype)) {
                this.setItemStackToSlot(equipmentslottype, stack);
            }
        }
        this.inventoryHandsDropChances[EquipmentSlotType.MAINHAND.getIndex()] = 100.0F;
        this.inventoryHandsDropChances[EquipmentSlotType.OFFHAND.getIndex()] = 100.0F;
    }

    public List<ItemStack> getItemsFromLootTable(EquipmentSlotType slot) {
        if (EQUIPMENT_SLOT_ITEMS.containsKey(slot)) {
            LootTable loot = this.world.getServer().getLootTableManager().getLootTableFromLocation(EQUIPMENT_SLOT_ITEMS.get(slot));
            LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerWorld) this.world)).withParameter(LootParameters.THIS_ENTITY, this).withRandom(this.getRNG());
            return loot.generate(lootcontext$builder.build(GuardLootTables.SLOT));
        }
        return null;
    }

    public int getGuardVariant() {
        return this.dataManager.get(GUARD_VARIANT);
    }

    public void setGuardVariant(int typeId) {
        this.dataManager.set(GUARD_VARIANT, typeId);
    }

    // Credit : the abnormals people for discovering this
    public ItemStack getPickedResult(RayTraceResult target) {
        return new ItemStack(GuardItems.GUARD_SPAWN_EGG.get());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(0, new KickGoal(this));
        this.goalSelector.addGoal(0, new GuardEatFoodGoal(this));
        this.goalSelector.addGoal(0, new RaiseShieldGoal(this));
        this.goalSelector.addGoal(1, new GuardRunToEatGoal(this));
        this.goalSelector.addGoal(1, new GuardSetRunningToEatGoal(this, 1.0D));
        this.goalSelector.addGoal(2, new RangedCrossbowAttackPassiveGoal<>(this, 1.0D, 8.0F));
        this.goalSelector.addGoal(2, new RangedBowAttackPassiveGoal<>(this, 0.5D, 20, 15.0F));
        this.goalSelector.addGoal(2, new GuardEntity.GuardMeleeGoal(this, 0.8D, true));
        this.goalSelector.addGoal(3, new GuardEntity.FollowHeroGoal(this));
        if (GuardConfig.GuardsRunFromPolarBears)
            this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, PolarBearEntity.class, 12.0F, 1.0D, 1.2D));
        this.goalSelector.addGoal(3, new ReturnToVillageGoal(this, 0.5D, false));
        this.goalSelector.addGoal(3, new PatrolVillageGoal(this, 0.5D));
        this.goalSelector.addGoal(3, new MoveThroughVillageGoal(this, 0.5D, false, 4, () -> false));
        if (GuardConfig.GuardsOpenDoors)
            this.goalSelector.addGoal(3, new OpenDoorGoal(this, true) {
                @Override
                public void startExecuting() {
                    super.startExecuting();
                    this.entity.swingArm(Hand.MAIN_HAND);
                }
            });
        if (GuardConfig.GuardFormation)
            this.goalSelector.addGoal(5, new FollowShieldGuards(this)); // phalanx
        if (GuardConfig.ClericHealing)
            this.goalSelector.addGoal(6, new RunToClericGoal(this));
        if (GuardConfig.armorerRepairGuardArmor)
            this.goalSelector.addGoal(6, new ArmorerRepairGuardArmorGoal(this));
        this.goalSelector.addGoal(4, new WalkBackToCheckPointGoal(this, 0.5D));
        this.goalSelector.addGoal(8, new LookAtGoal(this, AbstractVillagerEntity.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomWalkingGoal(this, 0.5D));
        this.goalSelector.addGoal(8, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.targetSelector.addGoal(5, new GuardEntity.DefendVillageGuardGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, RavagerEntity.class, true));
        this.targetSelector.addGoal(2, (new HurtByTargetGoal(this, GuardEntity.class, IronGolemEntity.class)).setCallsForHelp());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, WitchEntity.class, true));
        this.targetSelector.addGoal(3, new HeroHurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new HeroHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractIllagerEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractRaiderEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IllusionerEntity.class, true));
        if (GuardConfig.AttackAllMobs) {
            this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, MobEntity.class, 5, true, true, (mob) -> {
                return mob instanceof IMob && !GuardConfig.MobBlackList.contains(mob.getEntityString());
            }));
        }
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, 10, true, false, this::func_233680_b_));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, ZombieEntity.class, true));
        this.targetSelector.addGoal(4, new ResetAngerGoal<>(this, false));
    }

    @Override
    public boolean canBeLeashedTo(PlayerEntity player) {
        return false;
    }

    @Override
    public void attackEntityWithRangedAttack(LivingEntity target, float distanceFactor) {
        this.shieldCoolDown = 8;
        if (this.getHeldItemMainhand().getItem() instanceof CrossbowItem)
            this.func_234281_b_(this, 6.0F);
        if (this.getHeldItemMainhand().getItem() instanceof BowItem) {
            ItemStack itemstack = this.findAmmo(this.getHeldItem(GuardItems.getHandWith(this, item -> item instanceof BowItem)));
            ItemStack hand = this.getHeldItemMainhand();
            hand.damageItem(1, this, (entity) -> entity.sendBreakAnimation(EquipmentSlotType.MAINHAND));
            AbstractArrowEntity abstractarrowentity = ProjectileHelper.fireArrow(this, itemstack, distanceFactor);
            abstractarrowentity = ((net.minecraft.item.BowItem) this.getHeldItemMainhand().getItem()).customArrow(abstractarrowentity);
            double d0 = target.getPosX() - this.getPosX();
            double d1 = target.getPosYHeight(0.3333333333333333D) - abstractarrowentity.getPosY();
            double d2 = target.getPosZ() - this.getPosZ();
            double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
            abstractarrowentity.shoot(d0, d1 + d3 * (double) 0.2F, d2, 1.6F, (float) (14 - this.world.getDifficulty().getId() * 4));
            this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
            this.world.addEntity(abstractarrowentity);
        }
    }

    @Override
    public void setItemStackToSlot(EquipmentSlotType slotIn, ItemStack stack) {
        super.setItemStackToSlot(slotIn, stack);
        switch (slotIn) {
        case CHEST:
            this.guardInventory.setInventorySlotContents(1, this.inventoryArmor.get(slotIn.getIndex()));
            break;
        case FEET:
            this.guardInventory.setInventorySlotContents(3, this.inventoryArmor.get(slotIn.getIndex()));
            break;
        case HEAD:
            this.guardInventory.setInventorySlotContents(0, this.inventoryArmor.get(slotIn.getIndex()));
            break;
        case LEGS:
            this.guardInventory.setInventorySlotContents(2, this.inventoryArmor.get(slotIn.getIndex()));
            break;
        case MAINHAND:
            this.guardInventory.setInventorySlotContents(5, this.inventoryHands.get(slotIn.getIndex()));
            break;
        case OFFHAND:
            this.guardInventory.setInventorySlotContents(4, this.inventoryHands.get(slotIn.getIndex()));
            break;
        }
    }

    @Override
    public ItemStack findAmmo(ItemStack shootable) {
        if (shootable.getItem() instanceof ShootableItem) {
            Predicate<ItemStack> predicate = ((ShootableItem) shootable.getItem()).getAmmoPredicate();
            ItemStack itemstack = ShootableItem.getHeldAmmo(this, predicate);
            return itemstack.isEmpty() ? new ItemStack(Items.ARROW) : itemstack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    public int getKickTicks() {
        return this.kickTicks;
    }

    @Override
    public void travel(Vector3d travelVector) {
        if (!this.interacting)
            super.travel(travelVector);
    }

    public boolean isFollowing() {
        return this.dataManager.get(FOLLOWING);
    }

    public void setFollowing(boolean following) {
        this.dataManager.set(FOLLOWING, following);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !this.isOwner(target) && !(target instanceof VillagerEntity) && super.canAttack(target);
    }

    /**
     * Credit - SmellyModder for Biome Specific Textures
     */
    public static int getRandomTypeForBiome(IWorld world, BlockPos pos) {
        Biome biome = world.getBiome(pos);
        switch (biome.getCategory()) {
        case DESERT:
            return 1;
        case ICY:
            return 6;
        case JUNGLE:
            return 4;
        case MESA:
            return 1;
        case NONE:
            return 0;
        case PLAINS:
            return 0;
        case SAVANNA:
            return 2;
        case SWAMP:
            return 3;
        case TAIGA:
            return 5;
        default:
            return 0;
        }
    }

    @Override
    public void updateRidden() {
        super.updateRidden();
        if (this.getRidingEntity() instanceof CreatureEntity) {
            CreatureEntity creatureentity = (CreatureEntity) this.getRidingEntity();
            this.renderYawOffset = creatureentity.renderYawOffset;
        }
    }

    @Override
    public void func_230283_U__() {
        this.idleTime = 0;
    }

    @Override
    public void func_230284_a_(LivingEntity arg0, ItemStack arg1, ProjectileEntity arg2, float arg3) {
        this.func_234279_a_(this, arg0, arg2, arg3, 1.6F);
    }

    @Override
    protected void constructKnockBackVector(LivingEntity entityIn) {
        if (this.isKicking()) {
            this.setKicking(false);
        }
        super.constructKnockBackVector(this);
    }

    @Override
    protected ActionResultType func_230254_b_(PlayerEntity player, Hand hand) {
        boolean configValues = !GuardConfig.giveGuardStuffHOTV || !GuardConfig.setGuardPatrolHotv || player.isPotionActive(Effects.HERO_OF_THE_VILLAGE) && GuardConfig.giveGuardStuffHOTV || player.isPotionActive(Effects.HERO_OF_THE_VILLAGE) && GuardConfig.setGuardPatrolHotv
                || player.isPotionActive(Effects.HERO_OF_THE_VILLAGE) && GuardConfig.giveGuardStuffHOTV && GuardConfig.setGuardPatrolHotv;
        boolean inventoryRequirements = !player.isSecondaryUseActive() && this.onGround;
        if (configValues && inventoryRequirements) {
            if (this.getAttackTarget() != player && this.isServerWorld()) {
                if (player instanceof ServerPlayerEntity) {
                    this.openGui((ServerPlayerEntity) player);
                    return ActionResultType.SUCCESS;
                }
            }
            return ActionResultType.CONSUME;
        }
        return super.func_230254_b_(player, hand);
    }

    @Override
    public boolean replaceItemInInventory(int inventorySlot, ItemStack itemStackIn) {
        int i = inventorySlot - 400;
        if (i >= 0 && i < 2 && i < this.guardInventory.getSizeInventory()) {
            if (i == 0) {
                return false;
            } else if (itemStackIn.getItem() instanceof ArmorItem) {
                this.guardInventory.setInventorySlotContents(i, itemStackIn);
                return true;
            } else {
                return false;
            }
        } else {
            int j = inventorySlot - 500 + 2;
            if (j >= 2 && j < this.guardInventory.getSizeInventory()) {
                this.guardInventory.setInventorySlotContents(j, itemStackIn);
                return true;
            } else {
                return false;
            }
        }
    }

    protected void playHealEffect() {
        IParticleData iparticledata = ParticleTypes.HAPPY_VILLAGER;
        for (int i = 0; i < 7; ++i) {
            double d0 = this.rand.nextGaussian() * 0.02D;
            double d1 = this.rand.nextGaussian() * 0.02D;
            double d2 = this.rand.nextGaussian() * 0.02D;
            this.world.addParticle(iparticledata, this.getPosX() + (double) (this.rand.nextFloat() * this.getWidth() * 2.0F) - (double) this.getWidth(), this.getPosY() + 0.5D + (double) (this.rand.nextFloat() * this.getHeight()),
                    this.getPosZ() + (double) (this.rand.nextFloat() * this.getWidth() * 2.0F) - (double) this.getWidth(), d0, d1, d2);
        }
    }

    public static String getNameByType(int id) {
        switch (id) {
        case 0:
            return "plains";
        case 1:
            return "desert";
        case 2:
            return "savanna";
        case 3:
            return "swamp";
        case 4:
            return "jungle";
        case 5:
            return "taiga";
        case 6:
            return "snow";
        }
        return "";
    }

    @Override
    public void onInventoryChanged(IInventory invBasic) {
    }

    @Override
    protected void damageArmor(DamageSource damageSource, float damage) {
        if (damage >= 0.0F) {
            damage = damage / 4.0F;
            if (damage < 1.0F) {
                damage = 1.0F;
            }
            for (int i = 0; i < this.guardInventory.getSizeInventory(); ++i) {
                ItemStack itemstack = this.guardInventory.getStackInSlot(i);
                if ((!damageSource.isFireDamage() || !itemstack.getItem().isImmuneToFire()) && itemstack.getItem() instanceof ArmorItem) {
                    int j = i;
                    itemstack.damageItem((int) damage, this, (p_214023_1_) -> {
                        p_214023_1_.sendBreakAnimation(EquipmentSlotType.fromSlotTypeAndIndex(EquipmentSlotType.Group.ARMOR, j));
                    });
                }
            }
        }
    }

    @Override
    public void func_241841_a(ServerWorld p_241841_1_, LightningBoltEntity p_241841_2_) {
        if (p_241841_1_.getDifficulty() != Difficulty.PEACEFUL) {
            WitchEntity witchentity = EntityType.WITCH.create(p_241841_1_);
            if (witchentity == null)
                return;
            witchentity.copyLocationAndAnglesFrom(this);
            witchentity.onInitialSpawn(p_241841_1_, p_241841_1_.getDifficultyForLocation(witchentity.getPosition()), SpawnReason.CONVERSION, null, null);
            witchentity.setNoAI(this.isAIDisabled());
            witchentity.setCustomName(this.getCustomName());
            witchentity.setCustomNameVisible(this.isCustomNameVisible());
            witchentity.enablePersistence();
            p_241841_1_.func_242417_l(witchentity);
            this.remove();
        } else {
            super.func_241841_a(p_241841_1_, p_241841_2_);
        }
    }

    @Override
    public UUID getAngerTarget() {
        return this.field_234198_bw_;
    }

    @Override
    public int getAngerTime() {
        return this.field_234197_bv_;
    }

    @Override
    public void setAngerTarget(UUID arg0) {
        this.field_234198_bw_ = arg0;
    }

    @Override
    public void setAngerTime(int arg0) {
        this.field_234197_bv_ = arg0;
    }

    @Override
    public void func_230258_H__() {
        this.setAngerTime(angerTime.getRandomWithinRange(rand));
    }

    public void openGui(ServerPlayerEntity player) {
        if (player.openContainer != player.container) {
            player.closeScreen();
        }
        this.interacting = true;
        player.getNextWindowId();
        GuardPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new GuardOpenInventoryPacket(player.currentWindowId, this.guardInventory.getSizeInventory(), this.getEntityId()));
        player.openContainer = new GuardContainer(player.currentWindowId, player.inventory, this.guardInventory, this);
        player.openContainer.addListener(player);
        MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, player.openContainer));
    }

    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MobEntity.func_233666_p_().createMutableAttribute(Attributes.MAX_HEALTH, GuardConfig.GuardHealth).createMutableAttribute(Attributes.MOVEMENT_SPEED, GuardConfig.GuardSpeed).createMutableAttribute(Attributes.ATTACK_DAMAGE, 1.0D).createMutableAttribute(Attributes.FOLLOW_RANGE,
                GuardConfig.GuardFollowRange);
    }

    private net.minecraftforge.common.util.LazyOptional<?> itemHandler;

    @Override
    public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @Nullable net.minecraft.util.Direction facing) {
        if (this.isAlive() && capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && itemHandler != null)
            return itemHandler.cast();
        return super.getCapability(capability, facing);
    }

    public boolean isEating() {
        return this.dataManager.get(EATING);
    }

    public void setEating(boolean eating) {
        this.dataManager.set(EATING, eating);
    }

    public boolean isPatrolling() {
        return this.dataManager.get(PATROLLING);
    }

    public void setPatrolling(boolean patrolling) {
        this.dataManager.set(PATROLLING, patrolling);
    }

    public boolean isRunningToEat() {
        return this.dataManager.get(RUNNING_TO_EAT);
    }

    public void setRunningToEat(boolean running) {
        this.dataManager.set(RUNNING_TO_EAT, running);
    }

    public static class GuardData implements ILivingEntityData {
        public final int variantData;

        public GuardData(int type) {
            this.variantData = type;
        }
    }

    public static class DefendVillageGuardGoal extends TargetGoal {
        private final GuardEntity guard;
        private LivingEntity villageAggressorTarget;

        public DefendVillageGuardGoal(GuardEntity guardIn) {
            super(guardIn, false, true);
            this.guard = guardIn;
            this.setMutexFlags(EnumSet.of(Goal.Flag.TARGET, Goal.Flag.MOVE));
        }

        @Override
        public boolean shouldExecute() {
            AxisAlignedBB axisalignedbb = this.guard.getBoundingBox().grow(10.0D, 8.0D, 10.0D);
            List<LivingEntity> list = guard.world.getEntitiesWithinAABB(VillagerEntity.class, axisalignedbb);
            List<PlayerEntity> list1 = guard.world.getEntitiesWithinAABB(PlayerEntity.class, axisalignedbb);
            for (LivingEntity livingentity : list) {
                VillagerEntity villagerentity = (VillagerEntity) livingentity;
                for (PlayerEntity playerentity : list1) {
                    int i = villagerentity.getPlayerReputation(playerentity);
                    if (i <= -100) {
                        this.villageAggressorTarget = playerentity;
                    }
                }
            }
            return villageAggressorTarget != null && !this.villageAggressorTarget.isSpectator() && !((PlayerEntity) this.villageAggressorTarget).isCreative();
        }

        @Override
        public void startExecuting() {
            this.guard.setAttackTarget(this.villageAggressorTarget);
            super.startExecuting();
        }
    }

    public static class FollowHeroGoal extends Goal {
        public final GuardEntity guard;

        public FollowHeroGoal(GuardEntity mob) {
            guard = mob;
            this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public void startExecuting() {
            super.startExecuting();
            if (guard.getOwner() != null) {
                guard.getNavigator().tryMoveToEntityLiving(guard.getOwner(), 0.9D);
            }
        }

        @Override
        public void tick() {
            if (guard.getOwner() != null) {
                guard.getNavigator().tryMoveToEntityLiving(guard.getOwner(), 0.9D);
            }
        }

        @Override
        public boolean shouldContinueExecuting() {
            return guard.isFollowing() && this.shouldExecute();
        }

        @Override
        public boolean shouldExecute() {
            List<LivingEntity> list = this.guard.world.getEntitiesWithinAABB(PlayerEntity.class, this.guard.getBoundingBox().grow(10.0D));
            if (!list.isEmpty()) {
                for (LivingEntity mob : list) {
                    PlayerEntity player = (PlayerEntity) mob;
                    if (!player.isInvisible() && player.isPotionActive(Effects.HERO_OF_THE_VILLAGE)) {
                        guard.setOwnerId(player.getUniqueID());
                        return guard.isFollowing();
                    }
                }
            }
            return false;
        }

        @Override
        public void resetTask() {
            this.guard.getNavigator().clearPath();
            if (guard.getOwner() != null && !guard.getOwner().isPotionActive(Effects.HERO_OF_THE_VILLAGE)) {
                guard.setOwnerId(null);
                guard.setFollowing(false);
            }
        }
    }

    public class GuardMeleeGoal extends MeleeAttackGoal {
        public final GuardEntity guard;

        public GuardMeleeGoal(GuardEntity guard, double speedIn, boolean useLongMemory) {
            super(guard, speedIn, useLongMemory);
            this.guard = guard;
        }

        @Override
        public boolean shouldExecute() {
            return !(this.guard.getHeldItemMainhand().getItem() instanceof CrossbowItem) && this.guard.getAttackTarget() != null && !this.guard.isEating() && super.shouldExecute();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return super.shouldContinueExecuting() && this.guard.getAttackTarget() != null && !(this.guard.getHeldItemMainhand().getItem() instanceof CrossbowItem);
        }

        @Override
        public void tick() {
            LivingEntity target = guard.getAttackTarget();
            if (target != null) {
                if (target.getDistance(guard) <= 3.0D && !guard.isActiveItemStackBlocking()) {
                    guard.getMoveHelper().strafe(-2.0F, 0.0F);
                    guard.faceEntity(target, 30.0F, 30.0F);
                }
                if (path != null && target.getDistance(guard) <= 2.0D)
                    guard.getNavigator().clearPath();
                super.tick();
            }
        }

        @Override
        protected double getAttackReachSqr(LivingEntity attackTarget) {
            return super.getAttackReachSqr(attackTarget) * 3.55D;
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity enemy, double distToEnemySqr) {
            double d0 = this.getAttackReachSqr(enemy);
            if (distToEnemySqr <= d0 && this.field_234037_i_ <= 0) {
                this.func_234039_g_();
                this.guard.resetActiveHand();
                if (guard.shieldCoolDown == 0)
                    this.guard.shieldCoolDown = 8;
                this.guard.swingArm(Hand.MAIN_HAND);
                this.guard.attackEntityAsMob(enemy);
            }
        }
    }
}