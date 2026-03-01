package tallestegg.guardvillagers;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tallestegg.guardvillagers.common.entities.Guard;

public class GuardEntityType {
    private static final ResourceKey<EntityType<?>> GUARD_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(GuardVillagers.MODID, "guard")
    );

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, GuardVillagers.MODID);
    public static final DeferredHolder<EntityType<?>, EntityType<Guard>> GUARD =
            ENTITIES.register("guard", () ->
                    EntityType.Builder.of(Guard::new, MobCategory.MISC)
                            .sized(0.6F, 1.90F)
                            .ridingOffset(-0.7F)
                            .setShouldReceiveVelocityUpdates(true)
                            .build(GUARD_KEY)
            );
}
