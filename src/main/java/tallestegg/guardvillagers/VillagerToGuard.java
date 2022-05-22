package tallestegg.guardvillagers;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.entities.Guard;

@Mod.EventBusSubscriber(modid = GuardVillagers.MODID)
public class VillagerToGuard {
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack itemstack = event.getPlayer().getMainHandItem();
        Entity target = event.getTarget();
        if ((itemstack.getItem() instanceof SwordItem || itemstack.getItem() instanceof CrossbowItem) && event.getPlayer().isCrouching()) {
            if (target instanceof Villager villager) {
                if (!villager.isBaby()) {
                    if (villager.getVillagerData().getProfession() == VillagerProfession.NONE || villager.getVillagerData().getProfession() == VillagerProfession.NITWIT) {
                        if (!GuardConfig.ConvertVillagerIfHaveHOTV || event.getPlayer().hasEffect(MobEffects.HERO_OF_THE_VILLAGE) && GuardConfig.ConvertVillagerIfHaveHOTV) {
                            VillagerToGuard.convertVillager(villager, event.getPlayer());
                            if (!event.getPlayer().getAbilities().instabuild)
                                itemstack.shrink(1);
                        }
                    }
                }
            }
        }
    }

     private static void convertVillager(LivingEntity entity, Player player) {
        player.swing(InteractionHand.MAIN_HAND);
        ItemStack itemstack = player.getItemBySlot(EquipmentSlot.MAINHAND);
        Guard guard = GuardEntityType.GUARD.get().create(entity.level);
        Villager villager = (Villager) entity;
        if (guard == null)
            return;
        if (entity.level.isClientSide) {
            ParticleOptions iparticledata = ParticleTypes.HAPPY_VILLAGER;
            for (int i = 0; i < 10; ++i) {
                double d0 = villager.getRandom().nextGaussian() * 0.02D;
                double d1 = villager.getRandom().nextGaussian() * 0.02D;
                double d2 = villager.getRandom().nextGaussian() * 0.02D;
                villager.level.addParticle(iparticledata, villager.getX() + (double) (villager.getRandom().nextFloat() * villager.getBbWidth() * 2.0F) - (double) villager.getBbWidth(), villager.getY() + 0.5D + (double) (villager.getRandom().nextFloat() * villager.getBbHeight()),
                        villager.getZ() + (double) (villager.getRandom().nextFloat() * villager.getBbWidth() * 2.0F) - (double) villager.getBbWidth(), d0, d1, d2);
            }
        }
        guard.copyPosition(villager);
        guard.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.0F);
        guard.setItemSlot(EquipmentSlot.MAINHAND, itemstack.copy());
        int i = Guard.getRandomTypeForBiome(guard.level, guard.blockPosition());
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
        guard.getGossips().add(player.getUUID(), GossipType.MINOR_POSITIVE, GuardConfig.reputationRequirement);
        villager.level.addFreshEntity(guard);
        villager.releasePoi(MemoryModuleType.HOME);
        villager.releasePoi(MemoryModuleType.JOB_SITE);
        villager.releasePoi(MemoryModuleType.MEETING_POINT);
        villager.discard();
    }
}
