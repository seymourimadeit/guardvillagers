package tallestegg.guardvillagers;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Consumer;

public class GuardLootTables {
    public static final DeferredRegister<LootTable> LOOT_TABLE = DeferredRegister.create(Registries.LOOT_TABLE, GuardVillagers.MODID);
    public static final BiMap<ResourceLocation, LootContextParamSet> REGISTRY = HashBiMap.create();
    public static final LootContextParamSet SLOT = register("slot", (p_216252_0_) -> {
        p_216252_0_.required(LootContextParams.THIS_ENTITY);
    });
    public static final ResourceKey<LootTable> GUARD_MAIN_HAND = registerLootTable("entities/guard_main_hand");
    public static final ResourceKey<LootTable> GUARD_OFF_HAND = registerLootTable("entities/guard_off_hand");
    public static final ResourceKey<LootTable> GUARD_HELMET = registerLootTable("entities/guard_helmet");
    public static final ResourceKey<LootTable> GUARD_CHEST = registerLootTable("entities/guard_chestplate");
    public static final ResourceKey<LootTable> GUARD_LEGGINGS = registerLootTable("entities/guard_legs");
    public static final ResourceKey<LootTable> GUARD_FEET = registerLootTable("entities/guard_feet");

    public static ResourceKey<LootTable> registerLootTable(String id) {
        return ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(GuardVillagers.MODID, id));
    }

    public static LootContextParamSet register(String p_81429_, Consumer<LootContextParamSet.Builder> p_81430_) {
        LootContextParamSet.Builder lootcontextparamset$builder = new LootContextParamSet.Builder();
        p_81430_.accept(lootcontextparamset$builder);
        LootContextParamSet lootcontextparamset = lootcontextparamset$builder.build();
        ResourceLocation resourcelocation = ResourceLocation.parse(GuardVillagers.MODID + p_81429_);
        LootContextParamSet lootcontextparamset1 = REGISTRY.put(resourcelocation, lootcontextparamset);
        if (lootcontextparamset1 != null) {
            throw new IllegalStateException("Loot table parameter set " + resourcelocation + " is already registered");
        } else {
            return lootcontextparamset;
        }
    }
}
