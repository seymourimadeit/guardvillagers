package tallestegg.guardvillagers;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Optional;
import java.util.function.Supplier;

public class GuardDataAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, GuardVillagers.MODID);
    public static final Supplier<AttachmentType<Integer>> TIMES_THROWN_POTION = ATTACHMENT_TYPES.register(
            "times_thrown_potion",() -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build()
    );
    public static final Supplier<AttachmentType<Integer>> TIMES_HEALED_GOLEM = ATTACHMENT_TYPES.register(
            "times_repaired_golem",() -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build()
    );
    public static final Supplier<AttachmentType<Integer>> TIMES_REPAIRED_GUARD = ATTACHMENT_TYPES.register(
            "times_repaired_guard",() -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build()
    );
    public static final Supplier<AttachmentType<Long>> LAST_REPAIRED_GOLEM = ATTACHMENT_TYPES.register(
            "last_repaired_golem",() -> AttachmentType.builder(() -> 0L).serialize(Codec.LONG).build()
    );
    public static final Supplier<AttachmentType<Long>> LAST_THROWN_POTION = ATTACHMENT_TYPES.register(
            "last_thrown_potion",() -> AttachmentType.builder(() -> 0L).serialize(Codec.LONG).build()
    );
    public static final Supplier<AttachmentType<Long>> LAST_REPAIRED_GUARD = ATTACHMENT_TYPES.register(
            "last_repaired_guard",() -> AttachmentType.builder(() -> 0L).serialize(Codec.LONG).build()
    );
}
