package tallestegg.guardvillagers.common.entities.ai.tasks;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import tallestegg.guardvillagers.GuardDataAttachments;
import tallestegg.guardvillagers.common.entities.Guard;
import tallestegg.guardvillagers.configuration.GuardConfig;

import java.util.List;
import java.util.Optional;

public class HealGuardAndHero extends VillagerHelp {
    private LivingEntity targetToHeal;
    private int waitUntilInSightTicks = 0;

    public HealGuardAndHero() {
        super(ImmutableMap.of(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT), GuardConfig.COMMON.professionsThatHeal.get());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Villager owner) {
        List<LivingEntity> list = owner.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).get();
        if (!list.isEmpty()) {
            for (LivingEntity searchedForHeal : list) {
                if (searchedForHeal instanceof Guard || searchedForHeal.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) || searchedForHeal instanceof Villager) {
                    if (searchedForHeal.getHealth() < searchedForHeal.getMaxHealth() && searchedForHeal.distanceTo(owner) <= 4.0D) {
                        this.targetToHeal = searchedForHeal;
                        return super.checkExtraStartConditions(level, owner);
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected long timeToCheck(LivingEntity owner) {
        return owner.getData(GuardDataAttachments.LAST_THROWN_POTION.get());
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Villager entity, long gameTime) {
        return targetToHeal != null && checkIfDayHavePassedFromLastActivity(entity) && entity.getData(GuardDataAttachments.TIMES_THROWN_POTION.get()) < GuardConfig.COMMON.maxClericHeal.get();
    }


    @Override
    protected void tick(ServerLevel level, Villager owner, long gameTime) {
        super.tick(level, owner, gameTime);
        if (this.targetToHeal == null)
            return;
        BehaviorUtils.lookAtEntity(owner, targetToHeal);
        owner.lookAt(this.targetToHeal, 30.0F, 30.0F);
        owner.getLookControl().setLookAt(this.targetToHeal);
        if (!owner.hasLineOfSight(this.targetToHeal)) {
            this.waitUntilInSightTicks += 5;
        } else if (waitUntilInSightTicks > 0) this.waitUntilInSightTicks--;
        if (waitUntilInSightTicks == 0) {
            this.throwPotion(owner);
            this.waitUntilInSightTicks = 40;
        }
    }

    @Override
    protected void stop(ServerLevel level, Villager entity, long gameTime) {
        super.stop(level, entity, gameTime);
        this.waitUntilInSightTicks = 40;
        if (entity.getData(GuardDataAttachments.TIMES_THROWN_POTION.get()) >= GuardConfig.COMMON.maxClericHeal.get()) {
            entity.setData(GuardDataAttachments.LAST_THROWN_POTION.get(), level.getDayTime());
            entity.setData(GuardDataAttachments.TIMES_THROWN_POTION.get(), 0);
        }
        this.targetToHeal = null;
    }

    @Override
    protected void start(ServerLevel level, Villager entity, long gameTime) {
        this.waitUntilInSightTicks = 40;
    }

    public void throwPotion(LivingEntity healer) {
        if (!(healer.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        healer.setData(GuardDataAttachments.TIMES_THROWN_POTION.get(), healer.getData(GuardDataAttachments.TIMES_THROWN_POTION.get()) + 1);
        Holder<Potion> potion = targetToHeal.getHealth() > 4.0F ? Potions.REGENERATION : Potions.HEALING;
        ItemStack potionStack = PotionContents.createItemStack(Items.SPLASH_POTION, potion);
        ThrownPotion potionentity = new ThrownPotion(EntityType.POTION, serverLevel);
        potionentity.setOwner(healer);
        potionentity.setItem(potionStack.copy());
        potionentity.setItem(PotionContents.createItemStack(Items.SPLASH_POTION, potion));
        potionentity.shootFromRotation(healer, healer.getViewXRot(1.0F), healer.getYHeadRot(), -20.0F, 0.5F, 0.0F);
        healer.level().playSound(null, healer.getX(), healer.getY(), healer.getZ(), SoundEvents.SPLASH_POTION_THROW, healer.getSoundSource(), 1.0F, 0.8F + healer.getRandom().nextFloat() * 0.4F);
        healer.level().addFreshEntity(potionentity);
    }
}
