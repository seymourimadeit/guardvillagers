package tallestegg.guardvillagers.mixins;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tallestegg.guardvillagers.entities.GuardVillagersVillagerData;

@Mixin(Villager.class)
public abstract class VillagerMixin implements GuardVillagersVillagerData {
    private int timesThrownPotion;
    private int timesHealedGolem;
    private int timesRepairedGuard;
    private long lastThrownPotion;
    private long lastRepairedGolem;
    private long lastRepairedGuard;

    @Override
    public int getTimesThrownPotion() {
        return timesThrownPotion;
    }

    @Override
    public int getTimesHealedGolem() {
        return timesHealedGolem;
    }

    @Override
    public int getTimesRepairedGuard() {
        return timesRepairedGuard;
    }

    @Override
    public long getLastRepairedGolem() {
        return lastRepairedGolem;
    }

    @Override
    public long getLastRepairedGuard() {
        return lastRepairedGuard;
    }

    @Override
    public long getLastThrownPotion() {
        return lastThrownPotion;
    }

    @Override
    public void setTimesThrownPotion(int timesThrownPotion) {
        this.timesThrownPotion = timesThrownPotion;
    }

    @Override
    public void setTimesHealedGolem(int timesHealedGolem) {
        this.timesHealedGolem = timesHealedGolem;
    }

    @Override
    public void setTimesRepairedGuard(int timesRepairedGuard) {
        this.timesRepairedGuard = timesRepairedGuard;
    }

    @Override
    public void setLastRepairedGolem(long lastRepairedGolem) {
        this.lastRepairedGolem = lastRepairedGolem;
    }

    @Override
    public void setLastRepairedGuard(long lastRepairedGuard) {
        this.lastRepairedGuard = lastRepairedGuard;
    }

    @Override
    public void setLastHealedGuard(long lastHealedGuard) {
        this.lastThrownPotion = lastHealedGuard;
    }

    @Inject(method = "readAdditionalSaveData", at = @At(value = "TAIL"))
    public void guardvillagers_readAdditionalSaveData(CompoundTag pCompound, CallbackInfo ci) {
        this.lastRepairedGuard = pCompound.getLong("lastRepairedGuard");
        this.lastThrownPotion = pCompound.getLong("lastThrownPotion");
        this.lastRepairedGolem = pCompound.getLong("lastRepairedGolem");
        this.timesRepairedGuard = pCompound.getInt("timesRepairedGuard");
        this.timesThrownPotion = pCompound.getInt("timesThrownPotion");
        this.timesHealedGolem = pCompound.getInt("timesRepairedGolem");


    }

    @Inject(method = "addAdditionalSaveData", at = @At(value = "TAIL"))
    public void guardvillagers_addAdditionalSaveData(CompoundTag pCompound, CallbackInfo ci) {
        pCompound.putLong("lastRepairedGuard", this.lastRepairedGuard);
        pCompound.putLong("lastThrownPotion", this.lastThrownPotion);
        pCompound.putLong("lastRepairedGolem", this.lastRepairedGolem);
        pCompound.putInt("timesRepairedGuard", this.timesRepairedGuard);
        pCompound.putInt("timesThrownPotion", this.timesThrownPotion);
        pCompound.putInt("timesRepairedGolem", this.timesHealedGolem);
    }


}
