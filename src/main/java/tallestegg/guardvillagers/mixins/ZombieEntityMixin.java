package tallestegg.guardvillagers.mixins;

/*@Mixin(Zombie.class)
public class ZombieEntityMixin extends Monster {
    protected ZombieEntityMixin(EntityType<? extends Monster> type, Level worldIn) {
        super(type, worldIn);
    }

    @Inject(at = @At(value = "TAIL"), method = "killed(Lnet/minecraft/world/server/ServerWorld;Lnet/minecraft/entity/LivingEntity;)V", remap = false)
    public void onKill(ServerLevel p_241847_1_, LivingEntity p_241847_2_, CallbackInfo info) {
        if ((p_241847_1_.getDifficulty() == Difficulty.NORMAL || p_241847_1_.getDifficulty() == Difficulty.HARD) && p_241847_2_ instanceof GuardEntity) {
            if (p_241847_1_.getDifficulty() != Difficulty.HARD && this.random.nextBoolean()) {
                return;
            }
            GuardEntity guard = (GuardEntity) p_241847_2_;
            ZombieVillager zombieguard = guard.convertTo(EntityType.ZOMBIE_VILLAGER, true);
            zombieguard.finalizeSpawn(p_241847_1_, p_241847_1_.getCurrentDifficultyAt(zombieguard.blockPosition()), MobSpawnType.CONVERSION, new Zombie.ZombieGroupData(false, true), (CompoundTag) null);
            if (!this.isSilent())
                p_241847_1_.levelEvent((Player) null, 1026, this.blockPosition(), 0);
        }
    }
}*/
