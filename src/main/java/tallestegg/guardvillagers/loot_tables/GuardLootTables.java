package tallestegg.guardvillagers.loot_tables;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tallestegg.guardvillagers.GuardVillagers;
import tallestegg.guardvillagers.loot_tables.functions.ArmorSlotFunction;

import java.util.function.Consumer;

public class GuardLootTables {
    public static final BiMap<ResourceLocation, ContextKeySet> REGISTRY = HashBiMap.create();
    public static final ContextKeySet SLOT = register("slot", (table) -> {
        table.required(LootContextParams.THIS_ENTITY);
    });

    public static final DeferredRegister<LootItemFunctionType<?>> LOOT_ITEM_FUNCTION_TYPES = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, GuardVillagers.MODID);
    public static final DeferredRegister<LootItemConditionType> LOOT_ITEM_CONDITION_TYPES = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, GuardVillagers.MODID);
    public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<ArmorSlotFunction>> ARMOR_SLOT = LOOT_ITEM_FUNCTION_TYPES.register("slot", () -> new LootItemFunctionType<>(ArmorSlotFunction.CODEC));

    public static ContextKeySet register(String p_81429_, Consumer<ContextKeySet.Builder> p_81430_) {
        ContextKeySet.Builder lootcontextparamset$builder = new ContextKeySet.Builder();
        p_81430_.accept(lootcontextparamset$builder);
        ContextKeySet lootcontextparamset = lootcontextparamset$builder.build();
        REGISTRY.put(ResourceLocation.fromNamespaceAndPath(GuardVillagers.MODID, p_81429_), lootcontextparamset);
        return lootcontextparamset;
    }
}
