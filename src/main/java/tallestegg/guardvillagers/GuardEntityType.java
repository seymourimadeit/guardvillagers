package tallestegg.guardvillagers;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tallestegg.guardvillagers.entities.Guard;

//@Mod.EventBusSubscriber(modid = GuardVillagers.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GuardEntityType {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, GuardVillagers.MODID);
    public static final DeferredHolder<EntityType<?>, EntityType<Guard>> GUARD = ENTITIES.register("guard", () -> EntityType.Builder.of(Guard::new, MobCategory.MISC).sized(0.6F, 1.95F).setShouldReceiveVelocityUpdates(true).build(GuardVillagers.MODID + "guard"));
}
