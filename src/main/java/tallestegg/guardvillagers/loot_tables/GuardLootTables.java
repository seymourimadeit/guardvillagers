package tallestegg.guardvillagers.loot_tables;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tallestegg.guardvillagers.GuardVillagers;
import tallestegg.guardvillagers.loot_tables.functions.ArmorSlotFunction;

import java.util.function.Consumer;

public class GuardLootTables {
    public static final BiMap<Identifier, ContextKeySet> REGISTRY = HashBiMap.create();
    public static final ContextKeySet SLOT = register("slot", (table) -> {
        table.required(LootContextParams.THIS_ENTITY);
    });

    public static final DeferredRegister<MapCodec<? extends LootItemFunction>> LOOT_ITEM_FUNCTION_TYPES = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, GuardVillagers.MODID);
    public static final DeferredRegister<MapCodec<? extends LootItemCondition>> LOOT_ITEM_CONDITION_TYPES = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, GuardVillagers.MODID);
    public static final DeferredHolder<MapCodec<? extends LootItemFunction>, MapCodec<ArmorSlotFunction>> ARMOR_SLOT = LOOT_ITEM_FUNCTION_TYPES.register("slot", () -> ArmorSlotFunction.CODEC);

    public static ContextKeySet register(String p_81429_, Consumer<ContextKeySet.Builder> p_81430_) {
        ContextKeySet.Builder lootcontextparamset$builder = new ContextKeySet.Builder();
        p_81430_.accept(lootcontextparamset$builder);
        ContextKeySet lootcontextparamset = lootcontextparamset$builder.build();
        REGISTRY.put(Identifier.fromNamespaceAndPath(GuardVillagers.MODID, p_81429_), lootcontextparamset);
        return lootcontextparamset;
    }
}
