package tallestegg.guardvillagers;

import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import tallestegg.guardvillagers.client.GuardSounds;
import tallestegg.guardvillagers.common.entities.Guard;
import tallestegg.guardvillagers.configuration.GuardConfig;

/** Lightweight event hooks for the simple-AI branch. */
public final class HandlerEvents {
    private HandlerEvents() {}

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || event.getHand() != InteractionHand.MAIN_HAND) return;

        ItemStack stack = player.getMainHandItem();
        if (!stack.is(GuardVillagerTags.GUARD_CONVERT) || !player.isCrouching()) return;
        if (!(event.getTarget() instanceof Villager villager) || villager.isBaby()) return;
        if (GuardConfig.COMMON.ConvertVillagerIfHaveHOTV.get() && !player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE)) return;
        if (!GuardConfig.COMMON.convertibleProfessions.get().contains(professionId(villager))) return;

        convertVillager(villager, player);
        if (!player.getAbilities().instabuild) stack.shrink(1);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    private static void convertVillager(Villager villager, Player player) {
        Level level = villager.level();
        Guard guard = GuardEntityType.GUARD.get().create(level, EntitySpawnReason.EVENT);
        if (guard == null) return;

        guard.copyPosition(villager);
        guard.playSound(GuardSounds.GUARD_YES.value(), 1.0F, 1.0F);
        guard.setVariant(Guard.getVariantFromBiome(level, villager.blockPosition()));
        guard.setPersistenceRequired();
        guard.setCustomName(villager.getCustomName());
        guard.setCustomNameVisible(villager.isCustomNameVisible());
        guard.setItemSlot(EquipmentSlot.MAINHAND, player.getMainHandItem().copy());
        guard.setDropChance(EquipmentSlot.HEAD, 1.0F);
        guard.setDropChance(EquipmentSlot.CHEST, 1.0F);
        guard.setDropChance(EquipmentSlot.FEET, 1.0F);
        guard.setDropChance(EquipmentSlot.LEGS, 1.0F);
        guard.setDropChance(EquipmentSlot.MAINHAND, 1.0F);
        guard.setDropChance(EquipmentSlot.OFFHAND, 1.0F);

        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    villager.getX(), villager.getY() + 0.5D, villager.getZ(),
                    10, villager.getBbWidth(), villager.getBbHeight() * 0.5D,
                    villager.getBbWidth(), 0.02D);
        }

        level.addFreshEntity(guard);
        villager.releasePoi(net.minecraft.world.entity.ai.memory.MemoryModuleType.HOME);
        villager.releasePoi(net.minecraft.world.entity.ai.memory.MemoryModuleType.JOB_SITE);
        villager.releasePoi(net.minecraft.world.entity.ai.memory.MemoryModuleType.MEETING_POINT);
        villager.discard();

        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            CriteriaTriggers.SUMMONED_ENTITY.trigger(serverPlayer, guard);
            player.awardStat(GuardStats.GUARDS_MADE.get());
        }
    }

    private static String professionId(Villager villager) {
        return villager.getVillagerData().profession().unwrapKey()
                .map(key -> key.identifier().getPath())
                .orElse(villager.getVillagerData().profession().getRegisteredName());
    }
}
