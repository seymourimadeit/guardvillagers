package tallestegg.guardvillagers;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tallestegg.guardvillagers.common.entities.Guard;

//@Mod.EventBusSubscriber(modid = GuardVillagers.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GuardEntityType {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, GuardVillagers.MODID);
    public static final Supplier<EntityType<?>, EntityType<Guard>> GUARD = ENTITIES.register("guard", () -> EntityType.Builder.of(Guard::new, MobCategory.MISC).sized(0.6F, 1.90F).setShouldReceiveVelocityUpdates(true).build(prefix("guard")));


    private static ResourceKey<EntityType<?>> prefix(String path) {
        return ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(GuardVillagers.MODID, path));
    }
}
