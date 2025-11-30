package tallestegg.guardvillagers.mixins;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tallestegg.guardvillagers.GuardMemoryTypes;

import java.util.ArrayList;
import java.util.List;

@Mixin(Villager.class)
public class VillagerMixin {
    @Shadow @Final private static ImmutableList<MemoryModuleType<?>> MEMORY_TYPES;

    static{
        MEMORY_TYPES = ImmutableList.<MemoryModuleType<?>>builder()
                .addAll(MEMORY_TYPES)
                .addAll(ImmutableList.of(GuardMemoryTypes.LAST_REPAIRED_GOLEM.get(),
                        GuardMemoryTypes.LAST_REPAIRED_GUARD.get(),
                        GuardMemoryTypes.LAST_THROWN_POTION.get(),
                        GuardMemoryTypes.TIMES_REPAIRED_GUARD.get(),
                        GuardMemoryTypes.TIMES_HEALED_GOLEM.get(),
                        GuardMemoryTypes.TIMES_THROWN_POTION.get())).build();
    }
}
