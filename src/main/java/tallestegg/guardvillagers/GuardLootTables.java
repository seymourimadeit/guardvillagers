package tallestegg.guardvillagers;

import net.minecraft.loot.LootParameterSet;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.util.ResourceLocation;

public class GuardLootTables {
    public static final LootParameterSet SLOT = LootParameterSets.register("slot", (p_216252_0_) -> {
        p_216252_0_.required(LootParameters.THIS_ENTITY);
    });
    
    public static final ResourceLocation GUARD_MAIN_HAND = new ResourceLocation(GuardVillagers.MODID, "entity/guard_main_hand");
    public static final ResourceLocation GUARD_OFF_HAND = new ResourceLocation(GuardVillagers.MODID, "entity/guard_off_hand");
    public static final ResourceLocation GUARD_HELMET = new ResourceLocation(GuardVillagers.MODID, "entity/guard_helmet");
    public static final ResourceLocation GUARD_CHEST = new ResourceLocation(GuardVillagers.MODID, "entity/guard_chestplate");
    public static final ResourceLocation GUARD_LEGGINGS = new ResourceLocation(GuardVillagers.MODID, "entity/guard_legs");
    public static final ResourceLocation GUARD_FEET = new ResourceLocation(GuardVillagers.MODID, "entity/guard_feet");
}
