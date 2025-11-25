package tallestegg.guardvillagers;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;

public class GuardMemoryTypes {
    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPE = DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, GuardVillagers.MODID);
    public static final RegistryObject<MemoryModuleType<Long>> LAST_REPAIRED_GOLEM = MEMORY_MODULE_TYPE.register("last_repaired_golem", () -> new MemoryModuleType<>(Optional.of(Codec.LONG)));
    public static final RegistryObject<MemoryModuleType<Integer>> TIMES_THROWN_POTION = MEMORY_MODULE_TYPE.register("times_thrown_potion", () -> new MemoryModuleType<>(Optional.of(Codec.INT)));
    public static final RegistryObject<MemoryModuleType<Integer>> TIMES_HEALED_GOLEM = MEMORY_MODULE_TYPE.register("times_repaired_golem", () -> new MemoryModuleType<>(Optional.of(Codec.INT)));
    public static final RegistryObject<MemoryModuleType<Integer>> TIMES_REPAIRED_GUARD = MEMORY_MODULE_TYPE.register("times_repaired_guard", () -> new MemoryModuleType<>(Optional.of(Codec.INT)));
    public static final RegistryObject<MemoryModuleType<Long>> LAST_THROWN_POTION = MEMORY_MODULE_TYPE.register("last_thrown_potion", () -> new MemoryModuleType<>(Optional.of(Codec.LONG)));
    public static final RegistryObject<MemoryModuleType<Long>> LAST_REPAIRED_GUARD = MEMORY_MODULE_TYPE.register("last_repaired_guard", () -> new MemoryModuleType<>(Optional.of(Codec.LONG)));
}
