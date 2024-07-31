package tallestegg.guardvillagers;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GuardStats {
    public static final DeferredRegister<ResourceLocation> STATS = DeferredRegister.create(Registries.CUSTOM_STAT, GuardVillagers.MODID);
    public static final DeferredHolder<ResourceLocation, ResourceLocation> GUARDS_MADE = STATS.register("guards_made", () -> ResourceLocation.fromNamespaceAndPath(GuardVillagers.MODID, "guards_made"));
}
