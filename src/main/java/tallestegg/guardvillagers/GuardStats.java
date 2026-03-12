package tallestegg.guardvillagers;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stat;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GuardStats {
    public static final DeferredRegister<Identifier> STATS = DeferredRegister.create(Registries.CUSTOM_STAT, GuardVillagers.MODID);
    public static final DeferredHolder<Identifier, Identifier> GUARDS_MADE = STATS.register("guards_made", () -> Identifier.fromNamespaceAndPath(GuardVillagers.MODID, "guards_made"));
}
