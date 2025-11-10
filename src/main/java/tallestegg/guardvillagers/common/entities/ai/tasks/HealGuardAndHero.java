package tallestegg.guardvillagers.common.entities.ai.tasks;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.phys.Vec3;
import tallestegg.guardvillagers.common.entities.Guard;

import java.util.List;

public class HealGuardAndHero extends Behavior<Villager> {
    private LivingEntity targetToHeal;
    private int waitUntilInSightTicks = 0;
    private int timesThrownPotion = 0;
    private long lastTimeSinceUsed = 0;

    public HealGuardAndHero() {
        super(ImmutableMap.of(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Villager owner) {
        long gameTime = level.getGameTime();
        Activity activity = owner.getBrain().getActiveNonCoreActivity().orElse(null);
        if (owner.getVillagerData().getProfession() != VillagerProfession.CLERIC && (activity != Activity.AVOID && activity != Activity.HIDE && activity != Activity.PANIC)) {
            return false;
        }
        if (gameTime - this.lastTimeSinceUsed < 24000L) {
            return false;
        } else {
            List<LivingEntity> list = owner.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).get();
            if (!list.isEmpty()) {
                for (LivingEntity searchedForHeal : list) {
                    if (searchedForHeal instanceof Guard || searchedForHeal.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) || searchedForHeal instanceof Villager) {
                        if (searchedForHeal.getHealth() < searchedForHeal.getMaxHealth() && searchedForHeal.distanceTo(owner) <= 4.0D) {
                            this.targetToHeal = searchedForHeal;
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Villager entity, long gameTime) {
        return this.targetToHeal.getHealth() < this.targetToHeal.getMaxHealth() && (!this.targetToHeal.hasEffect(MobEffects.HEAL) || !this.targetToHeal.hasEffect(MobEffects.REGENERATION));
    }

    @Override
    protected void tick(ServerLevel level, Villager owner, long gameTime) {
        super.tick(level, owner, gameTime);
        owner.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        owner.lookAt(this.targetToHeal, 30.0F, 30.0F);
        owner.getLookControl().setLookAt(this.targetToHeal);
        if (!owner.hasLineOfSight(this.targetToHeal)) {
            this.waitUntilInSightTicks += 5;
        } else
            this.waitUntilInSightTicks--;
        if (waitUntilInSightTicks == 0)
            this.throwPotion(owner);
    }

    @Override
    protected void stop(ServerLevel level, Villager entity, long gameTime) {
        super.stop(level, entity, gameTime);
        if (this.timesThrownPotion >= 3) {
            this.lastTimeSinceUsed = gameTime;
            this.timesThrownPotion = 0;
        }
    }

    @Override
    protected void start(ServerLevel level, Villager entity, long gameTime) {
        this.waitUntilInSightTicks = 10;
    }

    public void throwPotion(LivingEntity healer) {
        this.timesThrownPotion++;
        Holder<Potion> potion = targetToHeal.getHealth() > 4.0F ? Potions.REGENERATION : Potions.HEALING;
        ThrownPotion potionentity = new ThrownPotion(healer.level(), healer);
        potionentity.setItem(PotionContents.createItemStack(Items.SPLASH_POTION, potion));
        potionentity.shootFromRotation(healer, healer.getViewXRot(1.0F), healer.getYHeadRot(), -20.0F, 0.5F, 0.0F);
        healer.level().playSound(null, healer.getX(), healer.getY(), healer.getZ(), SoundEvents.SPLASH_POTION_THROW, healer.getSoundSource(), 1.0F, 0.8F + healer.getRandom().nextFloat() * 0.4F);
        healer.level().addFreshEntity(potionentity);
    }
}
