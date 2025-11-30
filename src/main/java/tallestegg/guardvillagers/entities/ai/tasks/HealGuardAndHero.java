package tallestegg.guardvillagers.entities.ai.tasks;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import tallestegg.guardvillagers.GuardMemoryTypes;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.entities.Guard;

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
        Optional<Long> optional = owner.getBrain().getMemory(GuardMemoryTypes.LAST_THROWN_POTION.get());
        return optional.isPresent() ? optional.get() : 0;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Villager entity, long gameTime) {
        return checkIfDayHavePassedFromLastActivity(entity) && entity.getBrain().getMemory(GuardMemoryTypes.TIMES_THROWN_POTION.get()).orElse(null) < GuardConfig.COMMON.maxClericHeal.get();
    }


    @Override
    protected void tick(ServerLevel level, Villager owner, long gameTime) {
        super.tick(level, owner, gameTime);
        BehaviorUtils.lookAtEntity(owner, targetToHeal);
        owner.lookAt(this.targetToHeal, 30.0F, 30.0F);
        owner.getLookControl().setLookAt(this.targetToHeal);
        if (!owner.hasLineOfSight(this.targetToHeal)) {
            this.waitUntilInSightTicks += 5;
        } else this.waitUntilInSightTicks--;
        if (waitUntilInSightTicks == 0) this.throwPotion(owner);
    }

    @Override
    protected void stop(ServerLevel level, Villager entity, long gameTime) {
        super.stop(level, entity, gameTime);
        if (entity.getBrain().getMemory(GuardMemoryTypes.TIMES_THROWN_POTION.get()).orElse(null) >= GuardConfig.COMMON.maxClericHeal.get()) {
            entity.getBrain().setMemory(GuardMemoryTypes.LAST_THROWN_POTION.get(), level.getDayTime());
            entity.getBrain().setMemory(GuardMemoryTypes.TIMES_THROWN_POTION.get(), 0);
        }
    }

    @Override
    protected void start(ServerLevel level, Villager entity, long gameTime) {
        this.waitUntilInSightTicks = 10;
        if (!entity.getBrain().hasMemoryValue(GuardMemoryTypes.TIMES_THROWN_POTION.get())) {
            entity.getBrain().setMemory(GuardMemoryTypes.TIMES_THROWN_POTION.get(), 0);
        }
    }

    public void throwPotion(LivingEntity healer) {
        healer.getBrain().setMemory(GuardMemoryTypes.TIMES_THROWN_POTION.get(), healer.getBrain().getMemory(GuardMemoryTypes.TIMES_THROWN_POTION.get()).orElse(null) + 1);
        Potion potion = Potions.REGENERATION;
        if (targetToHeal.getHealth() <= 4.0F) {
            potion = Potions.HEALING;
        }
        ThrownPotion potionentity = new ThrownPotion(healer.level(), healer);
        potionentity.setItem(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), potion));
        potionentity.shootFromRotation(healer, healer.getViewXRot(1.0F), healer.getYHeadRot(), -20.0F, 0.5F, 0.0F);
        healer.level().playSound(null, healer.getX(), healer.getY(), healer.getZ(), SoundEvents.SPLASH_POTION_THROW, healer.getSoundSource(), 1.0F, 0.8F + healer.getRandom().nextFloat() * 0.4F);
        healer.level().addFreshEntity(potionentity);
    }
}