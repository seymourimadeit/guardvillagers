package tallestegg.guardvillagers.mixins;

/*import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.entity.ai.brain.task.SpawnGolemTask;
import net.minecraft.entity.ai.brain.task.VillagerTasks;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import tallestegg.guardvillagers.entities.ai.goals.tasks.HealGuardAndPlayerTask;
import tallestegg.guardvillagers.entities.ai.goals.tasks.RepairGolemTask;

@Mixin(VillagerTasks.class)
public class VillagerTasksMixin {
    @ModifyVariable(at = @At(value = "STORE", ordinal = 0), method = "work")
    private static SpawnGolemTask work(SpawnGolemTask task, VillagerProfession profession, float p_220639_1_) {
        if (profession == VillagerProfession.CLERIC) {
            task = new HealGuardAndPlayerTask(100, 0, 10.0F, 10.0F);
        } else if (profession == VillagerProfession.ARMORER || profession == VillagerProfession.WEAPONSMITH || profession == VillagerProfession.TOOLSMITH) {
            task = new RepairGolemTask();
        }
        return task;
    }
}*/
