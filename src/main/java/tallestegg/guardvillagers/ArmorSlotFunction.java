package tallestegg.guardvillagers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import static tallestegg.guardvillagers.GuardLootTables.ARMOR_SLOT;


public class ArmorSlotFunction extends LootItemConditionalFunction {
    final EquipmentSlot slot;

    ArmorSlotFunction(LootItemCondition[] pConditions, EquipmentSlot slot) {
        super(pConditions);
        this.slot = slot;
    }

    @Override
    protected ItemStack run(ItemStack pStack, LootContext pContext) {
        LivingEntity livingEntity = (LivingEntity) pContext.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (!livingEntity.hasItemInSlot(slot))
            livingEntity.setItemSlot(slot, pStack);
        return pStack;
    }

    @Override
    public LootItemFunctionType getType() {
        return ARMOR_SLOT.get();
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<ArmorSlotFunction> {
        public void serialize(JsonObject pJson, ArmorSlotFunction pValue, JsonSerializationContext pSerializationContext) {
            super.serialize(pJson, pValue, pSerializationContext);
            pJson.addProperty("slot", pValue.slot.getName());
        }

        public ArmorSlotFunction deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions) {
            return new ArmorSlotFunction(pConditions, EquipmentSlot.byName(GsonHelper.getAsString(pObject, "slot")));
        }
    }
}
