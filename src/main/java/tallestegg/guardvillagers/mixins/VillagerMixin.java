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

    @Shadow @Final private static ImmutableList<SensorType<? extends Sensor<? super Villager>>> SENSOR_TYPES;


    @Inject(method = "brainProvider", cancellable = true, at = @At("RETURN"))
    public void brainProvider(CallbackInfoReturnable<Brain.Provider<Villager>> cir) {
        List<MemoryModuleType<?>> VILLAGER_MEMORY_TYPES = new ArrayList<>(MEMORY_TYPES);
        VILLAGER_MEMORY_TYPES.add(GuardMemoryTypes.LAST_REPAIRED_GOLEM.get());
        VILLAGER_MEMORY_TYPES.add(GuardMemoryTypes.LAST_REPAIRED_GUARD.get());
        VILLAGER_MEMORY_TYPES.add(GuardMemoryTypes.LAST_THROWN_POTION.get());
        VILLAGER_MEMORY_TYPES.add(GuardMemoryTypes.TIMES_REPAIRED_GUARD.get());
        VILLAGER_MEMORY_TYPES.add(GuardMemoryTypes.TIMES_HEALED_GOLEM.get());
        VILLAGER_MEMORY_TYPES.add(GuardMemoryTypes.TIMES_THROWN_POTION.get());
        cir.setReturnValue(Brain.provider(ImmutableList.copyOf(VILLAGER_MEMORY_TYPES), SENSOR_TYPES));
    }
}
