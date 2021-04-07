package tallestegg.guardvillagers;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import tallestegg.guardvillagers.entities.GuardEntity;

@Mod.EventBusSubscriber(modid = GuardVillagers.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GuardEntityType {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, GuardVillagers.MODID);
    public static final RegistryObject<EntityType<GuardEntity>> GUARD = ENTITIES.register("guard", () -> EntityType.Builder.create(GuardEntity::new, EntityClassification.MISC).size(0.6F, 1.95F).setShouldReceiveVelocityUpdates(true).build(GuardVillagers.MODID + "guard"));
}
