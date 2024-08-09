package tallestegg.guardvillagers;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class GuardStats {
    public static final DeferredRegister<ResourceLocation> STATS = DeferredRegister.create(Registries.CUSTOM_STAT, GuardVillagers.MODID);
    public static final RegistryObject<ResourceLocation> GUARDS_MADE = STATS.register("guards_made", () -> new ResourceLocation(GuardVillagers.MODID, "guards_made"));
}
