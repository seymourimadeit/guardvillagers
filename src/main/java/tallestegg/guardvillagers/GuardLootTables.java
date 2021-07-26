package tallestegg.guardvillagers;

import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.resources.ResourceLocation;

public class GuardLootTables {
    public static final LootContextParamSet SLOT = LootContextParamSets.register("slot", (p_216252_0_) -> {
        p_216252_0_.required(LootContextParams.THIS_ENTITY);
    });
    
    public static final ResourceLocation GUARD_MAIN_HAND = new ResourceLocation(GuardVillagers.MODID, "entities/guard_main_hand");
    public static final ResourceLocation GUARD_OFF_HAND = new ResourceLocation(GuardVillagers.MODID, "entities/guard_off_hand");
    public static final ResourceLocation GUARD_HELMET = new ResourceLocation(GuardVillagers.MODID, "entities/guard_helmet");
    public static final ResourceLocation GUARD_CHEST = new ResourceLocation(GuardVillagers.MODID, "entities/guard_chestplate");
    public static final ResourceLocation GUARD_LEGGINGS = new ResourceLocation(GuardVillagers.MODID, "entities/guard_legs");
    public static final ResourceLocation GUARD_FEET = new ResourceLocation(GuardVillagers.MODID, "entities/guard_feet");
}
