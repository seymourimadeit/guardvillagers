package tallestegg.guardvillagers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.entities.GuardEntity;

@Mod.EventBusSubscriber(modid = GuardVillagers.MODID)
public class VillagerToGuard {
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack itemstack = event.getItemStack();
        if (itemstack.getItem() instanceof SwordItem && event.getPlayer().isCrouching() || itemstack.getItem() instanceof CrossbowItem && event.getPlayer().isCrouching()) {
            Entity target = event.getTarget();
            if (target instanceof VillagerEntity) {
                VillagerEntity villager = (VillagerEntity) event.getTarget();
                if (!villager.isChild()) {
                    if (villager.getVillagerData().getProfession() == VillagerProfession.NONE || villager.getVillagerData().getProfession() == VillagerProfession.NITWIT) {
                        if (!GuardConfig.ConvertVillagerIfHaveHOTV || event.getPlayer().isPotionActive(Effects.HERO_OF_THE_VILLAGE) && GuardConfig.ConvertVillagerIfHaveHOTV) {
                            VillagerToGuard.convertVillager(villager, event.getPlayer());
                            if (!event.getPlayer().abilities.isCreativeMode)
                                itemstack.shrink(1);
                        }
                    }
                }
            }
        }
    }

     private static void convertVillager(LivingEntity entity, PlayerEntity player) {
        player.swingArm(Hand.MAIN_HAND);
        ItemStack itemstack = player.getItemStackFromSlot(EquipmentSlotType.MAINHAND);
        GuardEntity guard = GuardEntityType.GUARD.get().create(entity.world);
        VillagerEntity villager = (VillagerEntity) entity;
        if (guard == null)
            return;
        if (entity.world.isRemote) {
            IParticleData iparticledata = ParticleTypes.HAPPY_VILLAGER;
            for (int i = 0; i < 10; ++i) {
                double d0 = villager.getRNG().nextGaussian() * 0.02D;
                double d1 = villager.getRNG().nextGaussian() * 0.02D;
                double d2 = villager.getRNG().nextGaussian() * 0.02D;
                villager.world.addParticle(iparticledata, villager.getPosX() + (double) (villager.getRNG().nextFloat() * villager.getWidth() * 2.0F) - (double) villager.getWidth(), villager.getPosY() + 0.5D + (double) (villager.getRNG().nextFloat() * villager.getHeight()),
                        villager.getPosZ() + (double) (villager.getRNG().nextFloat() * villager.getWidth() * 2.0F) - (double) villager.getWidth(), d0, d1, d2);
            }
        }
        guard.copyLocationAndAnglesFrom(villager);
        guard.playSound(SoundEvents.ENTITY_VILLAGER_YES, 1.0F, 1.0F);
        guard.setItemStackToSlot(EquipmentSlotType.MAINHAND, itemstack.copy());
        int i = GuardEntity.getRandomTypeForBiome(guard.world, guard.getPosition());
        guard.setGuardVariant(i);
        guard.enablePersistence();
        guard.setCustomName(villager.getCustomName());
        guard.setCustomNameVisible(villager.isCustomNameVisible());
        guard.setDropChance(EquipmentSlotType.HEAD, 100.0F);
        guard.setDropChance(EquipmentSlotType.CHEST, 100.0F);
        guard.setDropChance(EquipmentSlotType.FEET, 100.0F);
        guard.setDropChance(EquipmentSlotType.LEGS, 100.0F);
        guard.setDropChance(EquipmentSlotType.MAINHAND, 100.0F);
        guard.setDropChance(EquipmentSlotType.OFFHAND, 100.0F);
        villager.world.addEntity(guard);
        villager.resetMemoryPoint(MemoryModuleType.HOME);
        villager.resetMemoryPoint(MemoryModuleType.JOB_SITE);
        villager.resetMemoryPoint(MemoryModuleType.MEETING_POINT);
        villager.remove();
    }
}
