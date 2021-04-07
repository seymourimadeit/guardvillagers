package tallestegg.guardvillagers.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.monster.ZombieVillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import tallestegg.guardvillagers.entities.GuardEntity;

@Mixin(ZombieEntity.class)
public class ZombieEntityMixin extends MonsterEntity {
    protected ZombieEntityMixin(EntityType<? extends MonsterEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Inject(at = @At(value = "TAIL"), method = "func_241847_a(Lnet/minecraft/world/server/ServerWorld;Lnet/minecraft/entity/LivingEntity;)V", remap = false)
    public void onKill(ServerWorld p_241847_1_, LivingEntity p_241847_2_, CallbackInfo info) {
        if ((p_241847_1_.getDifficulty() == Difficulty.NORMAL || p_241847_1_.getDifficulty() == Difficulty.HARD) && p_241847_2_ instanceof GuardEntity) {
            if (p_241847_1_.getDifficulty() != Difficulty.HARD && this.rand.nextBoolean()) {
                return;
            }
            GuardEntity guard = (GuardEntity) p_241847_2_;
            ZombieVillagerEntity zombieguard = guard.func_233656_b_(EntityType.ZOMBIE_VILLAGER, true);
            zombieguard.onInitialSpawn(p_241847_1_, p_241847_1_.getDifficultyForLocation(zombieguard.getPosition()), SpawnReason.CONVERSION, new ZombieEntity.GroupData(false, true), (CompoundNBT) null);
            if (!this.isSilent())
                p_241847_1_.playEvent((PlayerEntity) null, 1026, this.getPosition(), 0);
        }
    }
}
